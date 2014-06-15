package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.sencha.gxt.cell.core.client.form.TextAreaInputCell.Resizable;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;

public class CreateProjectViewImpl extends AbstractClientView<CreateProjectPresenter> implements CreateProjectView {

	private final TextField title = new TextField();
	private final TextArea description = new TextArea();

	public CreateProjectViewImpl() {
		ContentPanel panel = new ContentPanel();
		panel.setHeadingText("Create a new project");
		FlowLayoutContainer container = new FlowLayoutContainer();

		container.add(new FieldLabel(title, "Project Title"));
		container.add(new FieldLabel(description, "Description"));
		description.setResizable(Resizable.BOTH);
		description.setHeight(100);

		panel.addButton(new TextButton("Create", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent selectEvent) {
				getPresenter().createWithNameAndDescription(title.getValue(), description.getValue());
			}
		}));

		panel.setWidget(container);
		initWidget(panel);
	}

	@Override
	public void startWith(String title, String description) {
		this.title.setValue(title);
		this.description.setValue(description);
	}
}
