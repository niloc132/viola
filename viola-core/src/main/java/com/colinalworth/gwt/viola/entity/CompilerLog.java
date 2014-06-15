package com.colinalworth.gwt.viola.entity;

import java.util.ArrayList;
import java.util.List;

public class CompilerLog extends CouchEntity {

	public static class LogNode {
		private LogDetail entry;
		private List<LogNode> children = new ArrayList<>();

		public LogNode() {
		}

		public LogNode(LogDetail detail) {
			entry = detail;
		}

		public LogDetail getEntry() {
			return entry;
		}

		public void setEntry(LogDetail entry) {
			this.entry = entry;
		}

		public List<LogNode> getChildren() {
			return children;
		}

		public void setChildren(List<LogNode> children) {
			this.children = children;
		}
	}
	public static class LogDetail {
		private LogLevel type;
		private String message;

		public LogDetail(LogLevel type, String message) {
			this.type = type;
			this.message = message;
		}

		public LogLevel getType() {
			return type;
		}

		public void setType(LogLevel type) {
			this.type = type;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
	public static enum LogLevel {
		ERROR,
		WARN,
		INFO,
		TRACE,
		DEBUG,
		SPAM,
		ALL
	}

	public CompilerLog(String id, LogNode root) {
		compiledProjectId = id;
		this.node = root;
	}

	private String compiledProjectId;
	private LogNode node;

	public String getCompiledProjectId() {
		return compiledProjectId;
	}

	public void setCompiledProjectId(String compiledProjectId) {
		this.compiledProjectId = compiledProjectId;
	}

	public LogNode getNode() {
		return node;
	}

	public void setNode(LogNode node) {
		this.node = node;
	}
}
