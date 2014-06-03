package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;

public interface Presenter<P extends Place> {
	void go(AcceptsView parent, P place);

	String maybeStop();

	void stop();

	void cancel();

}
