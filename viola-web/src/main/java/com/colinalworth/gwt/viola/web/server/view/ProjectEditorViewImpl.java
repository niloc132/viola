package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.View;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import java.util.List;

public class ProjectEditorViewImpl extends AbstractServerView<ProjectEditorPresenter> implements ProjectEditorView {
	private View<?> codeEd;
	private List<String> fileList;
	private String activeFile;

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
	public AcceptsView getRunningExampleSlot() {
		return new AcceptsView() {
			@Override
			public void setView(View view) {
				//ignore
			}
		};
	}

	@Override
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	@Override
	public void setActiveFile(String activeFile) {
		this.activeFile = activeFile;
	}

	@Override
	public void showProgress(CompiledProjectStatus status) {
		//no op
	}

	@Override
	public SafeHtml asSafeHtml() {
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		sb.appendHtmlConstant("<div>");
		//TODO proj details

		if (fileList != null) {
			sb.appendHtmlConstant("<div>");
			sb.appendHtmlConstant("<div>").appendEscaped("Project Files").appendHtmlConstant("</div>");
			for (String file : fileList) {
//				sb.appendHtmlConstant("<a href='/proj/").appendEscaped(file).appendHtmlConstant("'>")
//						.appendEscaped(file.substring(file.lastIndexOf("/") + 1)).appendHtmlConstant("</a>");
			}
			sb.appendHtmlConstant("</div>");
		}

		if (codeEd != null) {
			sb.append(codeEd.asSafeHtml());
		}

		return sb.appendHtmlConstant("</div>").toSafeHtml();
	}
}
