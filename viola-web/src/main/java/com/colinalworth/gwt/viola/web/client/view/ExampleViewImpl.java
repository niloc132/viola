package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExampleView;
import com.google.gwt.user.client.ui.Frame;

public class ExampleViewImpl extends AbstractClientView<ExamplePresenter> implements ExampleView {
	private Frame frame = new Frame();
	public ExampleViewImpl() {
		initWidget(frame);
	}

	@Override
	public void loadUrl(String url) {
		frame.setUrl(url);
	}
}
