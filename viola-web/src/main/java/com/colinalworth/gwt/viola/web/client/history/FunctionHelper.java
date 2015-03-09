package com.colinalworth.gwt.viola.web.client.history;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.function.Consumer;

/**
 * Brain-dead wiring for lambdas->functions, since GWT can't do it yet.
 */
public class FunctionHelper {
  public static native <T> JavaScriptObject func(Consumer<T> callable) /*-{
    return $entry(function(t) {
        callable.@java.util.function.Consumer::accept(Ljava/lang/Object;)(t);
    });
  }-*/;
}
