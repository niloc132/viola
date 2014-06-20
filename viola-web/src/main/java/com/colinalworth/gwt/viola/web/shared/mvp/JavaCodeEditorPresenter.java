package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.JavaCodeEditorPresenter.JavaCodeEditorView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.request.JobRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class JavaCodeEditorPresenter extends AbstractPresenterImpl<JavaCodeEditorView, ProjectEditorPlace> implements Presenter<ProjectEditorPlace> {
	@Inject
	Provider<JobRequest> jobRequest;
	@Override
	public void go(final AcceptsView parent, ProjectEditorPlace place) {
		getView().setPresenter(this);
		setCurrentPlace(place);
		jobRequest.get().getAttachment(place.getId(), place.getActiveFile(), new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(String result) {
				parent.setView(getView());
				getView().setValue(result);
			}
		});
	}

	@Override
	public void stop() {
		save();
	}

	public void save() {
		jobRequest.get().attach(getCurrentPlace().getId(), getCurrentPlace().getActiveFile(), getView().getValue(), new AsyncCallback<Project>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}
			@Override
			public void onSuccess(Project result) {
				//TODO
			}
		});
	}

	public interface JavaCodeEditorView extends View<JavaCodeEditorPresenter> {
		String getValue();
		void setValue(String code);
	}
}
