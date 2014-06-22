package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter.JavaCodeEditorView;
import com.google.gwt.dom.client.StyleInjector;
import com.sencha.gxt.widget.core.client.form.TextArea;

public class JavaCodeEditorViewImpl extends AbstractClientView<JavaCodeEditorPresenter> implements JavaCodeEditorView {
	private final TextArea textArea = new TextArea();

	public JavaCodeEditorViewImpl() {
		StyleInjector.inject(".code textarea {font-family: monospace;}");
		//noinspection GWTStyleCheck
		textArea.addStyleName("code");
		initWidget(textArea);
	}

	@Override
	public String getValue() {
		String value = textArea.getCurrentValue();
		return value == null ? "" : value;
	}

	@Override
	public void setValue(String code) {
		textArea.setValue(code);
	}
}
