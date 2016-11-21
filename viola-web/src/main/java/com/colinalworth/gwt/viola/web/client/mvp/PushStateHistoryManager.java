package com.colinalworth.gwt.viola.web.client.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.places.shared.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.viola.web.client.history.HistoryImpl;
import com.google.inject.Inject;

public class PushStateHistoryManager {
	@Inject
	PlaceFactory factory;

	@Inject
	HistoryImpl history;

	private boolean changingValue;

	@Inject
	public void register(final PlaceManager placeManager) {
		history.addBeforeHistoryChangeHandler(event -> {
			Place route = factory.route(event.getToken());
			//make sure we've got the right string
			String remapped = factory.route(route);
			if (!event.getToken().equals(remapped)) {
				event.setCanonicalToken(remapped);
			}

		});
		history.addHistoryChangeHandler(event -> {
			Place route = factory.route(event.getToken());
			assert factory.route(route).equals(event.getToken());

			boolean valid = placeManager.submit(route);
//			if (!valid) {
//				history.back();
//			}
		});

		placeManager.addValueChangeHandler(event -> {
			String url = factory.route(event.getValue());
			if (url != null) {
				history.newItem(url, false);
			}
		});
	}

	public void handleCurrent() {
		history.fireCurrentHistoryState();
	}
}
