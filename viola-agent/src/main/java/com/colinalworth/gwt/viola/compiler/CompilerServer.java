package com.colinalworth.gwt.viola.compiler;

import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService;
import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService.StatusUpdateQueries;
import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.colinalworth.gwt.viola.service.AgentStatusService.CompiledProjectQueries;
import com.google.gwt.dev.ThreadedPermutationWorkerFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import rxf.server.BlobAntiPatternObject;
import rxf.server.RelaxFactoryServer;
import rxf.server.guice.CouchModuleBuilder;
import rxf.server.guice.RxfModule;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;

public class CompilerServer {

	public static void main(String[] args) throws Exception {
		String myId = args[0];
		BlobAntiPatternObject.EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);
		//		BlobAntiPatternObject.DEBUG_SENDJSON = true;

		// avoid forking, thread instead to keep it in the same jvm
		System.setProperty("gwt.jjs.permutationWorkerFactory", ThreadedPermutationWorkerFactory.class.getName());

		Injector i = Guice.createInjector(new ViolaModule(), new AbstractModule() {

			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("hostname")).to("0.0.0.0");
				bindConstant().annotatedWith(Names.named("port")).to(9001);

				try {
					bind(URL[].class).annotatedWith(Names.named("gwtCompilerClasspath")).toInstance(new URL[]{
							new URL("file:///home/colin/.m2/repository/com/google/gwt/gwt-dev/2.6.0/gwt-dev-2.6.0.jar"),
							new URL("file:///home/colin/.m2/repository/com/google/gwt/gwt-user/2.6.0/gwt-user-2.6.0.jar"),
							new URL("file:///home/colin/workspaces42/rebased/viola/target/classes/")
					});

					install(new CouchModuleBuilder("v")
							.withService(StatusUpdateQueries.class)
							.withService(CompiledProjectQueries.class)
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

		// a minor delay here seems to make the system start significantly more consistently
		Thread.sleep(100);

		CouchCompiler c = i.getInstance(CouchCompiler.class);
		StatusUpdateService status = i.getInstance(StatusUpdateService.class);
		status.register(myId);


		//blocking call, run this until we're done
		try {
			c.serveUntilShutdown();
		} catch (Exception ex) {
			//if something goes wrong, emit error shutdown
			ex.printStackTrace();
			System.exit(1);
		}

		//in main, so exit with 0 to indicate successful safe shutdown
		System.exit(0);
	}


}
