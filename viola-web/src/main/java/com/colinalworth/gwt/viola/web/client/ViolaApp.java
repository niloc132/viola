package com.colinalworth.gwt.viola.web.client;

import com.colinalworth.gwt.viola.web.client.ioc.Session.SessionProvider;
import com.colinalworth.gwt.viola.web.client.ioc.ViolaGinjector;
import com.colinalworth.gwt.viola.web.client.mvp.ClientPlaceManager;
import com.colinalworth.gwt.viola.web.client.mvp.PushStateHistoryManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ViolaApp implements EntryPoint {
	private SimpleContainer container = new SimpleContainer();
	private TextButton loginbtn;
	private TextButton userbtn;

	@Inject
	ClientPlaceManager placeManager;

	@Inject
	PushStateHistoryManager navigation;

	@Inject
	SessionProvider sessionManager;



	@Override
	public void onModuleLoad() {
		Viewport vp = new Viewport();

		vp.setWidget(mainApp());

		ViolaGinjector ginjector = GWT.create(ViolaGinjector.class);

		//TODO let gin create this so it can be injected elsewhere
		ginjector.inject(this);
		placeManager.setContainer(new AcceptsOneWidget() {
			@Override
			public void setWidget(IsWidget w) {
				container.setWidget(w);
				container.forceLayout();
			}
		});

		navigation.handleCurrent();

		RootPanel.get().add(vp);
	}

	private IsWidget mainApp() {
		VerticalLayoutContainer outer = new VerticalLayoutContainer();
		ToolBar toolBar = new ToolBar();
		toolBar.add(new Label("Viola: a fiddler for GWT"));

		toolBar.add(new FillToolItem());

		loginbtn = new TextButton("Login", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				loginSequence();
			}
		});
		toolBar.add(loginbtn);
		userbtn = new TextButton("");
		userbtn.setMenu(new Menu());
		userbtn.getMenu().add(new MenuItem("Profile", new SelectionHandler<MenuItem>() {
			@Override
			public void onSelection(SelectionEvent<MenuItem> event) {
				Window.alert("not implemented");
			}
		}));
		userbtn.getMenu().add(new MenuItem("My Projects", new SelectionHandler<MenuItem>() {
			@Override
			public void onSelection(SelectionEvent<MenuItem> event) {
				Window.alert("not implemented");
			}
		}));
		userbtn.getMenu().add(new SeparatorMenuItem());
		userbtn.getMenu().add(new MenuItem("Log out", new SelectionHandler<MenuItem>() {
			@Override
			public void onSelection(SelectionEvent<MenuItem> event) {
				setSessionId(null, null);
			}
		}));
		userbtn.hide();
		toolBar.add(userbtn);


		//TODO menus/login/etc


		outer.add(toolBar, new VerticalLayoutData(1, -1));
		outer.add(container, new VerticalLayoutData(1, 1));
		return outer;
	}

	private void loginSequence() {
		exportAuthSuccess();
		Window.open("https://accounts.google.com/o/oauth2/auth?scope=openid&response_type=code&redirect_uri=http://viola.colinalworth.com/oauth2callback&client_id=888496828889-fjt0sjb686vkl3mesrs03tji682rinbg.apps.googleusercontent.com&hl=en&from_login=1&approval_prompt=force", "oauth", "");
	}

	private void setSessionId(String sessionId, String username) {
		sessionManager.setCurrentSessionId(sessionId);
		if (sessionId == null) {
			loginbtn.show();
			userbtn.hide();
		} else {
			loginbtn.hide();
			userbtn.setText(username);
			userbtn.show();
		}
		((ToolBar) loginbtn.getParent()).forceLayout();
	}

	private native void exportAuthSuccess() /*-{
		var that = this;
		if (!$wnd.authSuccess) {
			$wnd.authSuccess = $entry(function(sessionId, username) {
				that.@com.colinalworth.gwt.viola.web.client.ViolaApp::setSessionId(Ljava/lang/String;Ljava/lang/String;)(sessionId, username);
			});
		}
	}-*/;
}
