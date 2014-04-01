package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExampleView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ExampleViewImpl extends AbstractServerView<ExamplePresenter> implements ExampleView {
	private String url;
	@Override
	public void loadUrl(String url) {
		this.url = url;
	}

	@Override
	public SafeHtml asSafeHtml() {
		return new SafeHtmlBuilder()
				.appendHtmlConstant("<iframe width='100%' height='100%' src='").appendEscaped(url).appendHtmlConstant("' />").toSafeHtml();
	}
}
