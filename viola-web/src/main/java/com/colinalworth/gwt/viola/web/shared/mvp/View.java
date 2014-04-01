package com.colinalworth.gwt.viola.web.shared.mvp;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.IsWidget;
public interface View<P extends Presenter<?>> extends IsWidget {
	SafeHtml asSafeHtml();

	void setPresenter(P presenter);
}
