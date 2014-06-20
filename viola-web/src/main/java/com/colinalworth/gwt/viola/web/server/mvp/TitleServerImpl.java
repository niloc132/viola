package com.colinalworth.gwt.viola.web.server.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.Presenter.PageTitle;

public class TitleServerImpl implements PageTitle {
	private String title;
	@Override
	public void set(String title) {
		this.title = title;
	}

	@Override
	public String get() {
		return title;
	}
}
