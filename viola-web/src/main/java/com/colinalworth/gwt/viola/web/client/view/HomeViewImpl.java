package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;

public class HomeViewImpl extends AbstractClientView<HomePresenter> implements HomeView {
	public HomeViewImpl() {
		FlowLayoutContainer lc = new FlowLayoutContainer();
		lc.add(new HTML(HomePresenter.body));

		lc.add(new Anchor("Create a new project", "/proj/new"));

		initWidget(lc);
	}
}
