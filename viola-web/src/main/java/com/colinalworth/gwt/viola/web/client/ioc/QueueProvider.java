package com.colinalworth.gwt.viola.web.client.ioc;

import com.colinalworth.gwt.viola.web.shared.request.ViolaRequestQueue;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class QueueProvider implements Provider<ViolaRequestQueue> {
	@Inject @Session Provider<String> sessionId;
	private ViolaRequestQueue current = null;

	@Override
	public ViolaRequestQueue get() {
		if (current != null) {
			return current;
		}
		current = GWT.create(ViolaRequestQueue.class);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				current.fire();
				current = null;
			}
		});
		if (sessionId.get() != null) {
			current.session().setSessionId(sessionId.get());
		}
		return current;
	}
}
