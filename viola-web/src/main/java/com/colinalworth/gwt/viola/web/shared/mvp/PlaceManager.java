package com.colinalworth.gwt.viola.web.shared.mvp;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface PlaceManager extends HasValueChangeHandlers<Place> {
	<P extends Place> P create(Class<P> placeClass);

	boolean submit(Place place);

	Place getCurrentPlace();

	void back();

	/**
	 * Urls that exist in the app that are handled by various presenters, and the places that
	 * hold the state of those various urls. These URLs are somewhat like regular expressions -
	 * there are tokens that can be added to them that represent wildcards.
	 * <p/>
	 * The order of methods matter - earlier methods will be tested first, which allows the last
	 * method to match otherwise unmatched urls for 404s, or two methods to be ambiguous, and
	 * only resolved by order.
	 * <p/>
	 * All urls are assumed to be absolute, and no url will have a leading '/'.
	 * <p/>
	 * Each url is divided into two sections, the path, which is ordered and split on '/', and the
	 * query which is unordered but represented as key/value pairs joined by =, and split on '&'.
	 * Parametrized url sections may contain a '/' only if they are directly followed by '?'.
	 * <p/>
	 * Url variables are contained in {@literal{}}'s, and correspond to properties in the place itself.
	 */
	public interface PlaceFactory {
		@Retention(RetentionPolicy.RUNTIME)
		public @interface Route {
			String path();
			int priority();
		}

		Place route(String url);

		String route(Place place);

		<P extends Place> P create(Class<P> clazz);
	}

	/**
	 * TODO consider removing this and replacing it with wiring in the PlaceManager
	 */
	public interface PlaceBasedPresenterFactory {
		Presenter<?> getPresenterInstance(Place place);
	}
}
