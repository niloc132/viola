package com.colinalworth.gwt.viola.web.shared.mvp;

public interface Presenter<P extends Place> {
	void go(AcceptsView parent, P place);

	String maybeStop();

	void stop();

	void cancel();

}
