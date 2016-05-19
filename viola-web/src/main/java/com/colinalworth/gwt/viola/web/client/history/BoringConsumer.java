package com.colinalworth.gwt.viola.web.client.history;

import jsinterop.annotations.JsFunction;

/**
* Like Consumer, but no default methods, so can be a JsFunction
*/
@JsFunction
@FunctionalInterface
public interface BoringConsumer<T> {
	void accept(T data);
}
