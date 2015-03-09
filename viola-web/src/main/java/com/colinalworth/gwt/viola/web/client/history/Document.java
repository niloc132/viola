package com.colinalworth.gwt.viola.web.client.history;

import com.google.gwt.core.client.js.JsProperty;
import com.google.gwt.core.client.js.JsType;

/**
 * DOM Document object, i.e. $doc.
 */
@JsType
public interface Document {
	@JsProperty
	String getTitle();
}
