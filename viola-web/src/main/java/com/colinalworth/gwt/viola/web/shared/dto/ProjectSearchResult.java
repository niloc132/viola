package com.colinalworth.gwt.viola.web.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProjectSearchResult implements IsSerializable {
	private String _id;
	private String _rev;

	private String authorId;


	private String title;
	private String description;

	//not actually part of the model, populated to speed up later requests
	private String latestCompiledId;


	public String getId() {
		return _id;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public String getRev() {
		return _rev;
	}

	public void setRev(String _rev) {
		this._rev = _rev;
	}

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

	public String getLatestCompiledId() {
		return latestCompiledId;
	}

	public void setLatestCompiledId(String latestCompiledId) {
		this.latestCompiledId = latestCompiledId;
	}
}
