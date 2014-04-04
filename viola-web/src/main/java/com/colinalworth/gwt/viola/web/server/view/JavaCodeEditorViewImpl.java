package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter.JavaCodeEditorView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class JavaCodeEditorViewImpl extends AbstractServerView<JavaCodeEditorPresenter> implements JavaCodeEditorView {
	private String java;
	@Override
	public String getValue() {
		return java;
	}

	@Override
	public void setValue(String code) {
		this.java = code;
	}
	@Override
	public SafeHtml asSafeHtml() {
		return new SafeHtmlBuilder()
				.appendHtmlConstant("<textarea>")
				.appendEscaped(java)
				.appendHtmlConstant("</textarea>").toSafeHtml();
	}
}
