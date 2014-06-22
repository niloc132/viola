package com.colinalworth.gwt.viola.web.shared.mvp;

import com.colinalworth.gwt.places.shared.Place;
import com.colinalworth.gwt.places.shared.PlaceManager;
import com.colinalworth.gwt.viola.web.shared.dto.Project;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectPlace;
import com.colinalworth.gwt.viola.web.shared.mvp.CreateProjectPresenter.CreateProjectView;
import com.colinalworth.gwt.viola.web.shared.mvp.ProjectEditorPresenter.ProjectEditorPlace;
import com.colinalworth.gwt.viola.web.shared.request.JobRequest;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateProjectPresenter extends AbstractPresenterImpl<CreateProjectView, CreateProjectPlace> {
	public interface CreateProjectView extends View<CreateProjectPresenter> {
		void startWith(String title, String description);
	}
	public interface CreateProjectPlace extends Place {
		String getCopy();
		void setCopy(String id);
	}
	@Inject
	Provider<JobRequest> jobRequest;

	@Inject
	PlaceManager placeManager;

	@Override
	public void go(final AcceptsView parent, final CreateProjectPlace place) {
		if (place.getCopy() == null) {
			super.go(parent, place);
		} else {
			jobRequest.get().getProject(place.getCopy(), new AsyncCallback<Project>() {
				@Override
				public void onFailure(Throwable throwable) {
					getErrors().report(throwable.getMessage());
				}

				@Override
				public void onSuccess(Project project) {
					CreateProjectPresenter.super.go(parent, place);
					getView().startWith(project.getTitle(), project.getDescription());
				}
			});
		}
	}

	public void createWithNameAndDescription(final String title, final String description) {
		AsyncCallback<Project> callback = new AsyncCallback<Project>() {
			@Override
			public void onFailure(Throwable caught) {
				getErrors().report(caught.getMessage());
			}

			@Override
			public void onSuccess(Project result) {
				result.setTitle(title);
				result.setDescription(description);
				jobRequest.get().saveProject(result, new AsyncCallback<Project>() {
					@Override
					public void onFailure(Throwable caught) {
						getErrors().report(caught.getMessage());
					}

					@Override
					public void onSuccess(Project result) {
						ProjectEditorPlace next = placeManager.create(ProjectEditorPlace.class);
						next.setId(result.getId());

						placeManager.submit(next);
					}
				});
			}
		};
		if (getCurrentPlace().getCopy() != null) {
			jobRequest.get().cloneProject(getCurrentPlace().getCopy(), callback);
		} else {
			jobRequest.get().createProject(callback);
		}
	}

	public void back() {
		placeManager.back();
	}
}
