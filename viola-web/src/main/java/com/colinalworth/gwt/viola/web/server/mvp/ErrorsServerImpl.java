package com.colinalworth.gwt.viola.web.server.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.Presenter.Errors;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ErrorsServerImpl implements Errors {
	private final SafeHtmlBuilder errors = new SafeHtmlBuilder();
	@Override
	public void report(String messageText) {
		report(SafeHtmlUtils.fromString(messageText == null ? "null" : messageText));
	}

	@Override
	public void report(SafeHtml messageHtml) {
		errors.appendHtmlConstant("<p class=error>").append(messageHtml).appendHtmlConstant("</p>");
	}

	public SafeHtml getErrors() {
		return errors.toSafeHtml();
	}
}
