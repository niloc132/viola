package com.colinalworth.gwt.viola.web.client.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.Presenter.Errors;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.widget.core.client.box.MessageBox;

public class ErrorsClientImpl implements Errors {
	@Override
	public void report(String messageText) {
		report(SafeHtmlUtils.fromString(messageText));
	}

	@Override
	public void report(SafeHtml messageHtml) {
		MessageBox error = new MessageBox(SafeHtmlUtils.fromString("An error has occurred:"), messageHtml);
//		error.setIcon(MessageBox.ICONS.error());
		error.show();
	}
}
