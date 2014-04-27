package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class HomeViewImpl extends AbstractServerView<HomePresenter> implements HomeView {
	@Override
	public SafeHtml asSafeHtml() {
		return new SafeHtmlBuilder()
				.appendHtmlConstant("<a href='/proj/new'>").appendEscaped("Create a new project").appendHtmlConstant("</a>")
				.appendHtmlConstant("<a href='/search/?q=foo'>").appendEscaped("Search for a project").appendHtmlConstant("</a>")
				.toSafeHtml();
	}
}
