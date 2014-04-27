package com.colinalworth.gwt.viola.web.shared.request;

public interface SessionRequest {

	void logout(String sessionId);

	void setSessionId(String sessionId);
}
