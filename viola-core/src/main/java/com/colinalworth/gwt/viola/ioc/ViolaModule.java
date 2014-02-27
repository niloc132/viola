
package com.colinalworth.gwt.viola.ioc;

import rxf.server.guice.CouchModuleBuilder;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.CompilerLog;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.entity.User;
import com.colinalworth.gwt.viola.service.JobService;
import com.google.inject.AbstractModule;

public class ViolaModule extends AbstractModule {

	
	@Override
	protected void configure() {
		install(new CouchModuleBuilder("v")
		.withEntity(User.class)
		.withEntity(SourceProject.class)
		.withEntity(CompiledProject.class)
		.withEntity(CompilerLog.class)
		.withService(JobService.CompiledProjectQueries.class)
		.withService(JobService.SourceProjectQueries.class)
		.withService(JobService.LogQueries.class)
		.build());
	}

}
