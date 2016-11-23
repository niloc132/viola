package com.colinalworth.gwt.viola.compiler;

import com.colinalworth.gwt.viola.compiler.status.StatusUpdateService;
import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.CompiledProject.Status;
import com.colinalworth.gwt.viola.entity.CompilerLog;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.service.JobService;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.dev.CompilePerms;
import com.google.gwt.dev.CompilerContext;
import com.google.gwt.dev.CompilerOptions;
import com.google.gwt.dev.Link;
import com.google.gwt.dev.Permutation;
import com.google.gwt.dev.Precompilation;
import com.google.gwt.dev.Precompile;
import com.google.gwt.dev.PrecompileTaskOptions;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;
import com.google.gwt.dev.jjs.PermutationResult;
import com.google.gwt.dev.util.PersistenceBackedObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class CouchCompiler {
	private static final int jobCheckPeriodMillis = 10000;
	private static final int shutdownCheckPeriodSeconds = 5;//at least 1s to avoid monopolizing db
	private static final int shutdownTimeoutSeconds = 60 * 5;//at least 60s to allow compilation to complete

	//distinct pool from rxf to allow independent shutdown
	private ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

	@Inject JobService jobs;

	@Inject StatusUpdateService status;
	private Thread shutdownHook = new Thread(){
		@Override
		public void run() {
			System.out.println("JVM shutdown, notifying db");
			try {
				//TODO actually stop current job
				status.notifyStopped();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	public void serveUntilShutdown() {
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		pool.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				checkForWork();
			}
		}, 0, jobCheckPeriodMillis, TimeUnit.MILLISECONDS);

		System.out.println("agent successfully started");

		while (true) {
			try {
				if (status.shouldShutdown()) {
					break;
				}
				Thread.sleep(shutdownCheckPeriodSeconds * 1000);
			} catch (Exception e) {
				//failure in checking on shutdown status (network issue?) or interrupt
				e.printStackTrace();
			}
		}

		status.notifyShuttingDown();

		shutdownAndAwaitTermination(shutdownTimeoutSeconds);//allow 5 minutes to shutdown

		status.notifyStopped();
		Runtime.getRuntime().removeShutdownHook(shutdownHook);
	}

	public void checkForWork() {
		System.out.println("checking for work, idle time: " + status.getTimeIdle());
		try {
			CompiledProject proj = jobs.unqueue(status.getAgentId());
			if (proj != null) {
				SourceProject source = jobs.getSourceProject(proj);
				status.notifyWorking();
				compile(source, proj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			status.notifyIdle();
		}
	}


	private void compile(SourceProject source, CompiledProject proj) {

		ClassLoader old = Thread.currentThread().getContextClassLoader();
		File jobDir = null;

		SerializableTreeLogger logger = new SerializableTreeLogger();
		logger.setMaxDetail(TreeLogger.INFO);

		try {
			jobDir = File.createTempFile("rxf-job-", "-" + source.getId());

			jobDir.delete();
			jobDir.mkdirs();
			File sourceDir = new File(jobDir, "source");
			sourceDir.mkdir();
			unpack(source, sourceDir);

			File warDir = new File(jobDir, "war");
			warDir.mkdir();

			File genDir = new File(jobDir, "gen");
			genDir.mkdir();

			File workDir = new File(jobDir, "work");
			workDir.mkdir();

			File deployDir = new File(jobDir, "deploy");
			deployDir.mkdir();


			ClassLoader jobClassLoader = new URLClassLoader(new URL[]{ sourceDir.toURI().toURL() }, old);
			Thread.currentThread().setContextClassLoader(jobClassLoader);


			CompilerOptions options = (CompilerOptions) makeOptions(source, warDir, workDir, deployDir);

			CompilerContext.Builder compilerContextBuilder = new CompilerContext.Builder();
			CompilerContext compilerContext = compilerContextBuilder.options(options).build();

			ModuleDef module = makeModule(source, compilerContext, logger);
			compilerContext = compilerContextBuilder.module(module).build();

			//precompile
			proj = jobs.setJobStatus(proj, Status.PRECOMPILING);
			Precompilation precompilation = Precompile.precompile(logger, compilerContext);


			//compile
			proj = jobs.setJobStatus(proj, Status.COMPILING);
			Permutation[] allPerms = precompilation.getPermutations();
			List<PersistenceBackedObject<PermutationResult>> resultFiles = CompilePerms.makeResultFiles(workDir, allPerms, options);
			CompilePerms.compile(logger, compilerContext, precompilation, allPerms, options.getLocalWorkers(), resultFiles);

			ArtifactSet generatedArtifacts = precompilation.getGeneratedArtifacts();
			PrecompileTaskOptions precompileOptions = precompilation.getUnifiedAst().getOptions();

			//link
			proj = jobs.setJobStatus(proj, Status.LINKING);
			Link.link(logger, module, module.getPublicResourceOracle(), generatedArtifacts, allPerms, resultFiles, Collections.emptySet(), precompileOptions, options);

			//attach results to document
			proj = jobs.attachOutputDir(proj, new File(warDir, module.getName()));


			proj = jobs.setJobStatus(proj, Status.COMPLETE);

		} catch (Throwable ex) {
			ex.printStackTrace();
			proj = jobs.setJobStatus(proj, Status.FAILED);
			//TODO report error/s
		} finally {
			CompilerLog log = new CompilerLog(proj.getId(), logger.getModel());
			jobs.saveLog(log);
//			System.out.println(CouchMetaDriver.gson().toJson(logger.getModel()));

			//remove classloader
			Thread.currentThread().setContextClassLoader(old);

			//remove modules from internal gwt cache
			ModuleDefLoader.clearModuleCache();

			//cleanup files
			//war, work
			if (jobDir != null) {
				jobDir.delete();
			}

		}
	}

	private void unpack(SourceProject source, File sourceDir) throws IOException {
		for (String path : source.getAttachments().keySet()) {
			File newFile = new File(sourceDir, path);
			newFile.getParentFile().mkdirs();
			byte[] buffer = new byte[8 * 1024];

			InputStream stream = jobs.getSourceAsStream(source, path);
			try {
				OutputStream out = new FileOutputStream(newFile);
				try {
					int bytesRead;
					while ((bytesRead = stream.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
				} finally {
					out.close();
				}
			} finally {
				stream.close();
			}
		}
	}

	private Object makeOptions(SourceProject source, File warDir, File workDir, File deployDir) {
		return new CouchCompilerOptions(warDir, workDir, deployDir, source.getModule());
	}

	private ModuleDef makeModule(final SourceProject source, CompilerContext ctx, TreeLogger logger) throws UnableToCompleteException {
		return ModuleDefLoader.loadFromClassPath(logger, source.getModule(), true);
	}

	private void shutdownAndAwaitTermination(int timeoutInSeconds) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(timeoutInSeconds, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(timeoutInSeconds, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
					throw new IllegalStateException("Pool failed to terminate after " + timeoutInSeconds + " second timeout when requested");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}


}
