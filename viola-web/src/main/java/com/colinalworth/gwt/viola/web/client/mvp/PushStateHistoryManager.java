package com.colinalworth.gwt.viola.web.client.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.places.shared.PlaceManager.PlaceFactory;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;

public class PushStateHistoryManager {
	@Inject
	PlaceFactory factory;

	private boolean changingValue;

	@Inject
	public void register(final PlaceManager placeManager) {
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				boolean valid = placeManager.submit(factory.route(event.getValue()));
				if (!valid) {
					History.back();
				}
			}
		});

		placeManager.addValueChangeHandler(new ValueChangeHandler<Place>() {
			@Override
			public void onValueChange(ValueChangeEvent<Place> event) {
				String url = factory.route(event.getValue());
				if (url != null) {
					History.newItem(url, false);
				}
			}
		});
	}

	public void handleCurrent() {
		History.fireCurrentHistoryState();
	}
}
