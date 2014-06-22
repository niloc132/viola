package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.shared.dto.MustBeLoggedInException;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.rpq.client.AsyncService.Throws;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ProfileRequest {
	void getProfile(String id, AsyncCallback<UserProfile> callback);

	@Throws(MustBeLoggedInException.class)
	void updateProfile(UserProfile profile, AsyncCallback<UserProfile> callback);

	@Throws(MustBeLoggedInException.class)
	void getCompileCountToday(AsyncCallback<Integer> callback);
}
