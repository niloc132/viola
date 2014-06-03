package com.colinalworth.gwt.viola.web.client.events;

import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent.ProfileUpdateHandler;
import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class ProfileUpdateEvent extends GwtEvent<ProfileUpdateHandler> {
	public static final Type<ProfileUpdateHandler> TYPE = new Type<ProfileUpdateHandler>();
	private final UserProfile userProfile;

	public ProfileUpdateEvent(UserProfile userProfile) {

		this.userProfile = userProfile;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	@Override
	public Type<ProfileUpdateHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	public void dispatch(ProfileUpdateHandler handler) {
		handler.onProfileUpdate(this);
	}

	public interface ProfileUpdateHandler extends EventHandler {
		void onProfileUpdate(ProfileUpdateEvent event);
	}

	public interface HasProfileUpdateHandlers {
		HandlerRegistration addProfileUpdateHandler(ProfileUpdateHandler handler);
	}
}