package com.colinalworth.gwt.viola.entity;

import java.util.List;

public class CompilerLog extends CouchEntity {
	private String compiledProjectId;
	private LogNode node;
	
	
	

	public static class LogNode {
		private List<LogNode> children;
		private LogDetail entry;
	}
	private static class LogDetail {
		private LogLevel type;
		private String message;
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
}
