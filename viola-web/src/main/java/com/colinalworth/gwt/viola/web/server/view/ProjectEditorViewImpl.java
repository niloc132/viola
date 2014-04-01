package com.colinalworth.gwt.viola.web.server.view;

import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.mvp.AbstractPresenterImpl.AbstractServerView;
import com.colinalworth.gwt.viola.web.shared.mvp.AcceptsView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import java.util.List;

public class ProjectEditorViewImpl extends AbstractServerView<ProjectEditorPresenter> implements ProjectEditorView {

	@Override
	public AcceptsView getCodeEditorSlot() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setFileList(List<String> fileList) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setActiveFile(String activeFile) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void showProgress(CompiledProjectStatus status) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public SafeHtml asSafeHtml() {
		return SafeHtmlUtils.fromString("Gotta edit some code");
	}
}
