package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.HomePresenter.HomeView;

public class HomePresenter extends AbstractPresenterImpl<HomeView, HomePlace> {

	public interface HomePlace extends Place {
	}
	public interface HomeView extends View<HomePresenter> {

	}
}
