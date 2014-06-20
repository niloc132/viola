package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface Presenter<P extends Place> {
	void go(AcceptsView parent, P place);

	String maybeStop();

	void stop();

	void cancel();

	PageTitle getTitle();
	Errors getErrors();

	public interface PageTitle {
		void set(String title);
		String get();
	}

	public interface Errors {
		void report(String messageText);
		void report(SafeHtml messageHtml);
	}


}
