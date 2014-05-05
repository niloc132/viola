package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.service.JobService;
import com.colinalworth.gwt.viola.service.UserService;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import rxf.shared.CouchTx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class JobWebService {
	@Inject
	JobService jobService;

	@Inject
	SessionService sessionService;
	@Inject
	UserService userService;

	public List<ProjectSearchResult> getMyJobs() {
		return null;
	}

	public String getAttachment(String projectId, String path) {
		return jobService.getSourceAsString(jobService.find(projectId), path);
	}

	public Project attach(String projectId, String filename, String contents) {
		//TODO limit to owner
		SourceProject proj = jobService.find(projectId);
		if (proj.getAuthorId().equals(sessionService.getThreadLocalUserId("attach"))) {
			final CouchTx tx;
			if (proj.getAttachments().containsKey(filename)) {
				tx = jobService.updateSourceFile(proj, filename, contents);
			} else {
				tx = jobService.createSourceFile(proj, filename, contents);
			}
			return getProject(tx.id());
		}
		throw new IllegalStateException("Can't change files of project you don't own");
	}

	public Project delete(String projectId, String filename) {
		//TODO limit to owner


		SourceProject proj = jobService.find(projectId);
		if (proj.getAuthorId().equals(sessionService.getThreadLocalUserId("delete"))) {
			CouchTx tx = jobService.deleteSourceFile(proj, filename);
			return getProject(tx.id());
		}

		throw new IllegalStateException("Can't delete a project you don't own");
	}

	public Project getProject(String id) {
		SourceProject sourceProject = jobService.find(id);
		Project p = new Project();
		p._id = id;
		p._rev = sourceProject.getRev();
		p.description = sourceProject.getDescription();
		p.title = sourceProject.getTitle();
		List<CompiledProject> compiledOutput = jobService.getCompiledOuput(sourceProject);
		p.latestCompiledId = compiledOutput.isEmpty() ? null : compiledOutput.get(0).getId();
		p.files = new ArrayList<>();
		for (String path : sourceProject.getAttachments().keySet()) {
			p.files.add(path);
		}
		Collections.sort(p.files);
		return p;
	}

	public Project createProject() {
		String owner = sessionService.getThreadLocalUserId("create");
		if (owner == null) {
			throw new IllegalStateException("Can't create a project without logging in");
		}

		SourceProject project = jobService.createProject(owner);

		return getProject(project.getId());
	}
	public Project saveProject(Project project) {
		SourceProject sourceProject = jobService.find(project._id);
		if (sourceProject.getAuthorId().equals(sessionService.getThreadLocalUserId("save"))) {
			sourceProject.setDescription(project.description);
			sourceProject.setTitle(project.title);

			jobService.saveProject(sourceProject);

			return getProject(project._id);
		}
		throw new IllegalStateException("Can't save project user doesn't own");
	}

	public void build(String projectId) {
		SourceProject project = jobService.find(projectId);
		if (project.getAuthorId().equals(sessionService.getThreadLocalUserId("compile"))) {
			jobService.submitJob(project);
		} else {
			throw new IllegalStateException("Can't build project user doesn't own");
		}
	}

	public CompiledProjectStatus checkStatus(String projectId) {
		return CompiledProjectStatus.values()[jobService.getCompiledOuput(jobService.find(projectId)).get(0).getStatus().ordinal()];
	}

	public String getCompiledId(String id) {
		List<CompiledProject> output = jobService.getCompiledOuput(jobService.find(id));
		if (output.isEmpty()) {
			return null;
		}
		return output.get(0).getId();
	}
}
