package com.colinalworth.gwt.viola.web.client.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.Presenter.PageTitle;
import com.google.gwt.dom.client.Document;

public class TitleClientImpl implements PageTitle {
	@Override
	public void set(String title) {
		Document.get().setTitle(title);
	}

	@Override
	public String get() {
		return Document.get().getTitle();
	}
}
