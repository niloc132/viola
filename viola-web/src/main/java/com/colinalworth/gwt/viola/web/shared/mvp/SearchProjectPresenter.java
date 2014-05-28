package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchProjectPresenter.SearchProjectView;
import com.colinalworth.gwt.viola.web.shared.request.SearchRequest;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

public class SearchProjectPresenter extends AbstractPresenterImpl<SearchProjectView, SearchProjectPlace> {
	public interface SearchProjectView extends View<SearchProjectPresenter> {
		void reset();
		void setQuery(String query);
		void setResults(List<ProjectSearchResult> results);
	}
	public interface SearchProjectPlace extends Place {
		String getQuery();
		void setQuery(String query);
	}

	@Inject
	Provider<SearchRequest> searchRequest;
	@Inject
	PlaceManager placeManager;

	@Override
	public void go(AcceptsView parent, SearchProjectPlace place) {
		super.go(parent, place);
		getView().reset();

		if (place.getQuery() != null && !place.getQuery().isEmpty()) {
			getView().setQuery(place.getQuery());
			searchRequest.get().searchProjects(place.getQuery(), "", 20, new AsyncCallback<List<ProjectSearchResult>>() {
				@Override
				public void onFailure(Throwable caught) {
					GWT.reportUncaughtException(caught);
				}

				@Override
				public void onSuccess(List<ProjectSearchResult> results) {
					getView().setResults(results);
				}
			});
		}
	}

	public void search(String query) {
		SearchProjectPlace next = placeManager.create(SearchProjectPlace.class);
		next.setQuery(query);
		placeManager.submit(next);
	}

	public void select(ProjectSearchResult value) {
		ExamplePlace example = placeManager.create(ExamplePlace.class);
		example.setId(value.getLatestCompiledId());
		placeManager.submit(example);
	}

}
