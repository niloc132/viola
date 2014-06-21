package com.colinalworth.gwt.viola.web.client.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.View;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

public class SimpleAcceptsView implements AcceptsView, IsWidget {
	private final SimpleContainer parent = new SimpleContainer();

	@Override
	public void setView(View view) {
		parent.unmask();
		parent.setWidget(view);
		parent.forceLayout();
	}

	public void mask() {
		parent.mask("Loading...");
	}

	@Override
	public Widget asWidget() {
		return parent;
	}
}
