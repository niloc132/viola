package com.colinalworth.gwt.viola.entity;

public class Session {
	private String userId;

	private int sessionStarted;
	private int lastActive;
	private String lastAction;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getSessionStarted() {
		return sessionStarted;
	}

	public void setSessionStarted(int sessionStarted) {
		this.sessionStarted = sessionStarted;
	}

	public int getLastActive() {
		return lastActive;
	}

	public void setLastActive(int lastActive) {
		this.lastActive = lastActive;
	}

	public String getLastAction() {
		return lastAction;
	}

	public void setLastAction(String lastAction) {
		this.lastAction = lastAction;
	}
}
