package com.colinalworth.gwt.viola.web.shared.request;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerHealthRequest {
	void getPending(AsyncCallback<Integer> callback);
	void getCompiling(AsyncCallback<Integer> callback);
	void getComplete(AsyncCallback<Integer> callback);
	void getFailed(AsyncCallback<Integer> callback);

	void getAgentCount(AsyncCallback<Integer> callback);
}
