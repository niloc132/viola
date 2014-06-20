package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.places.vm.PlaceFactoryModuleBuilder;
import com.colinalworth.gwt.viola.service.UserService;
import com.colinalworth.gwt.viola.web.client.ioc.Session;
import com.colinalworth.gwt.viola.web.client.ioc.UserId;
import com.colinalworth.gwt.viola.web.server.SearchService.SearchQueries;
import com.colinalworth.gwt.viola.web.server.mvp.ErrorsServerImpl;
import com.colinalworth.gwt.viola.web.server.mvp.ServerPlaceManager;
import com.colinalworth.gwt.viola.web.server.mvp.TitleServerImpl;
import com.colinalworth.gwt.viola.web.server.mvp.ViolaServerApp;
import com.colinalworth.gwt.viola.web.server.oauth.OAuthCallbackVisitor;
import com.colinalworth.gwt.viola.web.server.rpq.impl.RpqServerModuleBuilder;
import com.colinalworth.gwt.viola.web.server.view.CreateProjectViewImpl;
import com.colinalworth.gwt.viola.web.server.view.HomeViewImpl;
import com.colinalworth.gwt.viola.web.server.view.JavaCodeEditorViewImpl;
import com.colinalworth.gwt.viola.web.server.view.ProfileViewImpl;
import com.colinalworth.gwt.viola.web.server.view.ProjectEditorViewImpl;
import com.colinalworth.gwt.viola.web.server.view.SearchProjectViewImpl;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter.JavaCodeEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceBasedPresenterFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.Presenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfileView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectView;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaceMapper;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaceMapper.PresenterFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaces;
import com.colinalworth.gwt.viola.web.shared.request.ViolaRequestQueue;
import com.colinalworth.rpq.server.BatchInvoker;
import com.colinalworth.rpq.server.BatchServiceLocator;
import com.colinalworth.rpq.shared.impl.BatchRequest;
import com.colinalworth.rpq.shared.impl.BatchResponse;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import rxf.server.RequestQueueVisitor;
import rxf.server.driver.RxfBootstrap;
import rxf.server.guice.CouchModuleBuilder;
import rxf.server.guice.RxfModule;

import java.util.List;
import java.util.regex.Pattern;

public class ViolaWebModule extends RxfModule {
	@Override
	protected void configureHttpVisitors() {
//		get("/source/([a-fA-F0-9]+/[^?]*)").with(new HttpProxyImpl(Pattern.compile("/source/([a-fA-F0-9]+/[^?]*)"), "/vsourceproject/", ""));
		get("/compiled/([a-fA-F0-9]+/[^?]*)").with(new HttpProxyImpl(Pattern.compile("/compiled/([a-fA-F0-9]+/[^?]*)"), "/vcompiledproject/", ""));

		post(".*/rpq").with(RequestQueueVisitor.class);
		bind(BatchServiceLocator.class).to(InjectingBatchServiceLocator.class);

		get("/static/.*").with(new ContentRootImpl("web", Pattern.compile("/static/(.*)")));

		get("/oauth2callback.*").with(OAuthCallbackVisitor.class);

		get("/.*").with(ViolaServerApp.class);

		bindConstant().annotatedWith(Names.named("hostname")).to("0.0.0.0");
		bindConstant().annotatedWith(Names.named("port")).to(8000);

		bindConstant().annotatedWith(Names.named("compiledServer")).to(RxfBootstrap.getVar("static.url", RxfBootstrap.getVar("url", "https://viola.colinalworth.com")));

		bind(Presenter.Errors.class).to(ErrorsServerImpl.class);
		bind(Presenter.PageTitle.class).to(TitleServerImpl.class);

		install(new PlaceFactoryModuleBuilder().build(ViolaPlaces.class));
		bind(PlaceBasedPresenterFactory.class).to(ViolaPlaceMapper.class);
		bind(PlaceManager.class).to(ServerPlaceManager.class);
		install(new FactoryModuleBuilder().build(PresenterFactory.class));

		//not singleton, we want this garbage collected, since nothing fun will happen
		//consider binding a dummy instead of a real impl
//		bind(SimpleEventBus.class);
		bind(EventBus.class).to(SimpleEventBus.class);
		bind(com.google.web.bindery.event.shared.EventBus.class).to(SimpleEventBus.class);

		bind(SearchProjectView.class).to(SearchProjectViewImpl.class);
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
