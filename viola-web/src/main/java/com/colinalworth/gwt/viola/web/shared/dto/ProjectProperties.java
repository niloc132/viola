package com.colinalworth.gwt.viola.web.shared.dto;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface ProjectProperties extends PropertyAccess<Project> {
	@Path("_id")
	ModelKeyProvider<ProjectSearchResult> key();


}
