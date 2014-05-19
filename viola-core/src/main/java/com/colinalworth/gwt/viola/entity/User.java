package com.colinalworth.gwt.viola.entity;

import java.util.EnumSet;
import java.util.List;

public class User extends CouchEntity {
	public static enum Permissions {
		MONITOR_AGENT, STOP_AGENT
	}
	public static class Identity {
		private String data;
		private String server;
	}

	private String username;

	private String displayName;

	private String description;
	private String organization;

	//consider a list here instead
	private List<Identity> identities;
	private String identityServer;
	private String identityData;


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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}
}
