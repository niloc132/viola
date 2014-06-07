package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;

public interface ProjectSearchResultTemplate extends XTemplates {
	@XTemplate("<div class='{style.wrap}'>" +
				"<div class='{style.title}'>{project.title}</div>" +
				"<div class='{style.description}'>{project.description}</div>" +
			"</div>")
	SafeHtml renderProject(ProjectSearchResult project, Styles style);

	public interface Styles {
		String wrap();
		String title();
		String description();
	}
}
