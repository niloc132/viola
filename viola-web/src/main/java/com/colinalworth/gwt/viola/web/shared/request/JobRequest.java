package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.shared.dto.CompileLimitException;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.MustBeLoggedInException;
import com.colinalworth.gwt.viola.web.shared.dto.NotFoundException;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.rpq.client.AsyncService.Throws;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JobRequest {
	@Throws(NotFoundException.class)
	void getAttachment(String projectId, String path, AsyncCallback<String> callback);

	@Throws({MustBeLoggedInException.class, NotFoundException.class})
	void attach(String projectId, String filename, String contents, AsyncCallback<Project> callback);
	@Throws({MustBeLoggedInException.class, NotFoundException.class})
	void delete(String projectId, String filename, AsyncCallback<Project> callback);

	@Throws(NotFoundException.class)
	void getProject(String id, AsyncCallback<Project> callback);

	@Throws(MustBeLoggedInException.class)
	void createProject(AsyncCallback<Project> callback);

	@Throws({MustBeLoggedInException.class, NotFoundException.class})
	void cloneProject(String other, AsyncCallback<Project> callback);

	@Throws({MustBeLoggedInException.class, NotFoundException.class})
	void saveProject(Project project, AsyncCallback<Project> callback);

	@Throws({MustBeLoggedInException.class, CompileLimitException.class, NotFoundException.class})
	void build(String projectId, AsyncCallback<Void> callback);

	@Throws(NotFoundException.class)
	void checkStatus(String projectId, AsyncCallback<CompiledProjectStatus> callback);

	@Throws(NotFoundException.class)
	void getCompiledId(String id, AsyncCallback<String> asyncCallback);
}
