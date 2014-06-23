
package com.colinalworth.gwt.viola.ioc;

import com.colinalworth.gwt.viola.entity.*;
import com.colinalworth.gwt.viola.gson.DateTypeAdapter;
import com.colinalworth.gwt.viola.service.AgentStatusService.AgentStatusQueries;
import com.colinalworth.gwt.viola.service.JobService.CompiledProjectQueries;
import com.colinalworth.gwt.viola.service.JobService.LogQueries;
import com.colinalworth.gwt.viola.service.JobService.SourceProjectQueries;
import com.colinalworth.gwt.viola.service.UserService.SessionQueries;
import com.colinalworth.gwt.viola.service.UserService.UserQueries;
import com.google.inject.AbstractModule;
import rxf.couch.driver.CouchMetaDriver;
import rxf.couch.guice.CouchModuleBuilder;

import java.util.Date;

public class ViolaModule extends AbstractModule {


	@Override
	protected void configure() {
//		System.setProperty("user.timezone", "GMT");
		CouchMetaDriver.gson(null);
		CouchMetaDriver.builder(CouchMetaDriver.builder().registerTypeAdapter(Date.class, new DateTypeAdapter()));

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
