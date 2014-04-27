package com.colinalworth.gwt.viola.entity;

import com.google.gson.JsonElement;

import java.util.Date;

public class AgentStatus extends CouchEntity {
	public static enum State {
		/** Agent has been requested by AgentManager, but has not yet connected */
		STARTING,
		/** Agent appears to be connected, but is not currently busy */
		IDLE,
		/** Agent is connected and busy */
		WORKING,
		/** Agent has not been heard from in a while, may be dead or unable to communicate */
		STUCK,
		/** Agent has received request to halt, but is still finishing work */
		SHUTTING_DOWN,
		/** Agent has stopped, but box may still be running, responsibility is back to AgentManager */
		STOPPED,
		/** Agent has been fully cleaned up, and this entity only is a marker that it used to be running */
		COMPLETE
	}
	private Date startup;
	private Date lastHeardFrom;
	private int idleTimeMillis;

	private boolean shutdownRequested;
	private State state;

	private String serverVersion;
	private String serverType;
	private JsonElement serverData;


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

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public JsonElement getServerData() {
		return serverData;
	}

	public void setServerData(JsonElement serverData) {
		this.serverData = serverData;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}
}
