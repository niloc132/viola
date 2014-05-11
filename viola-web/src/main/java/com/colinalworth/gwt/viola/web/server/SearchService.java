package com.colinalworth.gwt.viola.web.server;

import com.colinalworth.gwt.viola.entity.CompiledProject;
import com.colinalworth.gwt.viola.service.JobService;
import rxf.server.CouchService;

import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.web.shared.dto.ProjectSearchResult;
import com.google.inject.Inject;

import java.util.List;

public class SearchService {

	public interface SearchQueries extends CouchService<SourceProject> {

		@View(map = "function(doc) {" +
				"emit(doc.title, doc);" +
				"emit(doc.description, doc);" +
				"}")
		List<ProjectSearchResult> search(@StartKey String query, @StartKeyDocId String offsetId, @Limit int limit);
	}
	@Inject
	SearchQueries queries;

	@Inject
	JobService jobService;

	public List<ProjectSearchResult> search(String query, String lastId, Integer limit) {
		if ("".equals(lastId)) {
			lastId = null;
		}
		List<ProjectSearchResult> search = queries.search(query, lastId, Math.max(limit, 20));
		//wow, not sure this could be worse if i tried
		for (int i = search.size() - 1; i >= 0; i--) {
			for (int j = 0; j < i; j++) {
				if (search.get(i).getId().equals(search.get(j).getId())) {
					search.remove(i--);
				}
			}
		}
		for (int i = 0; i < search.size(); i++) {
			List<CompiledProject> compiled = jobService.getCompiledOuput(jobService.find(search.get(i).getId()));
			search.get(i).setLatestCompiledId(compiled == null || compiled.isEmpty() ? null : compiled.get(0).getId());
		}
		return search;
	}
}
