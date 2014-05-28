package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.shared.dto.ProfileSearchResult;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface SearchRequest {

	void searchProjects(String query, String lastId, Integer limit, AsyncCallback<List<ProjectSearchResult>> callback);

	void searchProfiles(String query, String lastId, Integer limit, AsyncCallback<List<ProfileSearchResult>> callback);
}
