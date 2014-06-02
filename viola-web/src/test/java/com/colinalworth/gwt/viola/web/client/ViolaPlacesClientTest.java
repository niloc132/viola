package com.colinalworth.gwt.viola.web.client;

import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ViolaPlaces;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import org.junit.Test;

public class ViolaPlacesClientTest extends GWTTestCase {
	@Override
	public String getModuleName() {
		return "com.colinalworth.gwt.viola.web.Viola";
	}

	@Test
	public void testRouteToString() throws Exception {
		ViolaPlaces places = GWT.create(ViolaPlaces.class);


	}

	@Test
	public void testRouteToPlace() throws Exception {
		ViolaPlaces places = GWT.create(ViolaPlaces.class);

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
	}
}
