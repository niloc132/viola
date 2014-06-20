package com.colinalworth.gwt.viola.web.client;

import com.colinalworth.gwt.places.shared.util.URL;
import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent;
import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent.ProfileUpdateHandler;
import com.colinalworth.gwt.viola.web.client.ioc.Session.SessionProvider;
import com.colinalworth.gwt.viola.web.client.ioc.UserId.UserIdProvider;
import com.colinalworth.gwt.viola.web.client.ioc.ViolaGinjector;
import com.colinalworth.gwt.viola.web.client.mvp.ClientPlaceManager;
import com.colinalworth.gwt.viola.web.client.mvp.PushStateHistoryManager;
import com.colinalworth.gwt.viola.web.client.styles.ViolaBundle;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.Presenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;
import com.colinalworth.gwt.viola.web.shared.request.ProfileRequest;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
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
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.menu.SeparatorMenuItem;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import de.barop.gwt.client.ui.HyperlinkPushState;

import java.util.logging.Level;
import java.util.logging.Logger;

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

	@Inject
	Presenter.Errors errors;

	private String userId;//hacky way to track, but this class hopefully won't grow much futher


	@Override
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			Logger logger = Logger.getLogger("Uncaught Exception");
			@Override
			public void onUncaughtException(Throwable throwable) {
				logger.log(Level.SEVERE, "Uncaught Exception", throwable);
			}
		});

		try {
			if (Storage.isLocalStorageSupported()) {
				String identityServer = Storage.getLocalStorageIfSupported().getItem("identityServer");
				if (identityServer != null) {
					hiddenLogin(identityServer);
				}
			}

			ViolaBundle.INSTANCE.app().ensureInjected();

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
		} catch (Exception ex) {
			GWT.reportUncaughtException(ex);
		}
	}

	private IsWidget mainApp() {
		VerticalLayoutContainer outer = new VerticalLayoutContainer();
		ToolBar toolBar = new ToolBar();
		toolBar.add(new HyperlinkPushState("Viola: a fiddle for GWT", ""));

		toolBar.add(new FillToolItem());

		TextField search = new TextField();
		search.setEmptyText("Project Search...");
//		search.getElement().child("input").setAttribute("type", "search");//breaks with chrome clear btn
		search.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
				String value = stringValueChangeEvent.getValue();
				if (value == null || value.equals("")) {
					return;
				}
				SearchProjectPlace next = placeManager.create(SearchProjectPlace.class);
				next.setQuery(value);
				placeManager.submit(next);
			}
		});
		toolBar.add(search);

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
				setSessionId(null, null, false, null);
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
		//TODO support other servers
		Window.open(getOAuthUrl("accounts.google.com"), "oauth", "");
	}
	private void hiddenLogin(String identityServer) {
		final IFrameElement hiddenIframe = Document.get().createIFrameElement();
		hiddenIframe.setSrc(getOAuthUrl(identityServer));
		hiddenIframe.getStyle().setDisplay(Display.NONE);

		exportAuthSuccess();

		Document.get().getBody().appendChild(hiddenIframe);

		new Timer() {
			@Override
			public void run() {
				hiddenIframe.removeFromParent();
			}
		}.schedule(2000);
	}

	private String getOAuthUrl(String identityServer) {
		String redirect = URL.encodeQueryString(Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/oauth2callback");
		String clientId = URL.encodeQueryString("888496828889-cjuie9aotun74v1p9tbrb568rchtjkc9.apps.googleusercontent.com");
		String state = URL.encodeQueryString("foobarbaz" + "-" + identityServer);//TODO real state
		//TODO pick a url based on the idServer arg
		return "https://accounts.google.com/o/oauth2/auth?scope=openid&response_type=code&redirect_uri=" + redirect + "&client_id=" + clientId + "&state=" + state + "&hl=en&from_login=1";
	}

	private void setSessionId(String sessionId, String userId, boolean newUser, String identityServer) {
		sessionManager.setCurrentSessionId(sessionId);
		userIdManager.setCurrentUserId(userId);
		if (Storage.isLocalStorageSupported()) {
			if (identityServer == null) {
				Storage.getLocalStorageIfSupported().removeItem("identityServer");
			} else {
				Storage.getLocalStorageIfSupported().setItem("identityServer", identityServer);
			}
		}
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
					errors.report(caught.getMessage());
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
			$wnd.authSuccess = $entry(function(sessionId, userId, newUser, identityServer) {
				that.@com.colinalworth.gwt.viola.web.client.ViolaApp::setSessionId(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)(sessionId, userId, newUser, identityServer);
//				$wnd.authSuccess = null;
			});
		}
	}-*/;
}
