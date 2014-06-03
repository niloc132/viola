package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.viola.web.client.ioc.UserId;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorView;
import com.colinalworth.gwt.viola.web.shared.request.ProfileRequest;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ProfileEditorPresenter extends AbstractPresenterImpl<ProfileEditorView, ProfileEditorPlace> {

	public interface ProfileEditorView extends View<ProfileEditorPresenter> {
		SimpleBeanEditorDriver<UserProfile, ?> getDriver();
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

	private SimpleBeanEditorDriver<UserProfile, ?> driver;

	@Override
	public void go(AcceptsView parent, ProfileEditorPlace place) {
		//TODO fail hard if current session id != place id?
		super.go(parent, place);

		driver = getView().getDriver();

		profileServiceProvider.get().getProfile(userIdProvider.get(), new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(UserProfile result) {
				driver.edit(result);
			}
		});
	}

	public void save() {
		profileServiceProvider.get().updateProfile(driver.flush(), new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {
				//TODO pass error to ui
			}

			@Override
			public void onSuccess(UserProfile result) {
				back();
			}
		});
	}

	public void back() {
		History.back();
	}
}
