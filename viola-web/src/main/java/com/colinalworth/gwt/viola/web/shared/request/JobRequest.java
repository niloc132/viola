package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.shared.dto.CompileLimitException;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.MustBeLoggedInException;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.rpq.client.AsyncService.Throws;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JobRequest {
	void getAttachment(String projectId, String path, AsyncCallback<String> callback);

	@Throws(MustBeLoggedInException.class)
	void attach(String projectId, String filename, String contents, AsyncCallback<Project> callback);
	@Throws(MustBeLoggedInException.class)
	void delete(String projectId, String filename, AsyncCallback<Project> callback);

	void getProject(String id, AsyncCallback<Project> callback);

	@Throws(MustBeLoggedInException.class)
	void createProject(AsyncCallback<Project> callback);

	@Throws(MustBeLoggedInException.class)
	void cloneProject(String other, AsyncCallback<Project> callback);

	@Throws(MustBeLoggedInException.class)
	void saveProject(Project project, AsyncCallback<Project> callback);

	@Throws({MustBeLoggedInException.class, CompileLimitException.class})
	void build(String projectId, AsyncCallback<Void> callback);

	void checkStatus(String projectId, AsyncCallback<CompiledProjectStatus> callback);

	void getCompiledId(String id, AsyncCallback<String> asyncCallback);
}
