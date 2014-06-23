package com.colinalworth.gwt.viola.web.client;

import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaces;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import org.junit.Test;

public class ViolaPlacesClientTest extends GWTTestCase {
	@Override
	public String getModuleName() {
		return "com.colinalworth.gwt.viola.web.Viola";
	}

    @Test(timeout = 39000L)
	public void testRouteToString() throws Exception {
		ViolaPlaces places = createPlaces();
	}

    @Test(timeout = 39000L)
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

    @Test(timeout = 39000L)
	protected ViolaPlaces createPlaces() {
		return GWT.create(ViolaPlaces.class);
	}
}
