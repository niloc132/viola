package com.colinalworth.gwt.viola.web.client.history;

import com.colinalworth.gwt.viola.web.client.history.HistoryChangeEvent.HistoryChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by colin on 11/21/16.
 */
public class HistoryChangeEvent extends GwtEvent<HistoryChangeHandler> {
	public static final Type<HistoryChangeHandler> TYPE = new Type<>();

	public interface HistoryChangeHandler extends EventHandler {
		void onHistoryChange(HistoryChangeEvent event);
	}

	private final String token;

	public HistoryChangeEvent(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	@Override
	public Type<HistoryChangeHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(HistoryChangeHandler historyChangeHandler) {
		historyChangeHandler.onHistoryChange(this);
	}

}
