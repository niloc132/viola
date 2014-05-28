package com.colinalworth.gwt.viola.web.client.impl;

import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.Place;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProfilePresenter.SearchProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaces;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

/**
 * TODO Generator for client impls
 * TODO Proxy for server impls
 *
 */
public class ViolaPlaces_Impl extends AbstractPlacesImpl implements ViolaPlaces {

	interface ABF extends AutoBeanFactory {
		AutoBean<ExamplePlace> example();
		AutoBean<SearchProjectPlace> searchProject();
		AutoBean<CreateProjectPlace> createProject();
		AutoBean<ProjectEditorPlace> editProject();
		AutoBean<ProfileEditorPlace> editProfile();
		AutoBean<ProfilePlace> viewProfile();
		AutoBean<SearchProfilePlace> searchProfile();
		AutoBean<HomePlace> home();
	}

	String validateRegex = "^(?:example/(?:[a-zA-Z0-9%]*)/)|" +
			"(?:search/\\?q=(?:[a-zA-Z0-9%]*))|" +
			"(?:proj/new)|" +
			"(?:proj/([a-zA-Z0-9%]+)(?:/([a-zA-Z0-9%./]*))?)|" +
			"(?:profile/([a-zA-Z0-9%]*)/edit)|" +
			"(?:profile/([a-zA-Z0-9%]*)/)|" +
			"(?:search/profile\\?q=([a-zA-Z0-9%]))" +
			"(?:)$";
	RegExp example = RegExp.compile("^example/([a-zA-Z0-9%]*)/$");
	RegExp searchProject = RegExp.compile("^search/project\\?q=([a-zA-Z0-9%]*)$");
	RegExp createProject = RegExp.compile("^proj/new$");
	RegExp editProject = RegExp.compile("^proj/([a-zA-Z0-9%]+)/(?:([a-zA-Z0-9%./]*))?$");
	RegExp editProfile = RegExp.compile("^profile/([a-zA-Z0-9%]+)/edit$");
	RegExp viewProfile = RegExp.compile("^profile/([a-zA-Z0-9%]*)/$");
	RegExp searchProfile = RegExp.compile("^search/profile\\?q=([a-zA-Z0-9%]*)$");
	RegExp home = RegExp.compile("^$");

	public ViolaPlaces_Impl() {
		super(GWT.<ABF>create(ABF.class));
	}

	@Override
	public ExamplePlace example() {
		return create(ExamplePlace.class);
	}

	@Override
	public SearchProjectPlace searchProject() {
		return create(SearchProjectPlace.class);
	}

	@Override
	public ProfileEditorPlace editProfile() {
		return create(ProfileEditorPlace.class);
	}

	@Override
	public ProfilePlace viewProfile() {
		return create(ProfilePlace.class);
	}

	@Override
	public SearchProfilePlace searchProfile() {
		return create(SearchProfilePlace.class);
	}

	@Override
	public HomePlace home() {
		return create(HomePlace.class);
	}

	@Override
	public CreateProjectPlace createProject() {
		return create(CreateProjectPlace.class);
	}

	@Override
	public ProjectEditorPlace editProject() {
		return create(ProjectEditorPlace.class);
	}

	@Override
	protected String innerRoute(Place place) {
		if (place instanceof SearchProjectPlace) {
			String query = ((SearchProjectPlace) place).getQuery();
			return "search/project?q=" + urlEncodeOrThrow(query, "SearchProjectPlace.getQuery()") + "";
		}
		if (place instanceof ExamplePlace) {
			String id = ((ExamplePlace) place).getId();
			return "example/" + urlEncodeOrThrow(id, "ExamplePlace.getId()") + "/";
		}
		if (place instanceof CreateProjectPlace) {
			return "proj/new";
		}
		if (place instanceof ProjectEditorPlace) {
			ProjectEditorPlace projectEditorPlace = (ProjectEditorPlace) place;
			String id = projectEditorPlace.getId();
			String activeFile = projectEditorPlace.getActiveFile();
			return "proj/" + urlEncodeOrThrow(id, "ProjectEditorPlace.getId()") + "/" + urlEncodeOrDefault(activeFile);
		}
		if (place instanceof ProfileEditorPlace) {
			String id = ((ProfileEditorPlace) place).getId();
			return "profile/" + urlEncodeOrThrow(id, "ProfileEditorPlace.getId()") + "/edit";
		}
		if (place instanceof ProfilePlace) {
			String id = ((ProfilePlace) place).getId();
			return "profile/" + urlEncodeOrThrow(id, "ProfilePlace.getId()") + "/";
		}
		if (place instanceof SearchProfilePlace) {
			String query = ((SearchProfilePlace)place).getQuery();
			return "search/profile?q=" + urlEncodeOrThrow(query, "SearchProfilePlace.getQuery()");
		}
		if (place instanceof HomePlace) {
			return "";
		}
		return null;
	}

	@Override
	protected Place innerRoute(String url) {
		if (searchProject.test(url)) {
			SearchProjectPlace s = searchProject();
			MatchResult res = searchProject.exec(url);
			//TODO url decode
			s.setQuery(res.getGroup(1));
			return s;
		}
		if (example.test(url)) {
			ExamplePlace s = example();
			MatchResult res = example.exec(url);
			//TODO url decode
			s.setId(res.getGroup(1));
			return s;
		}
		if (createProject.test(url)) {
			CreateProjectPlace s = createProject();
			return s;
		}
		if (editProject.test(url)) {
			ProjectEditorPlace s = editProject();
			MatchResult res = editProject.exec(url);
			s.setId(res.getGroup(1));
			if (res.getGroupCount() > 2) {
				s.setActiveFile(res.getGroup(2));
			}
			return s;
		}
		if (editProfile.test(url)) {
			ProfileEditorPlace s = editProfile();
			MatchResult res = editProfile.exec(url);
			s.setId(res.getGroup(1));
			return s;
		}
		if (viewProfile.test(url)) {
			ProfilePlace s = viewProfile();
			MatchResult res = viewProfile.exec(url);
			s.setId(res.getGroup(1));
			return s;
		}
		if (searchProfile.test(url)) {
			SearchProfilePlace s = searchProfile();
			MatchResult res = searchProfile.exec(url);
			s.setQuery(res.getGroup(1));
			return s;
		}
		if (home.test(url)) {
			HomePlace s = home();
			return s;
		}
		return null;
	}

	@Override
	protected boolean verifyValid(String url) {
		return url.matches(validateRegex);
	}
}
