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
	private transient AgentStatus agent;

	private Date compiled;

	private String sourceId;
	private transient SourceProject source;

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

	public AgentStatus getAgent() {
		return agent;
	}

	public void setAgent(AgentStatus agent) {
		this.agent = agent;
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

	public SourceProject getSource() {
		return source;
	}

	public void setSource(SourceProject source) {
		this.source = source;
	}
}
