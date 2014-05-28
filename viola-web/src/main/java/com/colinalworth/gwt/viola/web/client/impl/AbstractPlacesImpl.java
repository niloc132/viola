package com.colinalworth.gwt.viola.web.client.impl;

import com.colinalworth.gwt.viola.web.shared.mvp.Place;
import com.colinalworth.gwt.viola.web.shared.mvp.PlaceManager.PlaceFactory;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

	protected String urlEncodeOrThrow(Object param, String name) {
		if (param == null) {
			throw new NullPointerException(name);
		}
		return UriUtils.encode(String.valueOf(param));
	}
	protected String urlEncodeOrDefault(Object param) {
		if (param == null) {
			return "";
		}
		return UriUtils.encode(String.valueOf(param));
	}
	protected String urlEncodePairOrSkip(String key, Object value) {
		if (value == null) {
			return "";
		}
		return UriUtils.encode(key) + "=" + UriUtils.encode(String.valueOf(value)) + "&";
	}

	/** @see com.google.gwt.user.client.Window.Location#buildListParamMap(String) */
	protected Map<String, List<String>> buildListParamMap(String queryString) {
		Map<String, List<String>> out = new HashMap<String, List<String>>();

		if (queryString != null && queryString.length() > 1) {
			String qs = queryString.substring(1);

			for (String kvPair : qs.split("&")) {
				String[] kv = kvPair.split("=", 2);
				if (kv[0].length() == 0) {
					continue;
				}

				List<String> values = out.get(kv[0]);
				if (values == null) {
					values = new ArrayList<String>();
					out.put(kv[0], values);
				}
				//TODO jvm impl of decodeQueryString
				values.add(kv.length > 1 ? URL.decodeQueryString(kv[1]) : "");
			}
		}

		for (Map.Entry<String, List<String>> entry : out.entrySet()) {
			entry.setValue(Collections.unmodifiableList(entry.getValue()));
		}

		out = Collections.unmodifiableMap(out);

		return out;
	}

	protected abstract String innerRoute(Place place);
	protected abstract Place innerRoute(String url);
	protected boolean verifyValid(String url) {
		return true;
	}
}
