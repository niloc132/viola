package com.colinalworth.gwt.viola.service;

import com.colinalworth.gwt.viola.entity.Session;
import com.colinalworth.gwt.viola.entity.User;
import com.google.inject.Inject;
import rxf.server.CouchService;
import rxf.shared.CouchTx;

import java.util.Arrays;
import java.util.List;

public class UserService {

	public interface UserQueries extends CouchService<User> {
		@View(map = "function(doc) {" +
						"emit([doc.identityServer, doc.identityData], doc);" +
					"}")
		List<User> findWithToken(@Key List<String> key);
	}

	public interface SessionQueries extends CouchService<Session> {

		@View(map = "function(doc) {" +
						"emit(doc.userId, doc)" +
					"}")
		List<Session> getUserSessions(String userId);
	}

	@Inject UserQueries userQueries;
	@Inject SessionQueries sessionQueries;


	public User findUserWithIdToken(String id_token, String provider) {
		List<User> userList = userQueries.findWithToken(Arrays.asList(provider, id_token));
		return userList.isEmpty() ? null : userList.get(0);
	}

	public User createUserWithToken(String id_token, String provider, String initialName) {
		User u = new User();
		u.setDisplayName(initialName);
		u.setIdentityServer(provider);
		u.setIdentityData(id_token);
		CouchTx tx = userQueries.persist(u);

		return userQueries.find(tx.id());
	}

	public User findUserWithId(String userId) {
		return userQueries.find(userId);
	}

	public void updateUser(User user) {
		userQueries.persist(user);
	}

	public String createSession(User user) {
		Session s = new Session();
		s.setLastAction("login");
		s.setSessionStarted((int) (System.currentTimeMillis()/1000));
		s.setLastActive((int) (System.currentTimeMillis()/1000));
		s.setUserId(user.getId());

		return sessionQueries.persist(s).id();
	}

	public User updateSession(String sessionId, String action) {
		Session s = sessionQueries.find(sessionId);
		s.setLastActive((int) (System.currentTimeMillis()/1000));
		s.setLastAction(action);
		sessionQueries.persist(s);

		return userQueries.find(s.getUserId());
	}

	public void deleteSession(String sessionId) {
		//TODO
	}

	public User getUserWithSession(String sessionId) {
		Session session = sessionQueries.find(sessionId);
		if (session == null) {
			return null;
		}

		return userQueries.find(session.getUserId());
	}
}
