package com.colinalworth.gwt.viola.entity;

import java.util.EnumSet;

public class User extends CouchEntity {
	public static enum Permissions {
		MONITOR_AGENT, STOP_AGENT
	}

	private String username;

	//consider a list here instead
	private String identityServer;
	private String identityData;

	private String displayName;

	private EnumSet<Permissions> permissions = EnumSet.noneOf(Permissions.class);

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getIdentityServer() {
		return identityServer;
	}

	public void setIdentityServer(String identityServer) {
		this.identityServer = identityServer;
	}

	public String getIdentityData() {
		return identityData;
	}

	public void setIdentityData(String identityData) {
		this.identityData = identityData;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
