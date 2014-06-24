package com.colinalworth.gwt.viola.entity;

import java.util.Date;

public class SourceProject extends CouchEntityWithAttachments {
	private String authorId;

	private String title;
	private String description;
	
	private Date lastUpdated;
	private Date expires;
	
	private String module;
	
	private String previousVersionSource;

	private boolean isPrivate;


	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public Date getExpires() {
		return expires;
	}
	public void setExpires(Date expires) {
		this.expires = expires;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getPreviousVersionSource() {
		return previousVersionSource;
	}
	public void setPreviousVersionSource(String previousVersionSource) {
		this.previousVersionSource = previousVersionSource;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean aPrivate) {
		isPrivate = aPrivate;
	}
}