package com.colinalworth.gwt.viola.web.client;

import com.colinalworth.gwt.viola.web.client.ioc.ViolaGinjector;
import com.colinalworth.gwt.viola.web.client.mvp.ClientPlaceManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ViolaApp implements EntryPoint {
	SimpleContainer container = new SimpleContainer();

	@Override
	public void onModuleLoad() {
		Viewport vp = new Viewport();

		vp.setWidget(mainApp());

		ViolaGinjector ginjector = GWT.create(ViolaGinjector.class);

		//TODO let gin create this so it can be injected elsewhere
		ClientPlaceManager placeManager = ginjector.placeManager();
		placeManager.setContainer(new AcceptsOneWidget() {
			@Override
			public void setWidget(IsWidget w) {
				container.setWidget(w);
				container.forceLayout();
			}
		});

		ginjector.navigation().handleCurrent();

		RootPanel.get().add(vp);
	}

	private IsWidget mainApp() {
		VerticalLayoutContainer outer = new VerticalLayoutContainer();
		ToolBar toolBar = new ToolBar();
		//TODO menus/login/etc


		outer.add(toolBar, new VerticalLayoutData(1, -1));
		outer.add(container, new VerticalLayoutData(1, 1));
		return outer;
	}


}
