package com.colinalworth.gwt.viola.service;

import rxf.server.CouchService;

import com.colinalworth.gwt.viola.entity.AgentStatus;
import com.colinalworth.gwt.viola.entity.AgentStatus.State;
import com.google.inject.Inject;

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

	@Inject AgentStatusQueries queries;

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
}
