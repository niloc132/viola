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
//				.appendHtmlConstant("<div>This is an experimental, not-even-alpha project. Sometimes the compiler " +
//						"is down or unavailable, the db might run out of space, and the security of the system is " +
//						"nearly non-existent. Assume (at least for now) that all posts are public, and assume that " +
//						"this site is prone to xss or other security issues.</div>")
				.appendHtmlConstant("<div><a href='/proj/new'>").appendEscaped("Create a new project").appendHtmlConstant("</a></div>")
				.appendHtmlConstant("<div><a href='/search/project/?q=foo'>").appendEscaped("Search for a project").appendHtmlConstant("</a></div>")
				.toSafeHtml();
	}
}
