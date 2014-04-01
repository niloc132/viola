package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter.JavaCodeEditorView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class JavaCodeEditorViewImpl extends AbstractServerView<JavaCodeEditorPresenter> implements JavaCodeEditorView {
	@Override
	public SafeHtml asSafeHtml() {
		return SafeHtmlUtils.fromString("This is java.");  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String getValue() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setValue(String code) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
