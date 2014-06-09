package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class HomePresenter extends AbstractPresenterImpl<HomeView, HomePlace> {
	public static SafeHtml body = SafeHtmlUtils.fromSafeConstant("<div class='plain'><p>Fiddle around in GWT! Try out a use case, demonstrate a bug, check simple performance numbers with Viola.</p>" +
			"<p>This is an experimental, not-even-alpha project. Sometimes the compiler is down or unavailable, the db might run out of space, and the security of the system is a work in progress. </p>" +
			"<p>Current short term TODO list:" +
			"<ul>" +
			"<li>Add support for common GWT widget libraries (GXT, GQuery, mgwt, etc - please suggest more!)</li>" +
			"<li>Make it easier to edit projects you own, and fork projects you don't</li>" +
			"<li>Full text search (currently prefix search)</li>" +
			"<li>List metadata on example pages (author, title, description)</li>" +
			"<li>Show log while compiling</li>" +
//			"<li></li>" +
			"</ul></p>" +
			"<p>Longer term changes:" +
			"<ul>" +
			"<li>Support multiple versions of GWT and libraries</li>" +
			"<li>Dramatically improve IDE</li>" +
			"<li>Support downloading project sources in standard formats (maven, eclipse, intellij)</li>" +
//			"<li></li>" +
			"</ul></p></div>");

	public interface HomePlace extends Place {
	}
	public interface HomeView extends View<HomePresenter> {

	}
}
