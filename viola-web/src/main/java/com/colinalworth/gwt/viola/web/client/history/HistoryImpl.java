package com.colinalworth.gwt.viola.web.client.history;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.inject.Singleton;

/**
 * Java-ish impl of PushState wiring, covering up all the ugly bits
 */
@Singleton
public class HistoryImpl implements HasValueChangeHandlers<String> {
	private HandlerManager handlerManager = new HandlerManager(this);

	public HistoryImpl() {
		initPopStateHandler();
		Event.addNativePreviewHandler(event -> {
			NativeEvent e = event.getNativeEvent();
			if (e.getType().equals("click")) {
				EventTarget target = e.getEventTarget();
				if (AnchorElement.is(target)) {
					e.preventDefault();
					String href = target.<AnchorElement>cast().getAttribute("href");
					if (href.startsWith("/")) {
						href = href.substring(1);
					} else {
						assert false : "Implement relative urls";
					}
					newItem(href, true);
				}
			}
		});
	}

	private void initPopStateHandler() {
		addPopStateListener(window(), (event) -> doPopState(event.getState().getHistoryToken()));
	}

	private void doPopState(String historyToken) {
		ValueChangeEvent.fire(this, historyToken);
	}

	public void newItem(String token, boolean fireEvent) {
		newToken(token);
		if (fireEvent) {
			ValueChangeEvent.fire(this, token);
		}
	}

	private void newToken(String token) {
		window().history().pushState(new History.StateImpl(token), window().document().getTitle(), "/" + token);
	}

	private native Window window() /*-{
		//window.history.pushState({}, '', '');
		return $wnd;
	}-*/;


	//TODO static as a workaround for failing default methods, and helper is workaround for no @JsFunction...
	//TODO also, should be in Window...
	static void addPopStateListener(Window window, History.PopStateEventListener listener) {
		window.addEventListener("popstate", listener::onPopState);
	}

	public void back() {
		window().history().back();
	}

	public void fireCurrentHistoryState() {
		ValueChangeEvent.fire(this, window().location().pathname().substring(1) + window().location().search());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		initPopStateHandler();
		return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}
}
