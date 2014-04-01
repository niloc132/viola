package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.web.shared.mvp.Place;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager;
import com.colinalworth.gwt.viola.web.shared.mvp.Presenter;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;
import one.xio.AsioVisitor.Impl;

import java.nio.channels.SelectionKey;

public class ServerPlaceManager extends Impl implements PlaceManager {

	private SafeHtmlBuilder sb = new SafeHtmlBuilder();

	private boolean hasView = false;
	private Place current;
	private Presenter<?> active;

	@Inject
	PlaceBasedPresenterFactory presenters;
	@Inject
	PlaceFactory placeFactory;

	private SelectionKey currentKey;

	@Override
	public <P extends Place> P create(Class<P> placeClass) {
		return placeFactory.create(placeClass);
	}

	@Override
	public boolean submit(Place place) {
		Errors.$303(currentKey, placeFactory.route(place));
		return true;//all new places are unconditional
	}

	@Override
	public Place getCurrentPlace() {
		return current;
	}

	@Override
	public void back() {
		//noop, can't go back
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		//not really sure what the server needs with this
		System.out.println("fireevent called");
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Place> handler) {
		//not really sure what the server needs with this
		System.out.println("addValueChangeHandler called");
		return null;
	}
}
