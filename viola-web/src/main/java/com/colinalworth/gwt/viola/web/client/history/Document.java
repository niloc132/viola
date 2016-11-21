package com.colinalworth.gwt.viola.web.client.history;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * DOM Document object, i.e. $doc.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Document {
	@JsProperty
	public native String getTitle();
}
