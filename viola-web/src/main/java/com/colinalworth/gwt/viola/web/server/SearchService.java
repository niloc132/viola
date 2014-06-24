package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.service.JobService;
import com.colinalworth.gwt.viola.web.shared.dto.ProfileSearchResult;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.google.inject.Inject;
import rxf.server.CouchService;

import java.util.ArrayList;
import java.util.List;

public class SearchService {

	public interface SearchQueries extends CouchService<SourceProject> {

		@View(map = "function(doc) {" +
					"emit(doc.title, doc);" +
					"emit(doc.description, doc);" +
				"}")
		List<ProjectSearchResult> search(@StartKey String query, @StartKeyDocId String offsetId, @Limit int limit);
		@View(map = "function(doc) {" +
					"emit(doc.authorId, doc);" +
				"}")
		List<ProjectSearchResult> searchByOwner(@Key String userId, @StartKeyDocId String offsetId, @Limit int limit);
	}
	@Inject
	SearchQueries queries;

	@Inject
	JobService jobService;

	public List<ProjectSearchResult> searchProjects(String query, String lastId, Integer limit) {
		List<ProjectSearchResult> search = queries.search(query, lastId, Math.max(limit, 20));
		return search;
	}

	public List<ProfileSearchResult> searchProfiles(String query, String lastId, Integer limit) {
		return new ArrayList<>();
	}

	public List<ProjectSearchResult> listProjectsByUser(String userId, String lastId, Integer limit) {
		List<ProjectSearchResult> search = queries.searchByOwner(userId, lastId, limit);
		return search;
	}
}
