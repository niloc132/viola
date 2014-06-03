package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.ExamplePresenter.ExamplePlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.colinalworth.gwt.viola.web.shared.request.JobRequest;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ProjectEditorPresenter extends AbstractPresenterImpl<ProjectEditorView, ProjectEditorPlace> {


	public interface ProjectEditorView extends View<ProjectEditorPresenter>, Editor<Project> {

		AcceptsView getCodeEditorSlot();
		AcceptsView getRunningExampleSlot();

		SimpleBeanEditorDriver<Project, ?> getDriver();

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

	@Inject
	ExamplePresenter examplePresenter;

	private SimpleBeanEditorDriver<Project, ?> driver;
	private Object editor;

	private Project current;

	@Override
	public void go(AcceptsView parent, ProjectEditorPlace place) {
		driver = getView().getDriver();

		super.go(parent, place);
		if (current != null && place.getId().equals(current.getId())) {
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
		driver.edit(result);
		if (getCurrentPlace().getActiveFile() != null && !"".equals(getCurrentPlace().getActiveFile())) {
			getView().setActiveFile(getCurrentPlace().getActiveFile());
			javaEditor.go(getView().getCodeEditorSlot(), getCurrentPlace());
			editor = javaEditor;
		}
		updateCompiledId();
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
		Project p = driver.flush();
		//save the project with its current rev
		jobRequest.get().saveProject(p, new AsyncCallback<Project>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(Project result) {
				updateWithProject(result);
			}
		});

		//then save the java file (attach doesn't care about rev incr)
		//TODO don't make the client care about revs, because that is stupid, and confusing

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
				updateCompiledId();
			}
		});
	}

	private void updateCompiledId() {
		jobRequest.get().getCompiledId(getCurrentPlace().getId(), new AsyncCallback<String>(){
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
				if (result != null) {
					ExamplePlace exPlace = placeManager.create(ExamplePlace.class);
					exPlace.setId(result);
					examplePresenter.go(getView().getRunningExampleSlot(), exPlace);
				}
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


	@Override
	public boolean equals(Object obj) {
		//always return true for another of the same type so that we dont rebuild
		return obj instanceof ProjectEditorPresenter;
	}
}
