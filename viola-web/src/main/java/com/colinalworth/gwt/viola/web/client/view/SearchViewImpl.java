package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.SearchPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.SearchPresenter.SearchView;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectProperties;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.TextField;

import java.util.List;

public class SearchViewImpl extends AbstractClientView<SearchPresenter> implements SearchView {
	private ListStore<ProjectSearchResult> store;
	private ListView<ProjectSearchResult, ProjectSearchResult> listview;
	private TextField query = new TextField();

	private VerticalLayoutContainer root = new VerticalLayoutContainer();


	public SearchViewImpl() {
		query.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				getPresenter().search(event.getValue());//TODO cooldown
			}
		});

		ProjectProperties props = GWT.create(ProjectProperties.class);
		store = new ListStore<ProjectSearchResult>(props.key());

		listview = new ListView<ProjectSearchResult, ProjectSearchResult>(store, new IdentityValueProvider<ProjectSearchResult>());
		listview.setCell(new AbstractCell<ProjectSearchResult>("click") {
			@Override
			public void render(Context context, ProjectSearchResult value, SafeHtmlBuilder sb) {
				sb.appendEscaped(value.title);//todo template me
			}

			@Override
			public void onBrowserEvent(Context context, Element parent, ProjectSearchResult value, NativeEvent event, ValueUpdater<ProjectSearchResult> valueUpdater) {
				if (event.getType().equals("click")) {
					getPresenter().select(value);
				}
			}
		});

		root.add(query, new VerticalLayoutData(1, -1));
		root.add(listview, new VerticalLayoutData(1, 1));
		initWidget(root);
	}

	@Override
	public void reset() {
		query.reset();
		store.clear();
	}

	@Override
	public void setQuery(String query) {
		this.query.setValue(query);
	}

	@Override
	public void setResults(List<ProjectSearchResult> results) {
		store.replaceAll(results);
	}

}
