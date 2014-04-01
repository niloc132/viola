package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface SearchRequest {
	void search(String query, String lastId, Integer limit, AsyncCallback<List<ProjectSearchResult>> callback);
}
