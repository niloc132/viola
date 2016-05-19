package com.colinalworth.gwt.viola.web.client.history;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * DOM Location object, more or less $doc.location
 */
@JsType
public interface Location {

	void assign(String newLocation);
	void replace(String newLocation);
	void reload();
	String toString();

	@JsProperty
	String getHref();
	@JsProperty
	String getProtocol();
	@JsProperty
	String getHost();
	@JsProperty
	String getHostname();
	@JsProperty
	String getPort();
	@JsProperty
	String getPathname();
	@JsProperty
	String getSearch();
	@JsProperty
	String getHash();
	@JsProperty
	String getUsername();
	@JsProperty
	String getPassword();
	@JsProperty
	String getOrigin();
}
