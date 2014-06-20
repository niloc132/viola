package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public abstract class AbstractPresenterImpl<V extends View, P extends Place> implements Presenter<P> {

	@Inject
	private V view;

	@Inject
	private Errors errors;

	@Inject
	private PageTitle title;

	private P currentPlace;

	@Override
	public void go(AcceptsView parent, P place) {
//		assert currentPlace == null : "Apparently still at previous place " + currentPlace;
		this.currentPlace = place;
		view.setPresenter(this);
		parent.setView(view);
	}

	@Override
	public String maybeStop() {
		return null;
	}

	@Override
	public void stop() {
		currentPlace = null;
		view.setPresenter(null);
	}

	@Override
	public void cancel() {
		currentPlace = null;
		view.setPresenter(null);
	}

	protected V getView() {
		return view;
	}
	protected P getCurrentPlace() {
		return currentPlace;
	}
	protected void setCurrentPlace(P place) {
		this.currentPlace = place;
	}

	@Override
	public PageTitle getTitle() {
		return title;
	}

	@Override
	public Errors getErrors() {
		return errors;
	}

	public abstract static class AbstractClientView<P extends Presenter<?>> implements View<P> {
		private P presenter;
		private Widget root;
		protected AbstractClientView() {
			assert GWT.isClient() : "Can't create client view on the server";
		}
		protected void initWidget(Widget root) {
			assert this.root == null : "Can't call initWidget more than once on the same instance";
			assert root != null : "Can't assign a null widget";
			this.root = root;
		}

		@Override
		public Widget asWidget() {
			assert root != null : "view's widget is null, either call initWidget or override asWidget";
			return root;
		}

		@Override
		public final SafeHtml asSafeHtml() {
			assert false : "asSafeHtml should not be called from client code";
			return null;
		}

		public P getPresenter() {
			return presenter;
		}

		public void setPresenter(P presenter) {
			this.presenter = presenter;
		}
	}
	public abstract static class AbstractServerView<P extends Presenter<?>> implements View<P> {
		private P presenter;
		protected AbstractServerView() {
			assert !GWT.isClient() : "Can't create server view on the client";
		}

		@Override
		public final Widget asWidget() {
			assert false : "asWidget should not be called from server code";
			return null;
		}

		public P getPresenter() {
			return presenter;
		}

		public void setPresenter(P presenter) {
			this.presenter = presenter;
		}
	}
}
