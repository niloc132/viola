package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.viola.web.client.ioc.UserId;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfileView;
import com.colinalworth.gwt.viola.web.shared.request.ProfileRequest;
import com.colinalworth.gwt.viola.web.shared.request.SearchRequest;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

public class ProfilePresenter extends AbstractPresenterImpl<ProfileView, ProfilePlace> {

	public interface ProfileView extends View<ProfilePresenter> {
		SimpleBeanEditorDriver<UserProfile, ?> getDriver();

		void setCanEdit(boolean canEdit);

		void setCreatedProjects(List<ProjectSearchResult> projects);
	}
	public interface ProfilePlace extends Place {
		String getId();
		void setId(String id);
	}
	
	@Inject
	Provider<ProfileRequest> profileProvider;
	@Inject
	Provider<SearchRequest> searchProvider;

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

		ProfileRequest request = profileProvider.get();
		request.getProfile(place.getId(), new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(UserProfile result) {
				driver.edit(result);
			}
		});
		searchProvider.get().listProjectsByUser(place.getId(), "", 20, new AsyncCallback<List<ProjectSearchResult>>() {
			@Override
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(List<ProjectSearchResult> result) {
				getView().setCreatedProjects(result);
			}
		});
	}

	public void edit() {
		ProfileEditorPlace editPlace = placeManager.create(ProfileEditorPlace.class);
		editPlace.setId(getCurrentPlace().getId());
		placeManager.submit(editPlace);
	}

	public void select(ProjectSearchResult value) {
		ExamplePlace example = placeManager.create(ExamplePlace.class);
		example.setId(value.getLatestCompiledId());
		placeManager.submit(example);
	}

}
