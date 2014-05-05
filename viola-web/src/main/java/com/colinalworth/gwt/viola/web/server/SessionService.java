package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.service.UserService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SessionService {
	@Inject
	UserService userService;

	private ThreadLocal<String> sessionId = new ThreadLocal<>();

	public void logout(String sessionId) {
		userService.deleteSession(sessionId);
		this.sessionId.remove();
	}

	public void setSessionId(String sessionId) {
		this.sessionId.set(sessionId);
	}

	public String getThreadLocalSessionId() {
		return sessionId.get();
	}

	public String getThreadLocalUserId(String action) {
		String id = getThreadLocalSessionId();
		if (id == null) {
			return null;
		}
		return userService.updateSession(id, action).getId();
	}
}
