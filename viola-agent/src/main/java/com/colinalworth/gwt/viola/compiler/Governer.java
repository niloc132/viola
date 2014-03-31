package com.colinalworth.gwt.viola.compiler;

import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService;
import com.colinalworth.gwt.viola.entity.AgentStatus;
import com.colinalworth.gwt.viola.entity.AgentStatus.State;
import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.colinalworth.gwt.viola.service.AgentStatusService;
import com.colinalworth.gwt.viola.service.AgentStatusService.CompiledProjectQueries;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import rxf.server.RelaxFactoryServer;
import rxf.server.guice.CouchModuleBuilder;
import rxf.server.guice.RxfModule;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO monitor updated agent.jar image, and cycle out all agents as available
public class Governer {

	public static void main(final String[] args) throws InterruptedException {
		//		BlobAntiPatternObject.DEBUG_SENDJSON = true;

		Injector i = Guice.createInjector(new ViolaModule(), new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("hostname")).to("0.0.0.0");
				bindConstant().annotatedWith(Names.named("port")).to(9002);

				try {
					bind(URL[].class).annotatedWith(Names.named("gwtCompilerClasspath")).toInstance(new URL[]{
							new URL("file:///home/colin/.m2/repository/com/google/gwt/gwt-dev/2.6.0/gwt-dev-2.6.0.jar"),
							new URL("file:///home/colin/.m2/repository/com/google/gwt/gwt-user/2.6.0/gwt-user-2.6.0.jar"),
							new URL("file:///home/colin/workspaces42/rebased/viola/target/classes/")
					});

					install(new CouchModuleBuilder("v")
							.withService(StatusUpdateService.StatusUpdateQueries.class)
							.withService(CompiledProjectQueries.class)
							.build());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				MapBinder<String, AgentManager> managers = MapBinder.newMapBinder(binder(), String.class, AgentManager.class);
				for (String arg : args) {
					String[] parts = arg.split(":", 2);
					try {
						managers.addBinding(parts[0]).to((Class) Class.forName(parts[1]));
					} catch (ClassNotFoundException e) {
						throw new RuntimeException("Failed to startup without " + parts[1] + " on classpath", e);
					}
				}
			}
		}, new RxfModule());

		final RelaxFactoryServer server = i.getInstance(RelaxFactoryServer.class);
		new Thread() {
			public void run() {
				try {
					//blocking
					server.start();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}.start();
		while (!server.isRunning()) {
			Thread.sleep(10);
		}
		AgentStatusService service = i.getInstance(AgentStatusService.class);
		Map<String, AgentManager> agentManagement = i.getInstance(Key.get(new TypeLiteral<Map<String, AgentManager>>(){}));

		int maxIdleAgents = 2;
		int minIdleAgents = 1;
		int maxIdleTime = 300;//seconds since working, 600 = 5 minutes
		int checkInterval = 150;//seconds between check, should be at least 2x CouchCompiler#jobCheckPeriodMillis
		Map<String, Date> lastHeardFrom = new HashMap<>();

		while (true) {
			//look for agents marked as shutdown or stuck, and power them off
			List<AgentStatus> killme = service.getAgentsInState(State.STOPPED, State.STUCK);
			for (AgentStatus agent : killme) {
				System.out.println("Stopping " + agent.getServerData() + " in state " + agent.getState());
				poweroff(agentManagement, agent);
			}

			// start/stop agents as necessary
			List<AgentStatus> maxIdleTimeAgents = service.getAgentsIdleMoreThan(maxIdleTime * 1000);
			List<AgentStatus> idleAgents = service.getAgentsInState(State.IDLE);
			if (maxIdleTimeAgents.size() > maxIdleAgents) {
				for (int index = 0; index < maxIdleTimeAgents.size() - maxIdleAgents; index++) {
					AgentStatus agent = maxIdleTimeAgents.get(index);
					System.out.println("Requesting shutdown for " + agent.getServerData());
					service.requestShutdown(agent);
				}
			} else if (idleAgents.size() < minIdleAgents) {
				// start servers!
				//TODO do something with these to make sure they start up correctly. Or is it enough to look for starting below?
				int agentsNeeded = minIdleAgents - idleAgents.size();
				System.out.println("Running low on agents, starting " + agentsNeeded + " more");
				List<AgentStatus> started = startServers(agentManagement, agentsNeeded);
			}

			//look for stuck/disconnected agents
			List<AgentStatus> runningAgents = service.getAgentsInState(State.IDLE, State.WORKING, State.STARTING, State.SHUTTING_DOWN);
			for (AgentStatus agent : runningAgents) {
				if (lastHeardFrom.containsKey(agent.getId()) &&
						lastHeardFrom.get(agent.getId()).equals(agent.getLastHeardFrom())) {
					service.markStuck(agent);
					System.out.println("Marking " + agent.getServerData() + " as stuck");
					//TODO look for any non-finished jobs and mark them as stuck as well
				} else {
					//if lastHeardFrom is null, then it stuck while starting, leave it running for manual inspection
					if (agent.getLastHeardFrom() != null) {
						lastHeardFrom.put(agent.getId(), agent.getLastHeardFrom());
					} else {
						System.out.println("Possible stuck while starting: " + agent.getServerData());
					}
				}
			}

			//misc cleanup for current agent impl
			cleanup(agentManagement);

			//wait interval, check again
			Thread.sleep(checkInterval * 1000);
		}
	}

	private static void poweroff(Map<String, AgentManager> agentManagement, AgentStatus agent) {
		AgentManager manager = agentManagement.get(agent.getServerType());
		if (manager == null) {
			System.err.println("No manager available for type " + agent.getServerType() + ", please stop agent manually: ");
			System.err.println("\tid: " + agent.getId() + "\n\tserverData: " + agent.getServerData() + "\n\tstate: " + agent.getState());
		} else {
			manager.poweroff(agent);
		}
	}

	private static List<AgentStatus> startServers(Map<String, AgentManager> agentManagement, int count) {
		List<AgentStatus> serversStarted = new ArrayList<>();
		for (AgentManager manager : agentManagement.values()) {
			List<AgentStatus> newlyStartedServers = manager.startServers(count);
			serversStarted.addAll(newlyStartedServers);
			count -= newlyStartedServers.size();
			if (count <= 0) {
				break;
			}
		}
		return serversStarted;
	}

	private static void cleanup(Map<String,AgentManager> agentManagement) {
		for (AgentManager manager : agentManagement.values()) {
			manager.cleanup();
		}
	}

	public interface AgentManager {
		void poweroff(AgentStatus agent);
		List<AgentStatus> startServers(int count);
		void cleanup();
	}
}
