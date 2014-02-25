package com.colinalworth.gwt.viola.entity;

import java.util.Date;
import java.util.List;

public class SourceProject extends CouchEntityWithAttachments {
	private String compiledId;
	private transient CompiledProject compiled;
	
	private String authorId;
	private transient User author;
	
	private String title;
	private String description;
	
	private Date lastUpdated;
	private Date expires;
	
	private String module;
	
	private String previousVersionSource;

	
	public String getCompiledId() {
		return compiledId;
	}
	public void setCompiledId(String compiledId) {
		this.compiledId = compiledId;
	}
	public CompiledProject getCompiled() {
		return compiled;
	}
	public void setCompiled(CompiledProject compiled) {
		this.compiled = compiled;
	}
	public String getAuthorId() {
		return authorId;
	}
	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}
	public User getAuthor() {
		return author;
	}
	public void setAuthor(User author) {
		this.author = author;
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
	
}