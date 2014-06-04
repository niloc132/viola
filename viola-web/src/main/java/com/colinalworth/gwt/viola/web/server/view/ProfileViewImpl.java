package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfileView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.testing.MockSimpleBeanEditorDriver;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Widget;

public class ProfileViewImpl {
	private final MockSimpleBeanEditorDriver<UserProfile, Editor<UserProfile>> driver = new MockSimpleBeanEditorDriver<>();

	protected ProfileViewImpl() {
		assert !GWT.isClient() : "Can't create server view on the client";
	}

	public final Widget asWidget() {
		assert false : "asWidget should not be called from server code";
		return null;
	}

	public SimpleBeanEditorDriver<UserProfile, ?> getDriver() {
		return driver;
	}

	public SafeHtml asSafeHtml() {
		UserProfile profile = driver.getObject();
		SafeHtmlBuilder sb = new SafeHtmlBuilder()
				.appendHtmlConstant("<div>")
				.appendHtmlConstant("<h1>User Profile</h1>")
				.appendHtmlConstant("<label>").appendEscaped(profile.getUsername()).appendHtmlConstant("</label>")
				.appendHtmlConstant("<label>").appendEscaped(profile.getDisplayName()).appendHtmlConstant("</label>")
				.appendHtmlConstant("<label>").appendEscaped(profile.getOrganization()).appendHtmlConstant("</label>")
				.appendHtmlConstant("<label>").appendEscaped(profile.getDescription()).appendHtmlConstant("</label>");

		//TODO user's public projects


		sb.appendHtmlConstant("</div>");
		return sb.toSafeHtml();

	}

	public static class View extends ProfileViewImpl implements ProfileView {
		@Override
		public void setPresenter(ProfilePresenter presenter) {
			//no-op, server can't interact
		}

		@Override
		public void setCanEdit(boolean canEdit) {
			//no-op, can't edit in readonly view
		}
	}

	public static class Edit extends ProfileViewImpl implements ProfileEditorView {
		@Override
		public void setPresenter(ProfileEditorPresenter presenter) {
			//no-op, server can't interact
		}

		@Override
		public void setCompiledTodayCount(int result) {
			//no-op, server will never render individual details
		}
	}
}
