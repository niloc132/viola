package com.colinalworth.gwt.viola.entity;

import com.google.gson.annotations.SerializedName;
import rxf.couch.Attachment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CouchEntityWithAttachments extends CouchEntity {
	@SerializedName("_attachments")
	private Map<String, Attachment> attachments = new HashMap<>();

	public Map<String, Attachment> getAttachments() {
		return Collections.unmodifiableMap(attachments);
	}

	public void removeAttachment(String filename) {
		assert attachments.containsKey(filename);
		attachments.remove(filename);
	}
}
