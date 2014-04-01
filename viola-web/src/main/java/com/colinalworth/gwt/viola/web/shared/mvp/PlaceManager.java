package com.colinalworth.gwt.viola.web.shared.mvp;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;

public interface PlaceManager extends HasValueChangeHandlers<Place> {
	<P extends Place> P create(Class<P> placeClass);

	boolean submit(Place place);

	Place getCurrentPlace();

	void back();

	public interface PlaceFactory {
		public @interface Route {
			String value();
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
