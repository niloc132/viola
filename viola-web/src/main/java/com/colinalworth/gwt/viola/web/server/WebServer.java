package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import rxf.server.BlobAntiPatternObject;
import rxf.server.RelaxFactoryServer;

import java.io.IOException;
import java.util.concurrent.Executors;

public class WebServer {

	public static void main(String[] args) throws Exception {
		BlobAntiPatternObject.EXECUTOR_SERVICE = Executors.newCachedThreadPool();
//		BlobAntiPatternObject.DEBUG_SENDJSON = true;

		Injector i = Guice.createInjector(new ViolaModule(), new ViolaWebModule());

		final RelaxFactoryServer server = i.getInstance(RelaxFactoryServer.class);
		new Thread() {
			public void run() {
				try {
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

		System.out.println("server successfully started");
	}
}
