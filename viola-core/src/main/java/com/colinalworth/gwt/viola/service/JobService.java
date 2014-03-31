package com.colinalworth.gwt.viola.service;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.CompiledProject.Status;
import com.colinalworth.gwt.viola.entity.CompilerLog;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import rxf.server.CouchService;
import rxf.server.CouchService.Attachments;
import rxf.server.driver.CouchMetaDriver;
import rxf.shared.CouchTx;

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
import java.util.Date;
import java.util.List;

@Singleton
public class JobService {
	public interface SourceProjectQueries extends CouchService<SourceProject> {
	}
	public interface CompiledProjectQueries extends CouchService<CompiledProject> {
		@View(map = "function(doc){" +
				"emit(doc.status, doc);" +
				"}")
		@Limit(5)
		List<CompiledProject> getProjectsWithStatus(@Key CompiledProject.Status status);

		@View(map = "function(doc) {" +
				"emit(doc.sourceId, doc);" +
				"}")
		List<CompiledProject> getCompiledForSource(@Key String id);
	}
	public interface LogQueries extends CouchService<CompilerLog> {

	}

	@Inject SourceProjectQueries sourceQueries;
	@Inject CompiledProjectQueries compiledQueries;
	@Inject LogQueries logQueries;

	public SourceProject createJob(SourceProject project) {
		String id = sourceQueries.persist(project).id();
		return sourceQueries.find(id);
	}

	public SourceProject createProject() {
		SourceProject project = new SourceProject();
	    project.setLastUpdated(new Date());
		project.setModule("project.Sample");
		CouchTx tx = sourceQueries.persist(project);
		if (!tx.ok()) {
			throw new IllegalStateException(tx.error());
		}
		project = sourceQueries.find(tx.id());

		Attachments attachments = sourceQueries.attachments(project);
		attachments.addAttachment("<module>\n" +
				"  <inherits name=\"com.google.gwt.user.User\" />\n" +
				"  <entry-point class=\"project.client.SampleEntryPoint\" />\n" +
				"</module>", "project/Sample.gwt.xml", "application/xml");
		attachments.addAttachment("<!DOCTYPE html>\n" +
				"<html>\n" +
				"<head>\n" +
				"    <title>Sample App</title>\n" +
				"</head>\n" +
				"<body>\n" +
				"<script type=\"text/javascript\" src=\"project.Sample.nocache.js\"></script>\n" +
				"</body>\n" +
				"</html>", "project/public/index.html", "text/html");
		attachments.addAttachment("package project.client;\n" +
				"\n" +
				"import com.google.gwt.core.client.EntryPoint;\n" +
				"\n" +
				"public class SampleEntryPoint implements EntryPoint {\n" +
				"\tpublic void onModuleLoad() {\n" +
				"\t\tcom.google.gwt.user.client.Window.alert(\"SampleEntryPoint.java\");\n" +
				"\t}\n" +
				"}","project/client/SampleEntryPoint.java", "application/java");

		return project;
	}

	public SourceProject saveProject(SourceProject project) {
		String id = project.getId();
		project.setLastUpdated(new Date());
		CouchTx tx = sourceQueries.persist(project);
		if (!tx.ok()) {
			throw new IllegalStateException("Can't save project, was modified not up to date");
		}
		assert tx.id().equals(id);
		return sourceQueries.find(id);
	}

	public void submitJob(SourceProject project) {
		CompiledProject compiled = new CompiledProject();
		compiled.setStatus(Status.QUEUED);
		compiled.setSourceId(project.getId());
		String id = compiledQueries.persist(compiled).id();
//		project.setCompiledId(id);
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

	/**
	 *
	 * @param agentId the id of the server that will do the compiling
	 * @return a project that needs to be compiled, ready for use on the specified agent
	 */
	public CompiledProject unqueue(String agentId) {
		List<CompiledProject> possibleJobs = compiledQueries.getProjectsWithStatus(Status.QUEUED);
		for (CompiledProject proj : possibleJobs) {
			CompiledProject updated = setJobStatus(proj, Status.ACCEPTED);
			if (updated != null) {
				//now owned by the given agent (no one else will take it while marked ACCEPTED)), stick the id on it
				updated.setAgentId(agentId);
				updated = compiledQueries.find(compiledQueries.persist(updated).id());
				return updated;
			}
		}
		//either list was short/empty, or we got to the end without finding something that wasn't taken while 
		//we were looking at it, either way seems we're out of work to do
		return null;
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

	public String getSourceAsString(SourceProject source, String path) {
		return sourceQueries.attachments(source).getAttachment(path);
	}
	public InputStream getSourceAsStream(SourceProject source, String path) {
		String data = getSourceAsString(source, path);

		return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
	}

	public void saveLogs(CompiledProject proj, JsonObject log) {
		JsonObject root = new JsonObject();
		root.addProperty("compiledProjectId", proj.getId());
		root.add("node", log);
		//TODO
	}

	public SourceProject getSourceProject(CompiledProject proj) {
		return sourceQueries.find(proj.getSourceId());
	}

	public List<CompiledProject> getCompiledOuput(SourceProject proj) {
		return compiledQueries.getCompiledForSource(proj.getId());
	}

	public SourceProject find(String id) {
		return sourceQueries.find(id);
	}

	public CouchTx createSourceFile(SourceProject proj, String path, String contents) {
		String type = URLConnection.guessContentTypeFromName(path);
		return sourceQueries.attachments(proj).addAttachment(contents, path, type);
	}

	public CouchTx updateSourceFile(SourceProject proj, String path, String contents) {
		String type = URLConnection.guessContentTypeFromName(path);
		return sourceQueries.attachments(proj).updateAttachment(contents, path, type);
	}

	public CouchTx deleteSourceFile(SourceProject proj, String path) {
		return sourceQueries.attachments(proj).deleteAttachment(path);
	}
}
