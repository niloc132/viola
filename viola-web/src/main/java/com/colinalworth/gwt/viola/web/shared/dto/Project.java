package com.colinalworth.gwt.viola.web.shared.dto;

import java.util.List;

public class Project extends ProjectSearchResult {

	private List<String> files;


	public List<String> getFiles() {
		return files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}
}
