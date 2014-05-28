package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProfilePresenter.SearchProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;

public interface ViolaPlaces extends PlaceFactory {

	@Route("example/{id}/")
	ExamplePlace example();

	@Route("search/project/?q={query}")
	SearchProjectPlace searchProject();

	@Route("proj/new/")
	CreateProjectPlace createProject();

	@Route("proj/{id}/{activeFile?}/")
	ProjectEditorPlace editProject();

	@Route("profile/{id}/edit/")
	ProfileEditorPlace editProfile();

	@Route("profile/{id}/")
	ProfilePlace viewProfile();

	@Route("search/profile/?q={query}")
	SearchProfilePlace searchProfile();

	@Route("")
	HomePlace home();
}
