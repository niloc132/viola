package com.colinalworth.gwt.viola.service;

import com.colinalworth.gwt.viola.entity.User;

import rxf.server.CouchService;

public class UserService {
	public interface UserQueries extends CouchService<User> {
		
	}
}
