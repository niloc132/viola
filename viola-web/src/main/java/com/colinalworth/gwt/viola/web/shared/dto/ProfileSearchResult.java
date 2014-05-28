package com.colinalworth.gwt.viola.web.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProfileSearchResult implements IsSerializable {
	private String _id;

	private String displayName;
	private String description;
	private String organization;

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
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
