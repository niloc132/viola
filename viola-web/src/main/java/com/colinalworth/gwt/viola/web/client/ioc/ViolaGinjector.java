package com.colinalworth.gwt.viola.web.client.ioc;

import com.colinalworth.gwt.viola.web.client.ViolaApp;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

@GinModules(ViolaClientModule.class)
public interface ViolaGinjector extends Ginjector {
	void inject(ViolaApp violaApp);
}
