package com.colinalworth.gwt.viola.web.client.history;

import com.google.gwt.core.client.js.JsFunction;

/**
* Like Consumer, but no default methods, so can be a JsFunction
*/
@JsFunction
@FunctionalInterface
public interface BoringConsumer<T> {
	void accept(T data);
}
