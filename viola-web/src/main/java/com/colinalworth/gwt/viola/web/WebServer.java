package com.colinalworth.gwt.viola.web;

import java.io.IOException;

import rxf.server.BlobAntiPatternObject;
import rxf.server.RelaxFactoryServer;

import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class WebServer {

	public static void main(String[] args) throws Exception {
		BlobAntiPatternObject.DEBUG_SENDJSON = false;


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
