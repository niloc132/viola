package com.colinalworth.gwt.viola.web.client.history;

import com.google.gwt.core.client.js.JsProperty;
import com.google.gwt.core.client.js.JsType;

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
  String href();
  @JsProperty
  String protocol();
  @JsProperty
  String host();
  @JsProperty
  String hostname();
  @JsProperty
  String port();
  @JsProperty
  String pathname();
  @JsProperty
  String search();
  @JsProperty
  String hash();
  @JsProperty
  String username();
  @JsProperty
  String password();
  @JsProperty
  String origin();
}
