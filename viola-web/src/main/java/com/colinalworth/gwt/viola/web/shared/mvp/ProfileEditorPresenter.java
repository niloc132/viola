package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent;
import com.colinalworth.gwt.viola.web.client.ioc.UserId;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorView;
import com.colinalworth.gwt.viola.web.shared.request.ProfileRequest;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

public class ProfileEditorPresenter extends AbstractPresenterImpl<ProfileEditorView, ProfileEditorPlace> {

	public interface ProfileEditorView extends View<ProfileEditorPresenter> {
		SimpleBeanEditorDriver<UserProfile, ?> getDriver();

		void setCompiledTodayCount(int result);
	}
	public interface ProfileEditorPlace extends Place {
		String getId();
		void setId(String id);
	}

	@Inject
	@UserId
	Provider<String> userIdProvider;

	@Inject
	Provider<ProfileRequest> profileServiceProvider;

	@Inject
	EventBus eventBus;

	@Inject
	PlaceManager placeManager;

	private SimpleBeanEditorDriver<UserProfile, ?> driver;

	@Override
	public void go(AcceptsView parent, ProfileEditorPlace place) {
		//TODO fail hard if current session id != place id?
		super.go(parent, place);

		driver = getView().getDriver();

		ProfileRequest request = profileServiceProvider.get();
		request.getProfile(userIdProvider.get(), new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(UserProfile result) {
				driver.edit(result);
			}
		});
		request.getCompileCountToday(new AsyncCallback<Integer>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(Integer result) {
				getView().setCompiledTodayCount(result);
			}
		});
	}

	public void save() {
		profileServiceProvider.get().updateProfile(driver.flush(), new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(UserProfile result) {
				back();
				eventBus.fireEvent(new ProfileUpdateEvent(result));
			}
		});
	}

	public void back() {
		placeManager.back();
	}
}
