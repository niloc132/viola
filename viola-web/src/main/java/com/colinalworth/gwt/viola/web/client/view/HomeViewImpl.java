package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import de.barop.gwt.client.ui.HyperlinkPushState;

public class HomeViewImpl extends AbstractClientView<HomePresenter> implements HomeView {
	public HomeViewImpl() {
		FlowLayoutContainer lc = new FlowLayoutContainer();
		lc.add(new HyperlinkPushState("Create a new project", "proj/new"));
		lc.add(new HyperlinkPushState("Search for a project", "search/?q=foo"));
//		lc.add(new HyperlinkPushState("Open existing project", "proj/4307c733f10126bbd2396ef99e784c69"));

		initWidget(lc);
	}
}
