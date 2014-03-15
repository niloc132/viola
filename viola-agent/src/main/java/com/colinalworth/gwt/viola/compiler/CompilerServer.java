package com.colinalworth.gwt.viola.compiler;

import rxf.server.RelaxFactoryServer;
import rxf.server.guice.CouchModuleBuilder;
import rxf.server.guice.RxfModule;

import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService;
import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService.StatusUpdateQueries;
import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CompilerServer {

	public static void main(String[] args) throws Exception {
		//		BlobAntiPatternObject.DEBUG_SENDJSON = true;



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


		CouchCompiler c = i.getInstance(CouchCompiler.class);
		StatusUpdateService status = i.getInstance(StatusUpdateService.class);
		status.register();

//    Thread.sleep(100);

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
