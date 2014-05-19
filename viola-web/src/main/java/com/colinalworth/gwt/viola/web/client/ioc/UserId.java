package com.colinalworth.gwt.viola.web.client.ioc;

import com.google.inject.BindingAnnotation;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface UserId {

	@Singleton
	public static class UserIdProvider implements Provider<String> {
		private String currentUserId;

		public void setCurrentUserId(String currentUserId) {
			this.currentUserId = currentUserId;
		}

		@Override
		public String get() {
			return currentUserId;
		}
	}
}
