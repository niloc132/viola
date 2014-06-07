package com.colinalworth.gwt.viola.web.client.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceBasedPresenterFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.Presenter;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.View;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

import java.util.logging.Logger;

public class ClientPlaceManager implements PlaceManager {
	private static Logger logger = Logger.getLogger(ClientPlaceManager.class.getName());

	private class ViewWrapper implements AcceptsView {
		@Override
		public void setView(View view) {
			started = true;
			getContainer().setWidget(view);
		}
	}

	private AcceptsOneWidget container;
	private boolean started = false;
	private Place current;
	private Presenter<?> active;

	@Inject
	PlaceBasedPresenterFactory presenters;
	@Inject
	PlaceFactory placeFactory;
	private HandlerManager handlerManager = new HandlerManager(this);


	public AcceptsOneWidget getContainer() {
		return container;
	}

	public void setContainer(AcceptsOneWidget container) {
		this.container = container;
	}

	@Override
	public <P extends Place> P create(Class<P> placeClass) {
		return placeFactory.create(placeClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean submit(Place place) {
		assert container != null : "Can't navigate to a place without a container set";
		if (place == null) {
			//TODO going nowhere?
//			assert false : "can't go nowhere";
			//default place
			place = create(SearchProjectPlace.class);
//			return true;
		}

		if (equals(place, current)) {
			//already there, don't do anything
			logger.finest("Already at current location, no need to do anything: " + placeFactory.route(place));
			return false;
		}

		//sanity check the place, make sure it is valid (or throws an exception because it has nulls it shouldn't have)
		placeFactory.route(place);

		Presenter presenter = presenters.getPresenterInstance(place);


		if (active != null) {
			if (active.equals(presenter)) {
				//already have the right instance, re-trigger our presence
				//this allows .equals to say "don't make a new one"

				//dont fire an event, just track the current place and re-call go()
				((Presenter) active).go(this.new ViewWrapper(), place);
				current = place;
				ValueChangeEvent.fire(this, place);
				return true;
			}

			//if existing running,
			if (started) {
				// maybe stop existing
				String msg = active.maybeStop();
				if (msg != null && !Window.confirm(msg)) {
					return false;
				}
				logger.finest("Stopping " + active);
				active.stop();
			} else {
				// else cancel existing
				logger.finest("Canceling " + active);
				active.cancel();
			}
		}

		started = false;
		presenter.go(this.new ViewWrapper(), place);
		active = presenter;
		current = place;
		ValueChangeEvent.fire(this, place);
		return true;
	}

	private boolean equals(Place place1, Place place2) {
		if (place1 == null) {
			return place2 == null;
		}
		return place2 != null && AutoBeanUtils.deepEquals(AutoBeanUtils.getAutoBean(place1), AutoBeanUtils.getAutoBean(place2));
	}

	@Override
	public Place getCurrentPlace() {
		return current;
	}

	@Override
	public void back() {
		//TODO
	}


	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Place> handler) {
		return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
	}
}
