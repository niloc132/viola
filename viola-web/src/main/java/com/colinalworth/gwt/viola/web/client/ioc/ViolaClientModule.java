package com.colinalworth.gwt.viola.web.client.ioc;

import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.places.shared.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.viola.web.client.ioc.Session.SessionProvider;
import com.colinalworth.gwt.viola.web.client.ioc.UserId.UserIdProvider;
import com.colinalworth.gwt.viola.web.client.mvp.ClientPlaceManager;
import com.colinalworth.gwt.viola.web.client.mvp.ErrorsClientImpl;
import com.colinalworth.gwt.viola.web.client.mvp.TitleClientImpl;
import com.colinalworth.gwt.viola.web.client.view.CreateProjectViewImpl;
import com.colinalworth.gwt.viola.web.client.view.HomeViewImpl;
import com.colinalworth.gwt.viola.web.client.view.JavaCodeEditorViewImpl;
import com.colinalworth.gwt.viola.web.client.view.ProfileEditorViewImpl;
import com.colinalworth.gwt.viola.web.client.view.ProfileViewImpl;
import com.colinalworth.gwt.viola.web.client.view.ProjectEditorViewImpl;
import com.colinalworth.gwt.viola.web.client.view.SearchProjectViewImpl;
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
import com.colinalworth.gwt.viola.web.shared.request.JobRequest;
import com.colinalworth.gwt.viola.web.shared.request.ProfileRequest;
import com.colinalworth.gwt.viola.web.shared.request.SearchRequest;
import com.colinalworth.gwt.viola.web.shared.request.SessionRequest;
import com.colinalworth.gwt.viola.web.shared.request.ViolaRequestQueue;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

public class ViolaClientModule extends AbstractGinModule {
	@Override
	protected void configure() {
		install(new GinFactoryModuleBuilder().build(PresenterFactory.class));

		bind(SimpleEventBus.class).in(Singleton.class);
		bind(EventBus.class).to(SimpleEventBus.class);
		bind(com.google.gwt.event.shared.EventBus.class).to(SimpleEventBus.class);

		bind(Presenter.Errors.class).to(ErrorsClientImpl.class);
		bind(Presenter.PageTitle.class).to(TitleClientImpl.class);

		bind(PlaceFactory.class).to(ViolaPlaces.class);

		bind(ClientPlaceManager.class).in(Singleton.class);
		bind(PlaceManager.class).to(ClientPlaceManager.class).in(Singleton.class);

		bind(PlaceBasedPresenterFactory.class).to(ViolaPlaceMapper.class);

		bind(SearchProjectView.class).to(SearchProjectViewImpl.class);
		bind(CreateProjectView.class).to(CreateProjectViewImpl.class);
		bind(ProjectEditorView.class).to(ProjectEditorViewImpl.class);
		bind(HomeView.class).to(HomeViewImpl.class);

		bind(ProfileView.class).to(ProfileViewImpl.class);
		bind(ProfileEditorView.class).to(ProfileEditorViewImpl.class);

		bind(JavaCodeEditorView.class).to(JavaCodeEditorViewImpl.class);

		bind(String.class).annotatedWith(Session.class).toProvider(SessionProvider.class);
		bind(String.class).annotatedWith(UserId.class).toProvider(UserIdProvider.class);

		bind(ViolaRequestQueue.class).toProvider(QueueProvider.class);
	}

	@Provides
	SearchRequest provideSearchRequest(ViolaRequestQueue queue) {
		return queue.search();
	}
	@Provides
	JobRequest provideJobRequest(ViolaRequestQueue queue) {
		return queue.job();
	}
	@Provides
	ProfileRequest provideProfileRequest(ViolaRequestQueue queue) {
		return queue.profile();
	}
	@Provides
	SessionRequest provideSessionRequest(ViolaRequestQueue queue) {
		return queue.session();
	}
	@Provides
	@Named("compiledServer")
	native String provideCompiledServerUrl() /*-{
		return $wnd.staticContentServer || "";
	}-*/;

}
