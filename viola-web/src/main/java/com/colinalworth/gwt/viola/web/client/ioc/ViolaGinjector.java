package com.colinalworth.gwt.viola.web.client.ioc;

import com.colinalworth.gwt.viola.web.client.mvp.ClientPlaceManager;
import com.colinalworth.gwt.viola.web.client.mvp.PushStateHistoryManager;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules(ViolaClientModule.class)
public interface ViolaGinjector extends Ginjector {
	PushStateHistoryManager navigation();
	ClientPlaceManager placeManager();

}
