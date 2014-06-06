package com.colinalworth.gwt.viola.web.client;

import com.colinalworth.gwt.places.shared.util.URL;
import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent;
import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent.ProfileUpdateHandler;
import com.colinalworth.gwt.viola.web.client.ioc.Session.SessionProvider;
import com.colinalworth.gwt.viola.web.client.ioc.UserId.UserIdProvider;
import com.colinalworth.gwt.viola.web.client.ioc.ViolaGinjector;
import com.colinalworth.gwt.viola.web.client.mvp.ClientPlaceManager;
import com.colinalworth.gwt.viola.web.client.mvp.PushStateHistoryManager;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfilePlace;
import com.colinalworth.gwt.viola.web.shared.request.ProfileRequest;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
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
import de.barop.gwt.client.ui.HyperlinkPushState;

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

	@Inject
	UserIdProvider userIdManager;

	@Inject
	Provider<ProfileRequest> profileRequestProvider;

	@Inject
	EventBus eventBus;

	private String userId;//hacky way to track, but this class hopefully won't grow much futher


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

		eventBus.addHandler(ProfileUpdateEvent.TYPE, new ProfileUpdateHandler() {
			@Override
			public void onProfileUpdate(ProfileUpdateEvent event) {
				String displayName = event.getUserProfile().getDisplayName();
				userbtn.setText((displayName == null || displayName.isEmpty()) ? event.getUserProfile().getId() : displayName);
				userbtn.show();
				((ToolBar) userbtn.getParent()).forceLayout();
			}
		});

		navigation.handleCurrent();

		RootPanel.get().add(vp);
	}

	private IsWidget mainApp() {
		VerticalLayoutContainer outer = new VerticalLayoutContainer();
		ToolBar toolBar = new ToolBar();
		toolBar.add(new HyperlinkPushState("Viola: a fiddle for GWT", ""));

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
				ProfileEditorPlace updateProfile = placeManager.create(ProfileEditorPlace.class);
				updateProfile.setId(userId);
				placeManager.submit(updateProfile);
			}
		}));
		userbtn.getMenu().add(new MenuItem("My Projects", new SelectionHandler<MenuItem>() {
			@Override
			public void onSelection(SelectionEvent<MenuItem> event) {
				ProfilePlace profilePlace = placeManager.create(ProfilePlace.class);
				profilePlace.setId(userId);
				placeManager.submit(profilePlace);
			}
		}));
		userbtn.getMenu().add(new SeparatorMenuItem());
		userbtn.getMenu().add(new MenuItem("Log out", new SelectionHandler<MenuItem>() {
			@Override
			public void onSelection(SelectionEvent<MenuItem> event) {
				setSessionId(null, null, false);
			}
		}));
		userbtn.hide();
		toolBar.add(userbtn);


		outer.add(toolBar, new VerticalLayoutData(1, -1));
		outer.add(container, new VerticalLayoutData(1, 1));
		return outer;
	}

	private void loginSequence() {
		exportAuthSuccess();
		String redirect = URL.encodeQueryString(Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/oauth2callback");
		String clientId = URL.encodeQueryString("888496828889-cjuie9aotun74v1p9tbrb568rchtjkc9.apps.googleusercontent.com");
		String state = URL.encodeQueryString("foobarbaz");//TODO real state
		Window.open("https://accounts.google.com/o/oauth2/auth?scope=openid&response_type=code&redirect_uri=" + redirect + "&client_id=" + clientId + "&state=" + state + "&hl=en&from_login=1", "oauth", "");
	}

	private void setSessionId(String sessionId, String userId, boolean newUser) {
		sessionManager.setCurrentSessionId(sessionId);
		userIdManager.setCurrentUserId(userId);
		if (sessionId == null) {
			loginbtn.show();
			userbtn.hide();
			((ToolBar) loginbtn.getParent()).forceLayout();
			ViolaApp.this.userId = null;
		} else {
			loginbtn.hide();

			if (newUser) {
				ProfileEditorPlace updateProfile = placeManager.create(ProfileEditorPlace.class);
				updateProfile.setId(userId);
				placeManager.submit(updateProfile);
			}
			ViolaApp.this.userId = userId;
			profileRequestProvider.get().getProfile(userId, new AsyncCallback<UserProfile>() {
				@Override
				public void onFailure(Throwable caught) {
					//error message...?
				}

				@Override
				public void onSuccess(UserProfile result) {
					eventBus.fireEvent(new ProfileUpdateEvent(result));
				}
			});
		}
	}

	private native void exportAuthSuccess() /*-{
		var that = this;
		if (!$wnd.authSuccess) {
			$wnd.authSuccess = $entry(function(sessionId, userId, newUser) {
				that.@com.colinalworth.gwt.viola.web.client.ViolaApp::setSessionId(Ljava/lang/String;Ljava/lang/String;Z)(sessionId, userId, newUser);
				$wnd.authSuccess = null;
			});
		}
	}-*/;
}
