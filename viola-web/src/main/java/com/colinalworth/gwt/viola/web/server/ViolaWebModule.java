package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.service.UserService;
import com.colinalworth.gwt.viola.web.client.ioc.Session;
import com.colinalworth.gwt.viola.web.client.ioc.UserId;
import com.colinalworth.gwt.viola.web.server.SearchService.SearchQueries;
import com.colinalworth.gwt.viola.web.server.oauth.OAuthCallbackVisitor;
import com.colinalworth.gwt.viola.web.server.rpq.impl.RpqServerModuleBuilder;
import com.colinalworth.gwt.viola.web.server.view.CreateProjectViewImpl;
import com.colinalworth.gwt.viola.web.server.view.ExampleViewImpl;
import com.colinalworth.gwt.viola.web.server.view.HomeViewImpl;
import com.colinalworth.gwt.viola.web.server.view.JavaCodeEditorViewImpl;
import com.colinalworth.gwt.viola.web.server.view.ProfileViewImpl;
import com.colinalworth.gwt.viola.web.server.view.ProjectEditorViewImpl;
import com.colinalworth.gwt.viola.web.server.view.SearchViewImpl;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExampleView;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter.JavaCodeEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceBasedPresenterFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfileView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchPresenter.SearchView;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaceMapper;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaceMapper.PresenterFactory;
import com.colinalworth.gwt.viola.web.shared.request.ViolaRequestQueue;
import com.colinalworth.rpq.server.BatchInvoker;
import com.colinalworth.rpq.server.BatchServiceLocator;
import com.colinalworth.rpq.shared.impl.BatchRequest;
import com.colinalworth.rpq.shared.impl.BatchResponse;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import rxf.server.RequestQueueVisitor;
import rxf.server.guice.CouchModuleBuilder;
import rxf.server.guice.RxfModule;

import java.util.List;
import java.util.regex.Pattern;

public class ViolaWebModule extends RxfModule {
	@Override
	protected void configureHttpVisitors() {
		get("/source/([a-fA-F0-9]+/[^?]*)").with(new HttpProxyImpl(Pattern.compile("/source/([a-fA-F0-9]+/[^?]*)"), "/vsourceproject/", ""));
		get("/compiled/([a-fA-F0-9]+/[^?]*)").with(new HttpProxyImpl(Pattern.compile("/compiled/([a-fA-F0-9]+/[^?]*)"), "/vcompiledproject/", ""));

		post(".*/rpq").with(RequestQueueVisitor.class);
		bind(BatchServiceLocator.class).to(InjectingBatchServiceLocator.class);

		get("/static/.*").with(new ContentRootImpl("web", Pattern.compile("/static/(.*)")));

		get("/oauth2callback.*").with(OAuthCallbackVisitor.class);

		get("/.*").with(ViolaServerApp.class);

		bindConstant().annotatedWith(Names.named("hostname")).to("0.0.0.0");
		bindConstant().annotatedWith(Names.named("port")).to(8000);

		bind(PlaceFactory.class).to(ViolaPlaces_ServerImpl.class);
		bind(PlaceBasedPresenterFactory.class).to(ViolaPlaceMapper.class);
		bind(PlaceManager.class).to(ServerPlaceManager.class);
		install(new FactoryModuleBuilder().build(PresenterFactory.class));


		bind(SearchView.class).to(SearchViewImpl.class);
		bind(ExampleView.class).to(ExampleViewImpl.class);
		bind(CreateProjectView.class).to(CreateProjectViewImpl.class);
		bind(ProjectEditorView.class).to(ProjectEditorViewImpl.class);
		bind(HomeView.class).to(HomeViewImpl.class);

		bind(ProfileView.class).to(ProfileViewImpl.View.class);
		bind(ProfileEditorView.class).to(ProfileViewImpl.Edit.class);

		bind(JavaCodeEditorView.class).to(JavaCodeEditorViewImpl.class);

		install(new RpqServerModuleBuilder().build(ViolaRequestQueue.class));

		install(new CouchModuleBuilder("v").withService(SearchQueries.class).build());
	}

	@Provides
	RequestQueueVisitor provideRequestQueueVisitor(BatchServiceLocator locator, final SessionService sessionService) {
		return new RequestQueueVisitor(new BatchInvoker(locator) {
			@Override
			public List<BatchResponse> batchedRequest(List<BatchRequest> requests) {
				try {
					return super.batchedRequest(requests);
				} finally {
     				sessionService.setSessionId(null);
				}
			}
		});
	}

	//TODO hack to get client/server to play nice
	@Provides
	@Session
	String provideSessionId(SessionService sessionService) {
		return sessionService.getThreadLocalSessionId();
	}

	@Provides
	@UserId
	String provideUserId(@Session String session, UserService userService) {
		if (session == null) {
			return null;
		}
		return userService.getUserWithSession(session).getId();
	}


}
