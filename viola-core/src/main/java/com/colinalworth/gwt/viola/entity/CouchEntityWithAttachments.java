package com.colinalworth.gwt.viola.entity;

import java.util.Collections;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import rxf.server.Attachment;

public class CouchEntityWithAttachments extends CouchEntity {
	@SerializedName("_attachments")
	private Map<String, Attachment> attachments;

	public Map<String, Attachment> getAttachments() {
		return Collections.unmodifiableMap(attachments);
	}

	public void removeAttachment(String filename) {
		assert attachments.containsKey(filename);
		attachments.remove(filename);
	}
}
