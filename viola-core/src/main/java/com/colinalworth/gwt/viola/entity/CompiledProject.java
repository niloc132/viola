package com.colinalworth.gwt.viola.entity;

import java.util.Date;

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

	private String agentId;

	private Date compiled;

	private String sourceId;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public Date getCompiled() {
		return compiled;
	}

	public void setCompiled(Date compiled) {
		this.compiled = compiled;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

}
