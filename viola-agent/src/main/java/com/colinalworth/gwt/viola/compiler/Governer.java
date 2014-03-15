package com.colinalworth.gwt.viola.compiler;

import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService;
import com.colinalworth.gwt.viola.entity.AgentStatus;
import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.colinalworth.gwt.viola.service.AgentStatusService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import rxf.server.RelaxFactoryServer;
import rxf.server.guice.CouchModuleBuilder;
import rxf.server.guice.RxfModule;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Governer {

	public static void main(String[] args) throws InterruptedException {
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
							.build());
				} catch (MalformedURLException e) {
					e.printStackTrace();
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

		int maxIdleAgents = 3;
		int minIdleAgents = 1;
		int maxIdleTime = 120;
		int checkInterval = 30;//should be at least 2x CouchCompiler#jobCheckPeriodMillis
		Map<String, Date> lastHeardFrom = new HashMap<>();

		while (true) {
			// start/stop agents as necessary
			List<AgentStatus> idleAgents = service.getAgentsIdleMoreThan(maxIdleTime * 1000);
			if (idleAgents.size() > maxIdleAgents) {
				for (int index = 0; index < idleAgents.size() - maxIdleAgents; index++) {
					service.requestShutdown(idleAgents.get(index));
					System.out.println("Requested shutdown for " + idleAgents.get(index));
				}
			} else if (idleAgents.size() < minIdleAgents) {
				//TODO start servers!
				System.out.println("Manually start a server, we're running low!");
			}

			//look for stuck agents
			List<AgentStatus> runningAgents = service.getAgentsInState(AgentStatus.State.IDLE);
			for (AgentStatus agent : runningAgents) {
				if (lastHeardFrom.containsKey(agent.getId()) &&
						lastHeardFrom.get(agent.getId()).equals(agent.getLastHeardFrom())) {
					service.markStuck(agent);
				} else {
					lastHeardFrom.put(agent.getId(), agent.getLastHeardFrom());
				}
			}


			Thread.sleep(checkInterval * 1000);
		}
	}
}
