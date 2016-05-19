package com.colinalworth.gwt.viola.web.client.history;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * DOM Window object, cast a $wnd to this to let it magically work
 */
@JsType
public interface Window {
	@JsProperty
	Document getDocument();

	@JsProperty
	History getHistory();

	@JsProperty
	Location getLocation();

//	//TODO static as a workaround for failing default methods, and helper is workaround for no @JsFunction...
	static void addPopStateListener(Window window, History.PopStateEventListener listener) {
		window.addEventListener("popstate", listener::onPopState);
	}


	<T> void addEventListener(String type, BoringConsumer<T> listenerFunction);
}
