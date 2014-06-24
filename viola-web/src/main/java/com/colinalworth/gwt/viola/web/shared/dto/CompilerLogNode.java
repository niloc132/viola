package com.colinalworth.gwt.viola.web.shared.dto;

import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sencha.gxt.data.shared.TreeStore.TreeNode;

import java.util.List;

public class CompilerLogNode implements IsSerializable, TreeNode<CompilerLogNode> {

	public static class Detail implements IsSerializable {
		private String message;
		private Level type;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Level getType() {
			return type;
		}

		public void setType(Level type) {
			this.type = type;
		}
	}
	public static enum Level {
		ERROR,
		WARN,
		INFO,
		TRACE,
		DEBUG,
		SPAM,
		ALL
	}
	private static int ID_COUNTER;
	@GwtTransient
	private final int id = ID_COUNTER++;
	private List<CompilerLogNode> children;
	private Detail entry;

	public List<CompilerLogNode> getChildren() {
		return children;
	}

	@Override
	public CompilerLogNode getData() {
		return this;
	}

	public void setChildren(List<CompilerLogNode> children) {
		this.children = children;
	}

	public Detail getEntry() {
		return entry;
	}

	public void setEntry(Detail entry) {
		this.entry = entry;
	}

	public int getId() {
		return id;
	}
}
