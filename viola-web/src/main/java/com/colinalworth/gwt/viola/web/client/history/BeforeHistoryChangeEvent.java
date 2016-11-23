package com.colinalworth.gwt.viola.web.client.history;

import com.colinalworth.gwt.viola.web.client.history.BeforeHistoryChangeEvent.BeforeHistoryChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by colin on 11/21/16.
 */
public class BeforeHistoryChangeEvent extends GwtEvent<BeforeHistoryChangeHandler> {
	public static final Type<BeforeHistoryChangeHandler> TYPE = new Type<>();

	public interface BeforeHistoryChangeHandler extends EventHandler {
		void onBeforeHistoryChange(BeforeHistoryChangeEvent event);
	}

	private final String token;
	private String canonicalToken;

	public BeforeHistoryChangeEvent(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public String getCanonicalToken() {
		return canonicalToken;
	}

	public void setCanonicalToken(String canonicalToken) {
		this.canonicalToken = canonicalToken;
	}

	@Override
	public Type<BeforeHistoryChangeHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(BeforeHistoryChangeHandler historyChangeHandler) {
		historyChangeHandler.onBeforeHistoryChange(this);
	}

}
