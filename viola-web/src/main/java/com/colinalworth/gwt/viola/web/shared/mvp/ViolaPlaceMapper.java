package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ViolaPlaceMapper implements PlaceBasedPresenterFactory {
	public interface PresenterFactory {
		SearchProjectPresenter search();
		CreateProjectPresenter createProject();
		ProjectEditorPresenter projEditor();
		ExamplePresenter example();
		HomePresenter home();

		ProfilePresenter viewProfile();
		ProfileEditorPresenter editProfile();
	}
	@Inject
	Provider<PresenterFactory> presenters;
	@Override
	public Presenter<?> getPresenterInstance(Place place) {
		if (place instanceof SearchProjectPlace) {
			return presenters.get().search();
		}
		if (place instanceof ExamplePlace) {
			return presenters.get().example();
		}
		if (place instanceof CreateProjectPlace) {
			return presenters.get().createProject();
		}
		if (place instanceof ProjectEditorPlace) {
			return presenters.get().projEditor();
		}
		if (place instanceof ProfilePlace) {
			return presenters.get().viewProfile();
		}
		if (place instanceof ProfileEditorPlace) {
			return presenters.get().editProfile();
		}
		if (place instanceof HomePlace) {
			return presenters.get().home();
		}

	   assert false : place;
	   return null;
	}
}
