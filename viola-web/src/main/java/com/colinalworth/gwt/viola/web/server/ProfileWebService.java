package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.entity.User;
import com.colinalworth.gwt.viola.service.JobService;
import com.colinalworth.gwt.viola.service.UserService;
import com.colinalworth.gwt.viola.web.shared.dto.MustBeLoggedInException;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.google.inject.Inject;

public class ProfileWebService {

	@Inject
	UserService userService;

	@Inject
	JobService jobService;
	@Inject
	SessionService sessionService;

	public UserProfile getProfile(String id) {
		User u = userService.findUserWithId(id);
		UserProfile profile = new UserProfile();
		profile.setId(u.getId());
		profile.setDescription(u.getDescription());
		profile.setDisplayName(u.getDisplayName());
	 	profile.setOrganization(u.getOrganization());
		profile.setUsername(u.getUsername());

		return profile;
	}
	public UserProfile updateProfile(UserProfile profile) throws MustBeLoggedInException {
		if (!sessionService.getThreadLocalUserId("updateProfile").equals(profile.getId())) {
			throw new MustBeLoggedInException("Can't update a profile that isn't yours");
		}
		User u = userService.findUserWithId(profile.getId());

		u.setDisplayName(profile.getDisplayName());
		u.setUsername(profile.getUsername());
		u.setDescription(profile.getDescription());
		u.setOrganization(profile.getOrganization());

		userService.updateUser(u);

		return getProfile(profile.getId());
	}

	public int getCompileCountToday() throws MustBeLoggedInException {
		String userId = sessionService.getThreadLocalUserId("getCompileCountToday");
		if (userId == null) {
			throw new MustBeLoggedInException("Can't check compile count without being logged in");
		}
		return jobService.getCompileCountTodayForUser(userId);
	}
}
