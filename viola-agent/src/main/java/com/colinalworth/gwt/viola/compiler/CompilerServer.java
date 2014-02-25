package com.colinalworth.gwt.viola.compiler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import rxf.server.BlobAntiPatternObject;
import rxf.server.RelaxFactoryServer;
import rxf.server.guice.RxfModule;

import com.colinalworth.gwt.viola.ioc.ViolaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class CompilerServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		BlobAntiPatternObject.DEBUG_SENDJSON = true;
		
		

		Injector i = Guice.createInjector(new ViolaModule(), new AbstractModule() {
			
			@Override
			protected void configure() {
				bindConstant().annotatedWith(Names.named("hostname")).to("0.0.0.0");
				bindConstant().annotatedWith(Names.named("port")).to(8080);
				
				try {
					bind(URL[].class).annotatedWith(Names.named("gwtCompilerClasspath")).toInstance(new URL[]{
							new URL("file:///home/colin/.m2/repository/com/google/gwt/gwt-dev/2.6.0/gwt-dev-2.6.0.jar"),
							new URL("file:///home/colin/.m2/repository/com/google/gwt/gwt-user/2.6.0/gwt-user-2.6.0.jar"),
							new URL("file:///home/colin/workspaces42/rebased/viola/target/classes/")
					});
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}, new RxfModule());
		
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

		
		CouchCompiler c = i.getInstance(CouchCompiler.class);
		
		Thread.sleep(100);
		
		c.start();
		
		System.out.println("returning from main");
		
	}

}
