package com.colinalworth.gwt.viola.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import rxf.server.BlobAntiPatternObject;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.CompiledProject.Status;
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
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;
import com.google.gwt.dev.jjs.JJSOptions;
import com.google.gwt.dev.jjs.PermutationResult;
import com.google.gwt.dev.util.FileBackedObject;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CouchCompiler {
	private volatile boolean shutdown = false;
	private volatile long lastBusy;
	
	@Inject JobService jobs;
	@Inject @Named("gwtCompilerClasspath") URL[] gwtCompilerClasspath;

	public void start() {
		BlobAntiPatternObject.getEXECUTOR_SERVICE().submit(new Runnable() {
			public void run() {
				if (shutdown) {
					System.out.println("shutting down");
					return;
				}
				System.out.println("checking for work");
				try {
					CompiledProject proj = jobs.unqueue();
					if (proj != null) {
						//may be several, for now just one?
						List<SourceProject> sources = jobs.getSources(proj);
		
						//TODO handle more than one
						//TODO figure out how to merge them in the first place
						compile(sources, proj);
						
						lastBusy = System.currentTimeMillis();
					} else {
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					return;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					start();
				}

			}
		});
	}
	
	public void stop() {
		shutdown = true;
	}
	
	public int getTimeIdle() {
		return (int) (System.currentTimeMillis() - lastBusy);
	}
	
	
	private void compile(List<SourceProject> sources, CompiledProject proj) {
		assert !sources.isEmpty();
		SourceProject source = sources.get(0);

		ClassLoader old = Thread.currentThread().getContextClassLoader();
		File jobDir = null;
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

			SerializableTreeLogger logger = new SerializableTreeLogger();
			logger.setMaxDetail(TreeLogger.INFO);
			
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
			List<FileBackedObject<PermutationResult>> resultFiles = CompilePerms.makeResultFiles(workDir, allPerms);
			CompilePerms.compile(logger, compilerContext, precompilation, allPerms, options.getLocalWorkers(), resultFiles);

			ArtifactSet generatedArtifacts = precompilation.getGeneratedArtifacts();
			JJSOptions precompileOptions = precompilation.getUnifiedAst().getOptions();

			//link
			proj = jobs.setJobStatus(proj, Status.LINKING);
			Link.link(logger, module, generatedArtifacts, allPerms, resultFiles, precompileOptions, options);

			//attach results to document
			proj = jobs.attachOutputDir(proj, new File(warDir, module.getName()));
			
			System.out.println(logger.getJsonObject());

			proj = jobs.setJobStatus(proj, Status.COMPLETE);

		} catch (Throwable ex) {
			ex.printStackTrace();
			proj = jobs.setJobStatus(proj, Status.FAILED);
			//TODO report error/s
		} finally {
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
		return new CouchCompilerOptions(warDir, workDir, deployDir);
	}
	
	private ModuleDef makeModule(final SourceProject source, CompilerContext ctx, TreeLogger logger) throws UnableToCompleteException {
		return ModuleDefLoader.loadFromClassPath(logger, ctx, source.getModule());
	}
	
	private URL[] getCompilerClasspathElements() {
		return gwtCompilerClasspath;
	}

}
