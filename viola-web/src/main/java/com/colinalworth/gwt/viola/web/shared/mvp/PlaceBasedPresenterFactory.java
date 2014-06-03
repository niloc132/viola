package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;

public interface PlaceBasedPresenterFactory {
	Presenter<?> getPresenterInstance(Place place);
}