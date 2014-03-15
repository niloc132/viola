package com.colinalworth.gwt.viola.service;

import rxf.server.CouchService;

import com.colinalworth.gwt.viola.entity.User;
import com.google.inject.Inject;

public class UserService {
	public interface UserQueries extends CouchService<User> {

	}

	@Inject UserQueries queries;


}
