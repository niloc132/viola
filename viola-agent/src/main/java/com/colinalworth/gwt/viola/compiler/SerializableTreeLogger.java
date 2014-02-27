package com.colinalworth.gwt.viola.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gwt.dev.util.log.AbstractTreeLogger;

public class SerializableTreeLogger extends AbstractTreeLogger {
//	private List<JsonObject> possibleBranches = new ArrayList<JsonObject>();
	private final Object mutex = new Object();
	
	private final SerializableTreeLogger parent;
	
	private final JsonObject me = new JsonObject();
	
	public SerializableTreeLogger() {
		this(null);
	}
	private SerializableTreeLogger(SerializableTreeLogger parent) {
		this.parent = parent;
		me.add("children", new JsonArray());
		me.add("log", new JsonArray());
	}
	
	public JsonObject getJsonObject() {
		return me;
	}

	private void addToParent() {
		//TODO get the order right?
		if (parent != null) {
			System.out.println(getJsonObject());
			parent.me.get("children").getAsJsonArray().add(me);
			parent.addToParent();
		}
	}

	@Override
	protected AbstractTreeLogger doBranch() {
		return new SerializableTreeLogger(this);
	}

	  @Override
	  protected void doCommitBranch(AbstractTreeLogger childBeingCommitted,
			  Type type, String msg, Throwable caught, HelpInfo helpInfo) {
		  addToParent();
		  doLog(childBeingCommitted.getBranchedIndex(), type, msg, caught, helpInfo);
	  }

	@Override
	protected void doLog(int indexOfLogEntryWithinParentLogger, Type type,
			String msg, Throwable caught, HelpInfo helpInfo) {
		synchronized (mutex) {
			JsonObject detail = new JsonObject();
			detail.addProperty("type", type.getLabel());
			detail.addProperty("message", msg);
			
			//TODO deal with caught
			//TODO deal with helpInfo
			me.get("log").getAsJsonArray().add(detail);
		}
	}


}
