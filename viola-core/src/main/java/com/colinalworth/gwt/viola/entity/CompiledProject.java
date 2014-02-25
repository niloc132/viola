package com.colinalworth.gwt.viola.entity;

public class CompiledProject extends CouchEntityWithAttachments {

	public enum Status {
		QUEUED,
		ACCEPTED,
		PRECOMPILING,
		COMPILING,
		LINKING,
		COMPLETE,
		FAILED,
		STUCK
	}
	
	private Status status;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	
}
