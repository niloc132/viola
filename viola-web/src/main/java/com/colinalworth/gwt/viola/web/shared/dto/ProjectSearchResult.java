package com.colinalworth.gwt.viola.web.shared.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProjectSearchResult implements IsSerializable {
	public String _id;
	public String _rev;

	public String latestCompiledId;

	public String title;
	public String description;
}
