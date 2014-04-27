package com.colinalworth.gwt.viola.web.client.impl;

import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceFactory;
import com.colinalworth.gwt.viola.web.shared.mvp.Place;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;


public abstract class AbstractPlacesImpl implements PlaceFactory {
	protected final AutoBeanFactory factory;

	protected AbstractPlacesImpl(AutoBeanFactory factory) {
		this.factory = factory;
	}

	@Override
	public Place route(String url) {
		verifyValid(url);

		Place place = innerRoute(url);
		if (place == null) {
			return null;
		}
		AutoBeanUtils.getAutoBean(place).setFrozen(true);
		return place;
	}


	@Override
	public String route(Place place) {
		assert place != null : "Can't route to a null place";
		AutoBeanUtils.getAutoBean(place).setFrozen(true);

		String url = innerRoute(place);
		assert url != null : "Unsupported place type " + place.getClass();
		assert verifyValid(url) : "Generated url is invalid";
		return url;
	}

	@Override
	public <P extends Place> P create(Class<P> clazz) {
		return factory.create(clazz).as();
	}

	protected abstract String innerRoute(Place place);
	protected abstract Place innerRoute(String url);
	protected abstract boolean verifyValid(String url);
}
