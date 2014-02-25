package com.colinalworth.gwt.viola.compiler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gwt.dev.util.log.AbstractTreeLogger;

public class SerializableTreeLogger extends AbstractTreeLogger {
//	private List<JsonObject> possibleBranches = new ArrayList<JsonObject>();
	private final Object mutex = new Object();
	
	private final JsonObject me = new JsonObject();
	public SerializableTreeLogger() {
		me.add("children", new JsonArray());
		me.add("log", new JsonArray());
	}
	
	public JsonObject getJsonObject() {
		return me;
	}
	

	@Override
	protected AbstractTreeLogger doBranch() {
		SerializableTreeLogger tree = new SerializableTreeLogger();
//		possibleBranches.add(tree.getJsonObject());
		me.get("children").getAsJsonArray().add(tree.getJsonObject());
		return tree;
	}

	  @Override
	  protected void doCommitBranch(AbstractTreeLogger childBeingCommitted,
			  Type type, String msg, Throwable caught, HelpInfo helpInfo) {
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
