package com.colinalworth.gwt.viola.web.client.history;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * DOM Window object, cast a $wnd to this to let it magically work
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "window")
public class Window {
	@JsProperty
	public native static Window getSelf();

	@JsProperty
	public native Document getDocument();

	@JsProperty
	public native History getHistory();

	@JsProperty
	public native Location getLocation();

//	//TODO static as a workaround for failing default methods, and helper is workaround for no @JsFunction...
//	static void addPopStateListener(Window window, History.PopStateEventListener listener) {
//		window.addEventListener("popstate", listener::onPopState);
//	}


	public native <T> void addEventListener(String type, BoringConsumer<T> listenerFunction);
}
