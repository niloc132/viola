package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.dto.UserProfile;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProfileEditorPresenter.ProfileEditorView;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.IntegerField;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;

public class ProfileEditorViewImpl extends AbstractClientView<ProfileEditorPresenter>
		implements ProfileEditorView, Editor<UserProfile> {
	interface Driver extends SimpleBeanEditorDriver<UserProfile, ProfileEditorViewImpl> {}

	private final Driver driver = GWT.create(Driver.class);

	TextField username = new TextField();
	TextField displayName = new TextField();
	TextField organization = new TextField();
	TextArea description = new TextArea();

	@Ignore
	IntegerField compiledTodayCount = new IntegerField();


	public ProfileEditorViewImpl() {
		ContentPanel panel = new ContentPanel();
		panel.setShadow(true);
		panel.setHeadingText("Your Profile");

		FlowLayoutContainer container = new FlowLayoutContainer();

		container.add(new FieldLabel(username, "username"));
		container.add(new FieldLabel(displayName, "display name"));
		container.add(new FieldLabel(organization, "organization"));
		container.add(new FieldLabel(description, "about me"));
		description.setHeight(100);
		compiledTodayCount.setReadOnly(true);
		compiledTodayCount.setEnabled(false);
		container.add(new FieldLabel(compiledTodayCount, "times compiled today"));

		panel.add(container, new MarginData(10));

		panel.addButton(new TextButton("Back", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				getPresenter().back();
			}
		}));
		panel.addButton(new TextButton("Save", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				getPresenter().save();
			}
		}));

		driver.initialize(this);

		CenterLayoutContainer center = new CenterLayoutContainer();
		center.setWidget(panel);

		initWidget(center);
	}

	@Override
	public SimpleBeanEditorDriver<UserProfile, ?> getDriver() {
		return driver;
	}

	@Override
	public void setCompiledTodayCount(int result) {
		compiledTodayCount.setValue(result);
	}
}
