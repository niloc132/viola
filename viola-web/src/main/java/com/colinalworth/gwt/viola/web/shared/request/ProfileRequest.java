package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ProfileRequest {
	void getProfile(String id, AsyncCallback<UserProfile> callback);
	void updateProfile(UserProfile profile, AsyncCallback<UserProfile> callback);
}
