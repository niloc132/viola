package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.colinalworth.gwt.viola.web.shared.request.JobRequest;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

public class ProjectEditorPresenter extends AbstractPresenterImpl<ProjectEditorView, ProjectEditorPlace> {

	public interface ProjectEditorView extends View<ProjectEditorPresenter> {

		AcceptsView getCodeEditorSlot();

		void setFileList(List<String> fileList);
		void setActiveFile(String activeFile);

		void showProgress(CompiledProjectStatus status);
	}
	public interface ProjectEditorPlace extends Place {
		String getId();
		void setId(String id);

		String getActiveFile();
		void setActiveFile(String activeFile);
	}

	@Inject
	Provider<JobRequest> jobRequest;
	@Inject
	JavaCodeEditorPresenter javaEditor;
	@Inject
	PlaceManager placeManager;

	private Object editor;

	private Project current;

	@Override
	public void go(AcceptsView parent, ProjectEditorPlace place) {
		super.go(parent, place);
		if (current != null && place.getId().equals(current._id)) {
			//TODO reuse presenters
			updateWithProject(current);
			return;
		}
		jobRequest.get().getProject(place.getId(), new AsyncCallback<Project>(){
			@Override
			public void onFailure(Throwable caught) {
				GWT.reportUncaughtException(caught);
			}

			@Override
			public void onSuccess(Project result) {
				updateWithProject(result);
			}
		});
	}

	protected void updateWithProject(Project result) {
		current = result;
		getView().setFileList(result.files);
		if (getCurrentPlace().getActiveFile() != null) {
			getView().setActiveFile(getCurrentPlace().getActiveFile());
			javaEditor.go(getView().getCodeEditorSlot(), getCurrentPlace());
			editor = javaEditor;
		}
	}

	public boolean tryLoadFile(String path) {
		if (path == null) {
			return false;
		}
		if (path.equals(getCurrentPlace().getActiveFile())) {
			return true;
		}
		if (editor != null) {//TODO track editor better
			javaEditor.stop();
		}
		ProjectEditorPlace next = placeManager.create(ProjectEditorPlace.class);
		next.setId(getCurrentPlace().getId());
		next.setActiveFile(path);
		placeManager.submit(next);

		return true;
	}

	public void save() {
		if (editor != null) {//TODO track editor better
			javaEditor.save();
		}
	}

	public void compile() {
		jobRequest.get().build(getCurrentPlace().getId(), new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				poll();
			}
		});
	}

	private void poll() {
		jobRequest.get().checkStatus(getCurrentPlace().getId(), new AsyncCallback<CompiledProjectStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(CompiledProjectStatus result) {
				getView().showProgress(result);

				switch (result) {
					case FAILED:
					case STUCK:
					case COMPLETE:
						break;
					default:
						poll();
				}
			}
		});
	}

}
