package com.colinalworth.gwt.viola.entity;

import java.util.EnumSet;

public class User extends CouchEntity {
	public static enum Permissions {
		MONITOR_AGENT, STOP_AGENT
	}

	private String identityServer;
	private String username;
	private String identityData;

	private String displayName;

	private EnumSet<Permissions> permissions = EnumSet.noneOf(Permissions.class);

}
