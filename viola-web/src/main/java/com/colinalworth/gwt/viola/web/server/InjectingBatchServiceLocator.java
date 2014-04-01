package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.rpq.server.BatchServiceLocator;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class InjectingBatchServiceLocator extends BatchServiceLocator {
	@Inject Injector injector;

	@Override
	public Object getServiceInstance(Class<?> clazz) {
		return injector.getInstance(clazz);
	}
}