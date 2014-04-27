package com.colinalworth.gwt.viola.web.client.ioc;

import com.google.inject.BindingAnnotation;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface Session {

	@Singleton
	public static class SessionProvider implements Provider<String> {
		private String currentSessionId;

		public void setCurrentSessionId(String currentSessionId) {
			this.currentSessionId = currentSessionId;
		}

		@Override
		public String get() {
			return currentSessionId;
		}
	}
}
