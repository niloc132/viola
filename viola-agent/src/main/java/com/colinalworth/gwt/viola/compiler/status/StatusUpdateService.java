package com.colinalworth.gwt.viola.compiler.status;

import com.colinalworth.gwt.viola.entity.AgentStatus;
import com.colinalworth.gwt.viola.entity.AgentStatus.State;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import rxf.server.CouchService;
import rxf.shared.CouchTx;

import java.util.Date;

@Singleton
public class StatusUpdateService {
	public interface StatusUpdateQueries extends CouchService<AgentStatus> {

	}
	@Inject StatusUpdateQueries queries;

	private volatile AgentStatus me;
	private long lastBusy = System.currentTimeMillis();

	//TODO factor out manager calls like these to a new class
	public AgentStatus create(String type) {
		AgentStatus agentStatus = new AgentStatus();
		agentStatus.setServerType(type);
		agentStatus.setState(State.STARTING);
		CouchTx tx = queries.persist(agentStatus);

		return queries.find(tx.id());
	}
	public AgentStatus persist(AgentStatus status) {
		return queries.find(queries.persist(status).id());
	}
	//end manager calls

	public synchronized void register(String id) {
		me = queries.find(id);
		if (me == null) {
			throw new IllegalStateException("Error registering: " + id);
		}
		me.setStartup(new Date());
		queries.persist(me);
		notifyIdle();
	}

	public void notifyIdle() {
		if (me.getState() != State.IDLE) {
			lastBusy = System.currentTimeMillis();
		}
		update(State.IDLE);
	}
	public void notifyWorking() {
		update(State.WORKING);
	}
	public void notifyShuttingDown() {
		update(State.SHUTTING_DOWN);
	}
	public void notifyStopped() {
		update(State.STOPPED);
	}

	private synchronized void update(State state) {
		assert me != null : "Can't update if not yet registered";
		AgentStatus agentStatus = queries.find(me.getId());
		if (agentStatus == null) {
			//can't get a result from db, either we've lost network or it is too busy
			//either way we'll be shut down remotely if necessary, so stay up and
			//hope the connection is restored

			//missing this update will result in a hole in the logs or the wrong state,
			//but the distance between updates is close enough that we shouldn't lose
			//much
			return;
		}
		me = agentStatus;
		me.setState(state);
		me.setIdleTimeMillis(getTimeIdle());
		me.setLastHeardFrom(new Date());
		CouchTx tx = queries.persist(me);
		if (!tx.ok()) {
			throw new IllegalStateException("Error updating: " + tx.error());
		}
	}


	public int getTimeIdle() {
		return (int) (System.currentTimeMillis() - lastBusy);
	}

	public synchronized boolean shouldShutdown() {
		assert me != null : "Can't check for shutdown if not yet registered";
		AgentStatus agentStatus = queries.find(me.getId());
		if (agentStatus == null) {
			//can't get a result from db, either we've lost network or it is too busy
			//either way we'll be shut down remotely if necessary, so stay up and
			//hope the connection is restored
			return false;
		}
		me = agentStatus;
		return me.isShutdownRequested();
	}

	public String getAgentId() {
		assert me != null : "Can't return id if not yet registered";
		return me.getId();
	}
}
