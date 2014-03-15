package com.colinalworth.gwt.viola.entity;

import java.util.Date;

public class AgentStatus extends CouchEntity {
	public static enum State {
		IDLE, WORKING, STUCK, SHUTTING_DOWN, STOPPED
	}
	private Date startup;
	private Date lastHeardFrom;
	private int idleTimeMillis;

	private boolean shutdownRequested;
	private State state;


	public Date getStartup() {
		return startup;
	}

	public void setStartup(Date startup) {
		this.startup = startup;
	}

	public Date getLastHeardFrom() {
		return lastHeardFrom;
	}

	public void setLastHeardFrom(Date lastHeardFrom) {
		this.lastHeardFrom = lastHeardFrom;
	}

	public int getIdleTimeMillis() {
		return idleTimeMillis;
	}

	public void setIdleTimeMillis(int idleTimeMillis) {
		this.idleTimeMillis = idleTimeMillis;
	}

	public boolean isShutdownRequested() {
		return shutdownRequested;
	}

	public void setShutdownRequested(boolean shutdownRequested) {
		this.shutdownRequested = shutdownRequested;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
}
