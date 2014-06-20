package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.viola.web.shared.dto.ProfileSearchResult;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProfilePresenter.SearchProfilePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProfilePresenter.SearchProfileView;
import com.colinalworth.gwt.viola.web.shared.request.SearchRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

public class SearchProfilePresenter extends AbstractPresenterImpl<SearchProfileView, SearchProfilePlace> {

	public interface SearchProfileView extends View<SearchProfilePresenter> {
		void reset();
		void setQuery(String query);
		void setResults(List<ProfileSearchResult> results);
	}
	public interface SearchProfilePlace extends Place {
		String getQuery();
		void setQuery(String query);
	}

	@Inject
	Provider<SearchRequest> searchRequest;
	@Inject
	PlaceManager placeManager;

	@Override
	public void go(AcceptsView parent, SearchProfilePlace place) {
		super.go(parent, place);
		getView().reset();

		if (place.getQuery() != null && !place.getQuery().isEmpty()) {
			getView().setQuery(place.getQuery());
			searchRequest.get().searchProfiles(place.getQuery(), "", 20, new AsyncCallback<List<ProfileSearchResult>>() {
				@Override
				public void onFailure(Throwable caught) {
					getErrors().report(caught.getMessage());
				}

				@Override
				public void onSuccess(List<ProfileSearchResult> results) {
					getView().setResults(results);
				}
			});
		}
	}

	public void search(String query) {
		SearchProfilePlace next = placeManager.create(SearchProfilePlace.class);
		next.setQuery(query);
		placeManager.submit(next);
	}

	public void select(ProjectSearchResult value) {
		ProjectEditorPlace example = placeManager.create(ProjectEditorPlace.class);
		example.setId(value.getId());
		placeManager.submit(example);
	}
}
