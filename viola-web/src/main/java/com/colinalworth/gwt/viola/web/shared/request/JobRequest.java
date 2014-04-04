package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

public interface JobRequest {
	void getMyJobs(AsyncCallback<List<ProjectSearchResult>> callback);

	void getAttachment(String projectId, String path, AsyncCallback<String> callback);
	void attach(String projectId, String filename, String contents, AsyncCallback<Project> callback);
	void delete(String projectId, String filename, AsyncCallback<Project> callback);

	void getProject(String id, AsyncCallback<Project> callback);

	void createProject(AsyncCallback<Project> callback);
	void saveProject(Project project, AsyncCallback<Project> callback);

	void build(String projectId, AsyncCallback<Void> callback);
	void checkStatus(String projectId, AsyncCallback<CompiledProjectStatus> callback);

	void getCompiledId(String id, AsyncCallback<String> asyncCallback);
}
