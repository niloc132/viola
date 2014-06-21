package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.service.JobService;
import com.colinalworth.gwt.viola.service.UserService;
import com.colinalworth.gwt.viola.web.shared.dto.CompileLimitException;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.MustBeLoggedInException;
import com.colinalworth.gwt.viola.web.shared.dto.NotFoundException;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
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

	public String getAttachment(String projectId, String path) throws NotFoundException {
		return jobService.getSourceAsString(jobService.find(projectId), path);
	}

	public Project attach(String projectId, String filename, String contents) throws MustBeLoggedInException, NotFoundException {
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
		throw new MustBeLoggedInException("Can't change files of project you don't own");
	}

	public Project delete(String projectId, String filename) throws MustBeLoggedInException, NotFoundException {
		SourceProject proj = jobService.find(projectId);
		if (proj.getAuthorId().equals(sessionService.getThreadLocalUserId("delete"))) {
			CouchTx tx = jobService.deleteSourceFile(proj, filename);
			return getProject(tx.id());
		}

		throw new MustBeLoggedInException("Can't delete a project you don't own");
	}

	public Project getProject(String id) throws NotFoundException {
		SourceProject sourceProject = jobService.find(id);
		if (sourceProject == null) {
			throw new NotFoundException("Project with id " + id + " could not be found");
		}
		Project p = new Project();
		p.setId(id);
		p.setDescription(sourceProject.getDescription());
		p.setTitle(sourceProject.getTitle());
//		List<CompiledProject> compiledOutput = jobService.getCompiledOuput(sourceProject);
//		p.setLatestCompiledId(compiledOutput.isEmpty() ? null : compiledOutput.get(0).getId());
		p.setFiles(new ArrayList<>(sourceProject.getAttachments().keySet()));
		Collections.sort(p.getFiles());
		return p;
	}

	public Project createProject() throws MustBeLoggedInException {
		String owner = sessionService.getThreadLocalUserId("create");
		if (owner == null) {
			throw new MustBeLoggedInException("Can't create a project without logging in");
		}

		SourceProject project = jobService.createProject(owner);

		try {
			return getProject(project.getId());
		} catch (NotFoundException e) {
			//not possible, just created it
			return null;
		}
	}
	public Project cloneProject(String otherId) throws MustBeLoggedInException, NotFoundException {
		String owner = sessionService.getThreadLocalUserId("clone");
		if (owner == null) {
			throw new MustBeLoggedInException("Can't clone a project without logging in");
		}

		SourceProject original = jobService.find(otherId);
		if (original == null) {
			throw new NotFoundException("Project with id " + otherId + " could not be found");
		}
		SourceProject clone = jobService.cloneProjectToUser(original, owner);

		return getProject(clone.getId());
	}

	public Project saveProject(Project project) throws MustBeLoggedInException, NotFoundException {
		SourceProject sourceProject = jobService.find(project.getId());
		if (sourceProject == null) {
			throw new NotFoundException("Project with id " + project.getId() + " could not be found");
		}
		if (sourceProject.getAuthorId().equals(sessionService.getThreadLocalUserId("save"))) {
			sourceProject.setDescription(project.getDescription());
			sourceProject.setTitle(project.getTitle());

			jobService.saveProject(sourceProject);

			return getProject(project.getId());
		}
		throw new MustBeLoggedInException("Can't save project user doesn't own");
	}

	public void build(String projectId) throws CompileLimitException, MustBeLoggedInException {
		SourceProject project = jobService.find(projectId);
		String userId = sessionService.getThreadLocalUserId("compile");
		if (project.getAuthorId().equals(userId)) {
			//user owns the project

			//user is not presently compiling anything
			if (jobService.isCurrentlyCompiling(userId)){
				throw new CompileLimitException("You are already compiling, can't have multiple concurrent jobs running");
			}

			//user is within their quota
			if (jobService.getCompileCountTodayForUser(userId) > 20) {
				throw new CompileLimitException("You've exceeded your daily limit for compiles");
			}

			jobService.submitJob(project);
		} else {
			throw new MustBeLoggedInException("Can't build project user doesn't own");
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
