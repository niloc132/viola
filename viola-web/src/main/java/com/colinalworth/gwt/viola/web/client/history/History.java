package com.colinalworth.gwt.viola.web.client.history;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Quick and dirty PushState api
 */
@JsType
public interface History {
	void back();
	void forward();
	void pushState(State state, String title, String url);
	void replaceState(State state, String title, String url);

	@JsProperty
	State getState();

	@JsType
	public static interface State {
		@JsProperty
		void setHistoryToken(String token);
		@JsProperty
		String getHistoryToken();
	}
	public static class StateImpl implements State {
		private String historyToken;

		public StateImpl(String historyToken) {
			this.historyToken = historyToken;
		}

		@Override
		public String getHistoryToken() {
			return historyToken;
		}

		@Override
		public void setHistoryToken(String historyToken) {
			this.historyToken = historyToken;
		}
	}

	@JsType
	public interface PopStateEvent {
		@JsProperty
		State getState();
	}

	@FunctionalInterface
	public interface PopStateEventListener {
		void onPopState(PopStateEvent event);
	}
}
