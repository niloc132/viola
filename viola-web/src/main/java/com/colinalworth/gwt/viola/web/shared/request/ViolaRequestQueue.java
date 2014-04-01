package com.colinalworth.gwt.viola.web.shared.request;

import com.colinalworth.gwt.viola.web.server.JobWebService;
import com.colinalworth.gwt.viola.web.server.SearchService;
import com.colinalworth.rpq.client.RequestQueue;

public interface ViolaRequestQueue extends RequestQueue {
	@Service(SearchService.class)
	SearchRequest search();

	@Service(JobWebService.class)
	JobRequest job();
//
//  ServerHealthRequest serverHealth();
}
