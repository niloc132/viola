package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import de.barop.gwt.client.ui.HyperlinkPushState;

public class HomeViewImpl extends AbstractClientView<HomePresenter> implements HomeView {
	public HomeViewImpl() {
		FlowLayoutContainer lc = new FlowLayoutContainer();
		lc.add(new Label("This is an experimental, not-even-alpha project. Sometimes the compiler " +
				"is down or unavailable, the db might run out of space, and the security of the system is " +
				"nearly non-existent. Assume (at least for now) that all posts are public, and assume that " +
				"this site is prone to xss or other security issues."));
		lc.add(new HyperlinkPushState("Create a new project", "proj/new"));

		initWidget(lc);
	}
}
