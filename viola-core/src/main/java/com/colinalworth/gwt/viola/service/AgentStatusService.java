package com.colinalworth.gwt.viola.service;

import com.colinalworth.gwt.viola.entity.AgentStatus;
import com.colinalworth.gwt.viola.entity.AgentStatus.State;
import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.google.inject.Inject;
import rxf.server.CouchService;

import java.util.List;

public class AgentStatusService {
	public interface AgentStatusQueries extends CouchService<AgentStatus> {
		@View(map = "function(doc) {" +
						"emit(doc.state, doc);" +
					"}")
		List<AgentStatus> getAgentsInState(@Keys State... state);

		@View(map = "function(doc) {" +
						"if (doc.state == 'IDLE')" +
							"emit(doc.idleTimeMillis, doc);" +
					"}")
		List<AgentStatus> getAgentsIdleMoreThan(@StartKey int millis);
	}

	public interface CompiledProjectQueries extends CouchService<CompiledProject> {
		@View(map = "function(doc) {" +
						"emit(doc.agentId, doc);" +
					"}")
		List<CompiledProject> getProjectAgentIsCompiling(String agentId);
	}

	@Inject AgentStatusQueries queries;
	@Inject CompiledProjectQueries jobs;

	public List<AgentStatus> getAgentsInState(AgentStatus.State... state) {
		return queries.getAgentsInState(state);
	}

	public List<AgentStatus> getAgentsIdleMoreThan(int millis) {
		return queries.getAgentsIdleMoreThan(millis);
	}

	public void requestShutdown(AgentStatus agent) {
		agent.setShutdownRequested(true);
		queries.persist(agent);
	}

	public void markStuck(AgentStatus agent) {
		agent.setState(State.STUCK);
		queries.persist(agent);

		// doesn't currently run since only IDLE agents get mark stuck
//		List<CompiledProject> projectAgentIsCompiling = jobs.getProjectAgentIsCompiling(agent.getId());
//		for (CompiledProject proj : projectAgentIsCompiling) {
//			proj.setStatus(Status.STUCK);
//			jobs.persist(proj);
//		}
	}
}
