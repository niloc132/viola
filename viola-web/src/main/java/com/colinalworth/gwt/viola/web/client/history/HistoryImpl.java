package com.colinalworth.gwt.viola.web.client.history;

import com.colinalworth.gwt.viola.web.client.history.BeforeHistoryChangeEvent.BeforeHistoryChangeHandler;
import com.colinalworth.gwt.viola.web.client.history.HistoryChangeEvent.HistoryChangeHandler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.inject.Singleton;

/**
 * Java-ish impl of PushState wiring, covering up all the ugly bits
 */
@Singleton
public class HistoryImpl {
	private HandlerManager handlerManager = new HandlerManager(this);
	private boolean handlingState;

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
		History.PopStateEventListener listener = (event) -> doPopState(event.getState().historyToken);
		Window.getSelf().addEventListener("popstate", listener::onPopState);
	}

	private void doPopState(String historyToken) {
		fireChange(historyToken);
	}

	public void newItem(String token, boolean fireEvent) {
		if (handlingState) {
			return;
		}
		newToken(token);
		if (fireEvent) {
			fireChange(token);
		}
	}

	private void fireChange(String token) {
		BeforeHistoryChangeEvent event = new BeforeHistoryChangeEvent(token);
		handlerManager.fireEvent(event);
		if (event.getCanonicalToken() != null) {
			assert !event.getCanonicalToken().equals(token);
			replaceToken(event.getCanonicalToken());
		}
		handlingState = true;
		handlerManager.fireEvent(new HistoryChangeEvent(event.getCanonicalToken() == null ? token : event.getCanonicalToken()));
		handlingState = false;
	}

	private void newToken(String token) {
		Window.getSelf().getHistory().pushState(History.state(token), Window.getSelf().getDocument().getTitle(), "/" + token);
	}

	private void replaceToken(String token) {
		Window.getSelf().getHistory().replaceState(History.state(token), Window.getSelf().getDocument().getTitle(), "/" + token);
	}

//	//TODO static as a workaround for failing default methods, and helper is workaround for no @JsFunction...
//	//TODO also, should be in Window...
//	static void addPopStateListener(Window window, History.PopStateEventListener listener) {
//		window.addEventListener("popstate", listener::onPopState);
//	}

	public void back() {
		Window.getSelf().getHistory().back();
	}

	public void fireCurrentHistoryState() {
		String token = Window.getSelf().getLocation().getPathname().substring(1) + Window.getSelf().getLocation().getSearch();

		replaceToken(token);
		fireChange(token);
	}


	public HandlerRegistration addBeforeHistoryChangeHandler(BeforeHistoryChangeHandler handler) {
		return handlerManager.addHandler(BeforeHistoryChangeEvent.TYPE, handler);
	}
	public HandlerRegistration addHistoryChangeHandler(HistoryChangeHandler handler) {
		return handlerManager.addHandler(HistoryChangeEvent.TYPE, handler);
	}

}
