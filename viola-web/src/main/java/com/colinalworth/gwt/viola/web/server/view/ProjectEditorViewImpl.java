package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.View;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.testing.MockSimpleBeanEditorDriver;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.UriUtils;

public class ProjectEditorViewImpl extends AbstractServerView<ProjectEditorPresenter> implements ProjectEditorView {
	private View<?> codeEd;
	private String activeFile;
	private MockSimpleBeanEditorDriver<Project, ? extends Editor<? super Project>> driver = new MockSimpleBeanEditorDriver<>();

	@Override
	public AcceptsView getCodeEditorSlot() {
		return new AcceptsView() {
			@Override
			public void setView(View view) {
				codeEd = view;
			}
		};
	}

	@Override
	public SimpleBeanEditorDriver<Project, ?> getDriver() {
		return driver;
	}

	@Override
	public void setCurrentCompiled(String compiledId, String url) {
		//TODO
	}

	@Override
	public void setActiveFile(String activeFile) {
		this.activeFile = activeFile;
	}

	@Override
	public void showProgress(CompiledProjectStatus status) {
		//no op, server doesn't get updates on compiled status
	}

	@Override
	public void setEditable(boolean editable) {
		//no op, server generated content never editable
	}

	@Override
	public SafeHtml asSafeHtml() {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		sb.appendHtmlConstant("<div>");
		//TODO proj details

		Project project = driver.getObject();
		if (project != null && project.getFiles() != null) {
			sb.appendHtmlConstant("<div>");

			sb.appendHtmlConstant("<div>");
			sb.appendHtmlConstant("<div>");
			sb.appendEscaped("Project Details");
			sb.appendHtmlConstant("</div>");
			sb.appendEscaped("Name: " + project.getTitle());
			sb.appendHtmlConstant("<br/>");
			sb.appendEscaped("Description: " + project.getDescription());
			sb.appendHtmlConstant("</div>");

			sb.appendHtmlConstant("<div>");
			sb.appendHtmlConstant("<div>").appendEscaped("Project Files").appendHtmlConstant("</div>");
			for (String file : project.getFiles()) {
				sb.appendHtmlConstant("<a href='" + UriUtils.fromString("/proj/" + project.getId() + "/" + file).asString() + "'>");
				sb.appendEscaped(file.substring(file.lastIndexOf("/") + 1));
				sb.appendHtmlConstant("</a>");
			}
			sb.appendHtmlConstant("</div>");

			sb.appendHtmlConstant("</div>");
		}

		if (codeEd != null) {
			sb.append(codeEd.asSafeHtml());
		}

		return sb.appendHtmlConstant("</div>").toSafeHtml();
	}
}
