package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
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
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collections;
import java.util.List;

public class ProfileViewImpl {
	private final MockSimpleBeanEditorDriver<UserProfile, Editor<UserProfile>> driver = new MockSimpleBeanEditorDriver<>();
	private List<ProjectSearchResult> createdProjects = Collections.emptyList();

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
				.appendHtmlConstant("<h2>User Profile</h2>")
				.appendHtmlConstant("<label>").appendEscaped(notNull(profile.getUsername())).appendHtmlConstant("</label>")
				.appendHtmlConstant("<label>").appendEscaped(notNull(profile.getDisplayName())).appendHtmlConstant("</label>")
				.appendHtmlConstant("<label>").appendEscaped(notNull(profile.getOrganization())).appendHtmlConstant("</label>")
				.appendHtmlConstant("<label>").appendEscaped(notNull(profile.getDescription())).appendHtmlConstant("</label>");


		if (!createdProjects.isEmpty()) {
			sb.appendHtmlConstant("<div>")
					.appendHtmlConstant("<h2>Projects</h2>");

			for (ProjectSearchResult project : createdProjects) {
				if (project.getLatestCompiledId() == null) {
					continue;
				}
				sb.appendHtmlConstant("<div><a href='/example/" + UriUtils.encode(project.getLatestCompiledId()) + "/'>")
						.appendEscaped(project.getTitle())
						.appendHtmlConstant("</a>")
						.appendEscaped(notNull(project.getDescription()))
						.appendHtmlConstant("</div>");
			}


			sb.appendHtmlConstant("</div>");
		}

		sb.appendHtmlConstant("</div>");
		return sb.toSafeHtml();

	}

	private String notNull(String string) {
		if (string == null) {
			return "";
		}
		return string;
	}

	public void setCreatedProjects(List<ProjectSearchResult> projects) {
		createdProjects = projects;
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
