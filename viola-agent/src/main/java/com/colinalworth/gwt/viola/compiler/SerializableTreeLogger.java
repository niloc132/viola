package com.colinalworth.gwt.viola.compiler;

import com.colinalworth.gwt.viola.entity.CompilerLog.LogDetail;
import com.colinalworth.gwt.viola.entity.CompilerLog.LogLevel;
import com.colinalworth.gwt.viola.entity.CompilerLog.LogNode;
import com.google.gwt.dev.util.log.AbstractTreeLogger;

public class SerializableTreeLogger extends AbstractTreeLogger {
	private final Object mutex = new Object();

	private final SerializableTreeLogger parent;

	private final LogNode me = new LogNode();

	public SerializableTreeLogger() {
		this(null);
	}
	private SerializableTreeLogger(SerializableTreeLogger parent) {
		this.parent = parent;
	}

	public LogNode getModel() {
		return me;
	}

	private void addToParent() {
		//TODO get the order right?
		if (parent != null) {
			assert !parent.me.getChildren().contains(me);
			parent.me.getChildren().add(me);
		}
	}

	@Override
	protected AbstractTreeLogger doBranch() {
		return new SerializableTreeLogger(this);
	}

	@Override
	protected void doCommitBranch(AbstractTreeLogger childBeingCommitted, Type type, String msg, Throwable caught, HelpInfo helpInfo) {
		assert childBeingCommitted instanceof SerializableTreeLogger : "well that's not very useful now is it";
		SerializableTreeLogger child = (SerializableTreeLogger) childBeingCommitted;

		child.addToParent();
		child.me.setEntry(new LogDetail(LogLevel.valueOf(type.getLabel()), msg));
	}

	@Override
	protected void doLog(int indexOfLogEntryWithinParentLogger, Type type, String msg, Throwable caught, HelpInfo helpInfo) {
		synchronized (mutex) {
			LogDetail detail = new LogDetail(LogLevel.valueOf(type.getLabel()), msg);

			//TODO deal with caught
			//TODO deal with helpInfo
			//log message being added directly, no branch
			me.getChildren().add(new LogNode(detail));
		}
	}


}
