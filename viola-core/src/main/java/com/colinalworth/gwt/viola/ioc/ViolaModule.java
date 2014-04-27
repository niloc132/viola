
package com.colinalworth.gwt.viola.ioc;

import com.colinalworth.gwt.viola.entity.AgentStatus;
import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.entity.CompilerLog;
import com.colinalworth.gwt.viola.entity.Session;
import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.entity.User;
import com.colinalworth.gwt.viola.service.AgentStatusService.AgentStatusQueries;
import com.colinalworth.gwt.viola.service.JobService.CompiledProjectQueries;
import com.colinalworth.gwt.viola.service.JobService.LogQueries;
import com.colinalworth.gwt.viola.service.JobService.SourceProjectQueries;
import com.colinalworth.gwt.viola.service.UserService.SessionQueries;
import com.colinalworth.gwt.viola.service.UserService.UserQueries;
import com.google.inject.AbstractModule;
import rxf.server.guice.CouchModuleBuilder;

public class ViolaModule extends AbstractModule {


	@Override
	protected void configure() {
		install(new CouchModuleBuilder("v")
				.withEntity(User.class)
				.withEntity(Session.class)
				.withEntity(SourceProject.class)
				.withEntity(CompiledProject.class)
				.withEntity(CompilerLog.class)
				.withEntity(AgentStatus.class)
				.withService(CompiledProjectQueries.class)
				.withService(SourceProjectQueries.class)
				.withService(LogQueries.class)
				.withService(AgentStatusQueries.class)
				.withService(UserQueries.class)
				.withService(SessionQueries.class)
				.build());
	}

}
