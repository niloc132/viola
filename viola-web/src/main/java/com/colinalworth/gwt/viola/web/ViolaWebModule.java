package com.colinalworth.gwt.viola.web;

import java.util.regex.Pattern;

import rxf.server.guice.RxfModule;
import rxf.server.web.inf.ContentRootImpl;

import com.google.inject.name.Names;

public class ViolaWebModule extends RxfModule {
	@Override
	protected void configureHttpVisitors() {
		get("^/source/.*").with(new HttpProxyImpl(Pattern.compile("/source/([a-fA-F0-9]+/[^?]+)"), "/vsourceproject/", ""));
		get("^/compiled/.*").with(new HttpProxyImpl(Pattern.compile("/compiled/([a-fA-F0-9]+/[^?]+)"), "/vcompiledproject/", ""));

		bindConstant().annotatedWith(Names.named("hostname")).to("0.0.0.0");
		bindConstant().annotatedWith(Names.named("port")).to(8000);


//		get(".*").with(ContentRootImpl.class);
	}

}
