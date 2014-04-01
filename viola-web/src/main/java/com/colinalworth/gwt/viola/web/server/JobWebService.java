package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.service.JobService;
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
	public List<ProjectSearchResult> getMyJobs() {
		return null;
	}

	public String getAttachment(String projectId, String path) {
		return jobService.getSourceAsString(jobService.find(projectId), path);
	}

	public Project attach(String projectId, String filename, String contents) {
		//TODO limit to owner
		SourceProject proj = jobService.find(projectId);
		final CouchTx tx;
		if (proj.getAttachments().containsKey(filename)) {
			tx = jobService.updateSourceFile(proj, filename, contents);
		} else {
			tx = jobService.createSourceFile(proj, filename, contents);
		}
		return getProject(tx.id());
	}

	public Project delete(String projectId, String filename) {
		//TODO limit to owner
		CouchTx tx = jobService.deleteSourceFile(jobService.find(projectId), filename);
		return getProject(tx.id());
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
		//TODO set owner
		return getProject(jobService.createProject().getId());
	}
	public Project saveProject(Project project) {
		//TODO limit to owner
		SourceProject sourceProject = jobService.find(project._id);
		sourceProject.setDescription(project.description);
		sourceProject.setTitle(project.title);

		jobService.saveProject(sourceProject);

		return getProject(project._id);
	}

	public void build(String projectId) {
		jobService.submitJob(jobService.find(projectId));
	}

	public CompiledProjectStatus checkStatus(String projectId) {
		return CompiledProjectStatus.values()[jobService.getCompiledOuput(jobService.find(projectId)).get(0).getStatus().ordinal()];
	}
}
