package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.request.JobRequest;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateProjectPresenter extends AbstractPresenterImpl<CreateProjectView, CreateProjectPlace> {
	public interface CreateProjectView extends View<CreateProjectPresenter> {

	}
	public interface CreateProjectPlace extends Place {

	}
	@Inject
	Provider<JobRequest> jobRequest;
	@Inject
	PlaceManager placeManager;

	public void createWithNameAndDescription(final String title, final String description) {
		jobRequest.get().createProject(new AsyncCallback<Project>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
			@Override
			public void onSuccess(Project result) {
				result.title = title;
				result.description = description;
				jobRequest.get().saveProject(result, new AsyncCallback<Project>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(caught.getMessage());
					}
					@Override
					public void onSuccess(Project result) {
						ProjectEditorPlace next = placeManager.create(ProjectEditorPlace.class);
						next.setId(result._id);

						placeManager.submit(next);
					}
				});
			}
		});
	}
}
