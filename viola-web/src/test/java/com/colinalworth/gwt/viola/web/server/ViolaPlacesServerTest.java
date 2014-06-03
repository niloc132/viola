package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaces;
import com.colinalworth.gwt.places.vm.PlaceFactoryModuleBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;


public class ViolaPlacesServerTest {
	@Test
	public void testRouteToString() throws Exception {
		ViolaPlaces places = createPlaces();


	}

	@Test
	public void testRouteToPlace() throws Exception {
		ViolaPlaces places = createPlaces();

		CreateProjectPlace createProjectPlace = (CreateProjectPlace) places.route("proj/new");

		ProjectEditorPlace projectEditor = (ProjectEditorPlace) places.route("proj/1234abcd");
		assert projectEditor.getId().equals("1234abcd") : projectEditor.getId();
		assert projectEditor.getActiveFile() == null || "".equals(projectEditor.getActiveFile()) : projectEditor.getActiveFile();

		projectEditor = (ProjectEditorPlace) places.route("proj/1234abcd/");
		assert projectEditor.getId().equals("1234abcd") : projectEditor.getId();
		assert projectEditor.getActiveFile() == null || "".equals(projectEditor.getActiveFile()) : projectEditor.getActiveFile();

		projectEditor = (ProjectEditorPlace) places.route("proj/1234abcd/foo/bar/File.java");
		assert projectEditor.getId().equals("1234abcd") : projectEditor.getId();
		assert projectEditor.getActiveFile().equals("foo/bar/File.java") : projectEditor.getActiveFile();

		projectEditor = (ProjectEditorPlace) places.route("proj/asdf/foo.txt?a=b");
		assert projectEditor.getId().equals("asdf") : projectEditor.getId();
		assert projectEditor.getActiveFile().equals("foo.txt") : projectEditor.getActiveFile();

		SearchProjectPlace searchResult = (SearchProjectPlace) places.route("search/project/?q=foo");
		assert searchResult.getQuery().equals("foo");
		searchResult = (SearchProjectPlace) places.route("search/project/?a=b&q=foo");
		assert searchResult.getQuery().equals("foo");
		searchResult = (SearchProjectPlace) places.route("search/project/?q=foo&gwt.codesvr=localhost:9997");
		assert searchResult.getQuery().equals("foo");
	}

	protected ViolaPlaces createPlaces() {
		Injector i = Guice.createInjector(new PlaceFactoryModuleBuilder().build(ViolaPlaces.class));
		return i.getInstance(ViolaPlaces.class);
	}

}
