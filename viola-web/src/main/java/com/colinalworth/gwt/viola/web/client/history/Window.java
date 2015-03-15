package com.colinalworth.gwt.viola.web.client.history;

import com.google.gwt.core.client.js.JsExport;
import com.google.gwt.core.client.js.JsProperty;
import com.google.gwt.core.client.js.JsType;

/**
 * DOM Window object, cast a $wnd to this to let it magically work
 */
@JsType
@JsExport
public interface Window {
	@JsProperty
	Document document();

	@JsProperty
	History history();

	@JsProperty
	Location location();

//	//TODO static as a workaround for failing default methods, and helper is workaround for no @JsFunction...
//	static void addPopStateListener(Window window, History.PopStateEventListener listener) {
//		window.addEventListener("popstate", listener::onPopState);
//	}


	<T> void addEventListener(String type, BoringConsumer<T> listenerFunction);
}
