package com.colinalworth.gwt.viola.web.client.history;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * DOM Document object, i.e. $doc.
 */
@JsType
public interface Document {
	@JsProperty
	String getTitle();
}
