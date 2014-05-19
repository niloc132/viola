package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfilePresenter.ProfileView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ProfileViewImpl extends AbstractClientView<ProfilePresenter>
		implements ProfileView, Editor<UserProfile> {
	interface Driver extends SimpleBeanEditorDriver<UserProfile, ProfileViewImpl> {}

	private final Driver driver = GWT.create(Driver.class);

	Label username = new Label();
	Label displayName = new Label();
	Label organization = new Label();
	Label description = new Label();

	private ToolBar toolBar = new ToolBar();

	public ProfileViewImpl() {
		ContentPanel panel = new ContentPanel();
		panel.setHeadingText("User Profile");

		VerticalLayoutContainer outer = new VerticalLayoutContainer();

		toolBar.add(new TextButton("Edit", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent selectEvent) {
				getPresenter().edit();
			}
		}));
		outer.add(toolBar, new VerticalLayoutData(1, -1));

		FlowLayoutContainer container = new FlowLayoutContainer();

		container.add(username);
		container.add(displayName);
		container.add(organization);
		container.add(description);

		outer.add(container, new VerticalLayoutData(1, 1));
		panel.setWidget(outer);

		driver.initialize(this);
		initWidget(panel);
	}

	@Override
	public SimpleBeanEditorDriver<UserProfile, ?> getDriver() {
		return driver;
	}

	@Override
	public void setCanEdit(boolean canEdit) {
		toolBar.setVisible(canEdit);
	}
}
