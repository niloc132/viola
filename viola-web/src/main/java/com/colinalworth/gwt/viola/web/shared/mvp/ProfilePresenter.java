package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.client.ioc.UserId;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfileView;
import com.colinalworth.gwt.viola.web.shared.request.ProfileRequest;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ProfilePresenter extends AbstractPresenterImpl<ProfileView, ProfilePlace> {

	public interface ProfileView extends View<ProfilePresenter> {
		SimpleBeanEditorDriver<UserProfile, ?> getDriver();

		void setCanEdit(boolean canEdit);
	}
	public interface ProfilePlace extends Place {
		String getId();
		void setId(String id);
	}
	
	@Inject
	Provider<ProfileRequest> profileProvider;
	
	@Inject
	PlaceManager placeManager;
	
	@Inject
	@UserId
	Provider<String> userIdProvider;

	private SimpleBeanEditorDriver<UserProfile, ?> driver;
	@Override
	public void go(AcceptsView parent, ProfilePlace place) {
		super.go(parent, place);
		
		driver = getView().getDriver();
		
		boolean canEdit = place.getId().equals(userIdProvider.get());
		getView().setCanEdit(canEdit);
		
		profileProvider.get().getProfile(place.getId(), new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {
				
			}

			@Override
			public void onSuccess(UserProfile result) {
				driver.edit(result);
			}
		});
	}

	public void edit() {
		ProfileEditorPlace editPlace = placeManager.create(ProfileEditorPlace.class);
		editPlace.setId(getCurrentPlace().getId());
		placeManager.submit(editPlace);
	}
}
