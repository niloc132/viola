package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class CreateProjectViewImpl extends AbstractServerView<CreateProjectPresenter> implements CreateProjectView {
	@Override
	public SafeHtml asSafeHtml() {
		return SafeHtmlUtils.fromString("Create a new project here");
	}
}
