package com.colinalworth.gwt.viola.web.client.styles;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.theme.base.client.listview.ListViewCustomAppearance;

public class SearchResultsListViewAppearance<M> extends ListViewCustomAppearance<M> {
	public SearchResultsListViewAppearance() {
		super(".item", "over", "selected");     //TODO move this stuff out of searchResults.css, into a real cssres
	}

	@Override
	public void renderItem(SafeHtmlBuilder builder, SafeHtml content) {
		builder.appendHtmlConstant("<div class='item'>").append(content).appendHtmlConstant("</div>");
	}
}
