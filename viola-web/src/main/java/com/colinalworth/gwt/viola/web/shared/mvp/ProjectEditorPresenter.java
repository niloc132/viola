package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent;
import com.colinalworth.gwt.viola.web.client.events.ProfileUpdateEvent.ProfileUpdateHandler;
import com.colinalworth.gwt.viola.web.client.ioc.UserId;
import com.colinalworth.gwt.viola.web.shared.dto.CompiledProjectStatus;
import com.colinalworth.gwt.viola.web.shared.dto.CompilerLogNode;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorView;
import com.colinalworth.gwt.viola.web.shared.request.JobRequest;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class ProjectEditorPresenter extends AbstractPresenterImpl<ProjectEditorView, ProjectEditorPlace> {

	private HandlerRegistration registration;

	public interface ProjectEditorView extends View<ProjectEditorPresenter>, Editor<Project> {

		AcceptsView getCodeEditorSlot();

		SimpleBeanEditorDriver<Project, ?> getDriver();

		void setCodeVisible(boolean visible);

		void setActiveFile(String activeFile);

		void setCurrentCompiled(String compiledId, String url);

		void showProgress(CompiledProjectStatus status);

		void setEditable(boolean editable);

		void setLogTree(CompilerLogNode compilerLogNode);
	}
	public interface ProjectEditorPlace extends Place {
		String getId();
		void setId(String id);

		String getActiveFile();
		void setActiveFile(String activeFile);

		//TODO support boolean
		/** null or 'true' by default, false means hide the code/tree */
		String getCode();
		void setCode(String code);
	}

	@Inject
	Provider<JobRequest> jobRequest;
	@Inject
	JavaCodeEditorPresenter javaEditor;
	@Inject
	PlaceManager placeManager;

	@Inject
	@Named("compiledServer")
	String compiledServer;

	@UserId
	@Inject
	Provider<String> userIdProvider;

	@Inject
	EventBus eventBus;

	private SimpleBeanEditorDriver<Project, ?> driver;
	private Object editor;

	private Project current;

	@Override
	public void go(AcceptsView parent, ProjectEditorPlace place) {
		super.go(parent, place);
		driver = getView().getDriver();
		getView().setCodeVisible(!"false".equals(place.getCode()));
		if (current != null && place.getId().equals(current.getId())) {
			//TODO reuse presenters
			updateWithProject(current);
			return;
		}
		registration = eventBus.addHandler(ProfileUpdateEvent.TYPE, new ProfileUpdateHandler() {
			@Override
			public void onProfileUpdate(ProfileUpdateEvent event) {
				updateUserId();
			}
		});
		jobRequest.get().getProject(place.getId(), new AsyncCallback<Project>(){
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(Project result) {
				updateWithProject(result);
			}
		});
	}

	@Override
	public void stop() {
		super.stop();
		current = null;
		if (registration != null) {
			registration.removeHandler();
			registration = null;
		}
	}

	@Override
	public void cancel() {
//		super.cancel();
//		if (registration != null) {
//			registration.removeHandler();
//			registration = null;
//		}
		assert false : "cancel() call not expected, this presenter finishes start synchronously";
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
		updateUserId();
	}

	public boolean tryLoadFile(String path) {
		if (path == null) {
			return false;
		}
		if (path.equals(getCurrentPlace().getActiveFile())) {
			return true;
		}
		if (editor != null) {//TODO track editor better
			String maybe = javaEditor.maybeStop();
			if (maybe != null) {
				if (!Window.confirm(maybe)) {
					return false;
				}
			}
			javaEditor.stop();
		}
		ProjectEditorPlace next = placeManager.create(ProjectEditorPlace.class);
		next.setId(getCurrentPlace().getId());
		next.setCode(null);
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
				getErrors().report(caught.getMessage());
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
		if (editor != null) {//TODO track editor better
			javaEditor.save();
		}
		//save operation will be batched with this next request as well
		jobRequest.get().build(getCurrentPlace().getId(), new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				poll();
				getView().setCurrentCompiled(null, null);
			}
		});
	}

	public void updateCompiledId() {
		jobRequest.get().getCompiledId(getCurrentPlace().getId(), new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
				if (result != null) {
					getView().setCurrentCompiled(result, compiledServer + "/compiled/" + result + "/");
				}
			}
		});
	}

	public void cloneProject() {
		CreateProjectPlace place = placeManager.create(CreateProjectPlace.class);
		place.setCopy(getCurrentPlace().getId());
		placeManager.submit(place);
	}

	public void createFile(final String path) {
		if (current.getFiles().contains(path)) {
			//select existing, don't create new
			tryLoadFile(path);
		}
		jobRequest.get().attach(getCurrentPlace().getId(), path, "", new AsyncCallback<Project>() {
			@Override
			public void onFailure(Throwable throwable) {
				getErrors().report(throwable.getMessage());
			}

			@Override
			public void onSuccess(Project project) {
				tryLoadFile(path);
				updateWithProject(project);
			}
		});
	}

	public void deleteFile(String path) {
		javaEditor.stop();
		jobRequest.get().delete(getCurrentPlace().getId(), path, new AsyncCallback<Project>() {
			@Override
			public void onFailure(Throwable throwable) {
				getErrors().report(throwable.getMessage());
			}

			@Override
			public void onSuccess(Project project) {
				tryLoadFile(project.getFiles().get(0));//server asserts that all projects have at least one file
				updateWithProject(project);
			}
		});
	}

	private void poll() {
		jobRequest.get().checkStatus(getCurrentPlace().getId(), new AsyncCallback<CompiledProjectStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(CompiledProjectStatus result) {

				getView().showProgress(result);

				switch (result) {
					case FAILED:
					case STUCK:
					case COMPLETE:
						updateCompiledId();
						break;
					default:
						poll();
				}
			}
		});
	}


	private void updateUserId() {
		if (current != null) {
			getView().setEditable(isEditable());
		}
	}

	private boolean isEditable() {
		return current.getAuthorId().equals(userIdProvider.get());
	}

	public void loadErrorLog(String lastCompiledId) {
		jobRequest.get().getLog(lastCompiledId, new AsyncCallback<CompilerLogNode>() {
			@Override
			public void onFailure(Throwable throwable) {
				getErrors().report(throwable.getMessage());
			}

			@Override
			public void onSuccess(CompilerLogNode compilerLogNode) {
				getView().setLogTree(compilerLogNode);
			}
		});
	}

	public void toggleCode(boolean codeVisible) {
		//if codeVisible is false, make sure that the code is *not* false
		if (codeVisible == ("false".equals(getCurrentPlace().getCode()))) {
			ProjectEditorPlace next = placeManager.create(ProjectEditorPlace.class);
			next.setId(getCurrentPlace().getId());
			next.setCode(codeVisible ? null : "false");
			next.setActiveFile(getCurrentPlace().getActiveFile());
			placeManager.submit(next);
		}
	}

	@Override
	public boolean equals(Object obj) {
		//always return true for another of the same type so that we dont rebuild
		return obj instanceof ProjectEditorPresenter;
	}
}
