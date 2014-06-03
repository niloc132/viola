package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExampleView;

public class ExamplePresenter extends AbstractPresenterImpl<ExampleView, ExamplePlace> implements Presenter<ExamplePlace> {
	public interface ExamplePlace extends Place {
		String getId();
		void setId(String id);
	}
	public interface ExampleView extends View<ExamplePresenter> {
		void loadUrl(String url);
	}

	@Override
	public void go(AcceptsView parent, ExamplePlace place) {
		super.go(parent, place);
		getView().loadUrl("/compiled/" + place.getId() + "/");
	}
}
