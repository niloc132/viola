package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceBasedPresenterFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchPresenter.SearchPlace;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ViolaPlaceMapper implements PlaceBasedPresenterFactory {
	public interface PresenterFactory {
		SearchPresenter search();
		CreateProjectPresenter createProject();
		ProjectEditorPresenter projEditor();
		ExamplePresenter example();
		HomePresenter home();
	}
	@Inject
	Provider<PresenterFactory> presenters;
	@Override
	public Presenter<?> getPresenterInstance(Place place) {
		if (place instanceof SearchPlace) {
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
		if (place instanceof HomePlace) {
			return presenters.get().home();
		}

	   assert false : place;
	   return null;
	}
}
