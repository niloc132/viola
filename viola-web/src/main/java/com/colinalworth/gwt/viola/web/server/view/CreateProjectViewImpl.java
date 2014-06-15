package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class CreateProjectViewImpl extends AbstractServerView<CreateProjectPresenter> implements CreateProjectView {
	@Override
	public SafeHtml asSafeHtml() {
		return SafeHtmlUtils.fromString("Create a new project here. Please enable JavaScript to create/edit a project.");
	}

	@Override
	public void startWith(String title, String description) {
		//no-op, can't create a project without JS
	}
}
