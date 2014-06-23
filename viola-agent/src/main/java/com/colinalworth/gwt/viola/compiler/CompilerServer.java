package com.colinalworth.gwt.viola.compiler;

import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService;
import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService.StatusUpdateQueries;
import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.colinalworth.gwt.viola.service.AgentStatusService.CompiledProjectQueries;
import com.google.gwt.dev.ThreadedPermutationWorkerFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import rxf.core.Server;
import rxf.couch.guice.CouchModuleBuilder;

import java.io.IOException;

public class CompilerServer {

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("Compiler must be started with an ID as its first argument");
			System.exit(1);
		}
		String myId = args[0];

		// avoid forking, thread instead to keep it in the same jvm
		System.setProperty("gwt.jjs.permutationWorkerFactory", ThreadedPermutationWorkerFactory.class.getName());

		Injector i = Guice.createInjector(new ViolaModule(), new AbstractModule() {
			@Override
			protected void configure() {
				install(new CouchModuleBuilder("v")
						.withService(StatusUpdateQueries.class)
						.withService(CompiledProjectQueries.class)
						.build());
			}
		});

		new Thread() {
			public void run() {
				try {
					//blocking
					Server.init(null);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}.start();

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
