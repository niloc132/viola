package com.colinalworth.gwt.viola.web.client.view;

import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractClientView;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
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
		panel.setShadow(true);
		panel.setHeadingText("Create a new project");
		FlowLayoutContainer container = new FlowLayoutContainer();

		container.add(new FieldLabel(title, "Project Title"));
		container.add(new FieldLabel(description, "Description"));
		description.setHeight(150);

		panel.addButton(new TextButton("Back", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				getPresenter().back();
			}
		}));
		panel.addButton(new TextButton("Create", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent selectEvent) {
				getPresenter().createWithNameAndDescription(title.getValue(), description.getValue());
			}
		}));

		panel.add(container, new MarginData(20));

		CenterLayoutContainer center = new CenterLayoutContainer();
		center.setWidget(panel);

		initWidget(center);
	}

	@Override
	public void startWith(String title, String description) {
		this.title.setValue(title);
		this.description.setValue(description);
	}
}
