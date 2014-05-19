package com.colinalworth.gwt.viola.web.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserProfile implements IsSerializable {
	private String _id;

	private String username;
	private String displayName;
	private String description;
	private String organization;

	public void setId(String id) {
		this._id = id;
	}

	public String getId() {
		return _id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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
