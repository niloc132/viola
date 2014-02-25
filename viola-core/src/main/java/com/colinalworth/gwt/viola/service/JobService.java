package com.colinalworth.gwt.viola.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import rxf.server.CouchService;
import rxf.server.CouchService.Attachments;
import rxf.server.CouchTx;
import rxf.server.driver.CouchMetaDriver;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.CompiledProject.Status;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

public class JobService {
	public interface SourceProjectQueries extends CouchService<SourceProject> {
		@View(map = "function(doc) {" +
						"emit(doc.compiledId, doc);" +
					"}")
		List<SourceProject> getSourcesForCompiledProject(@Key String compiledId);
	}
	public interface CompiledProjectQueries extends CouchService<CompiledProject> {
		@View(map = "function(doc){" +
						"emit(doc.status, doc);" +
					"}")
		@Limit(5)
		List<CompiledProject> getProjectsWithStatus(@Key CompiledProject.Status status);
	}
	
	@Inject SourceProjectQueries sourceQueries;
	@Inject CompiledProjectQueries compiledQueries;
	
	public SourceProject createJob(SourceProject project) {
		String id = sourceQueries.persist(project).id();
		return sourceQueries.find(id);
	}
	
	public SourceProject saveProject(SourceProject project) {
		if (project.getCompiled() != null) {
			//TODO copy and make a new one, link to old one, save it, return new one
			return null;
		} else {
			sourceQueries.persist(project);
			return project;
		}
	}
	
	public void submitJob(SourceProject project) {
		CompiledProject compiled = new CompiledProject();
		compiled.setStatus(Status.QUEUED);
		String id = compiledQueries.persist(compiled).id();
		project.setCompiledId(id);
	}
	
	
	public CompiledProject setJobStatus(CompiledProject job, CompiledProject.Status status) {
		CompiledProject.Status oldStatus = job.getStatus();
		job.setStatus(status);
		//fails if our copy isn't the latest (meaning someone else already updated)
		CouchTx tx = compiledQueries.persist(job);
		if (tx.ok()) {
			JsonObject obj = CouchMetaDriver.gson().toJsonTree(job).getAsJsonObject();
			obj.addProperty("_rev", tx.rev());
			return CouchMetaDriver.gson().fromJson(obj, CompiledProject.class);
		} else {
			job.setStatus(oldStatus);
			return null;
		}
	}

	public CompiledProject unqueue() {
		List<CompiledProject> possibleJobs = compiledQueries.getProjectsWithStatus(Status.QUEUED);
		for (CompiledProject proj : possibleJobs) {
			CompiledProject updated = setJobStatus(proj, Status.ACCEPTED);
			if (updated != null) {
				return updated;
			}
		}
		//either list was short/empty, or we got to the end without finding something that wasn't taken while 
		//we were looking at it, either way seems we're out of work to do
		return null;
	}

	public List<SourceProject> getSources(CompiledProject proj) {
		return sourceQueries.getSourcesForCompiledProject(proj.getId());
	}

	public CompiledProject attachOutputDir(CompiledProject proj, File warDir) throws FileNotFoundException {
		Attachments attachments = compiledQueries.attachments(proj);
		attachHelper(attachments, warDir, warDir);
		return compiledQueries.find(proj.getId());
	}

	private void attachHelper(Attachments attachments, File root, File current) throws FileNotFoundException {
		if (current.isDirectory()) {
			for (File f : current.listFiles()) {
				attachHelper(attachments, root, f);
			}
		} else {
			String fileName = root.toURI().relativize(current.toURI()).getPath();
			String type = URLConnection.guessContentTypeFromName(current.getAbsolutePath());
			
			CouchTx tx = attachments.addAttachment(readFileAsString(current), fileName, type);
			if (!tx.ok()) {
				throw new RuntimeException("Failed to updated: " + tx.error());
			}
		}
	}

	private String readFileAsString(File current) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(current.getAbsolutePath()));
			return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public InputStream getSourceAsStream(SourceProject source, String name) {
		String data = sourceQueries.attachments(source).getAttachment(name);
		
		return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
	}
}
