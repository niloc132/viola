package com.colinalworth.gwt.viola.entity;

import com.google.gson.annotations.SerializedName;

public abstract class CouchEntity {
	@SerializedName("_id")
	private String id;
	@SerializedName("_rev")
	private String rev;
	
	public String getId() {
		return id;
	}
	public String getRev() {
		return rev;
	}

}
