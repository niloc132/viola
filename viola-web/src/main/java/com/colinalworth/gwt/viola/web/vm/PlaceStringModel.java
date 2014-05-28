package com.colinalworth.gwt.viola.web.vm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaceStringModel {
	public static abstract class PathComponent {

	}
	public static class PathVariable extends PathComponent {
		private final String varName;
		private final boolean optional;

		public PathVariable(String varName, boolean optional) {
			this.varName = varName;
			this.optional = optional;
		}

		public String getVarName() {
			return varName;
		}

		public boolean isOptional() {
			return optional;
		}
	}
	public static class PathConstant extends PathComponent {
		private final String value;

		public PathConstant(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static class QueryVariable {
		private final String key;

		private final String varName;
		private final boolean optional;

		public QueryVariable(String key, String varName, boolean optional) {
			this.key = key;
			this.varName = varName;
			this.optional = optional;
		}

		public String getKey() {
			return key;
		}

		public String getVarName() {
			return varName;
		}

		public boolean isOptional() {
			return optional;
		}
	}

	private List<PathComponent> path = new ArrayList<>();

	private Set<QueryVariable> query = new HashSet<>();

	public List<PathComponent> getPath() {
		return path;
	}

	public Set<QueryVariable> getQuery() {
		return query;
	}
}
