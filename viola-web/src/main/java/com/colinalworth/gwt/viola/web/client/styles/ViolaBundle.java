package com.colinalworth.gwt.viola.web.client.styles;

import com.colinalworth.gwt.viola.web.client.view.ProjectSearchResultTemplate.Styles;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ViolaBundle extends ClientBundle {
	ViolaBundle INSTANCE = GWT.create(ViolaBundle.class);

	public interface ProjSearchResults extends Styles, CssResource {}
	@Source({"app.css", "com/sencha/gxt/theme/base/public/reset.css"})
	CssResource app();

	ProjSearchResults searchResults();
}
