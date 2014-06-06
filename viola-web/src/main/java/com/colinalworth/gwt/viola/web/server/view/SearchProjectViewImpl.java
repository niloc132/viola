package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.UriUtils;

import java.util.List;

public class SearchProjectViewImpl extends AbstractServerView<SearchProjectPresenter> implements SearchProjectView {
	private String query;
	private List<ProjectSearchResult> results;
	@Override
	public void reset() {
		//no-op
	}

	@Override
	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public void setResults(List<ProjectSearchResult> results) {
		this.results = results;
	}

	@Override
	public SafeHtml asSafeHtml() {
		SafeHtmlBuilder sb = new SafeHtmlBuilder()
				.appendHtmlConstant("<form method='get' action=''>")
				.appendHtmlConstant("<input name='q' value='").appendEscaped(query).appendHtmlConstant("' />")
				.appendHtmlConstant("<div>");

		if (results != null) {
			for (ProjectSearchResult project : results) {
				if (project.getLatestCompiledId() == null) {
					continue;
				}
				sb.appendHtmlConstant("<div><a href='/example/" + UriUtils.encode(project.getLatestCompiledId()) + "/'>")
						.appendEscaped(project.getTitle())
						.appendHtmlConstant("</a>")
						.appendEscaped(project.getDescription())
						.appendHtmlConstant("</div>");
			}
		}

		return sb.appendHtmlConstant("</div></form>").toSafeHtml();
	}
}
