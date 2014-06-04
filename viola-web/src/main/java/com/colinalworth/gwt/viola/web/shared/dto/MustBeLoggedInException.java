package com.colinalworth.gwt.viola.web.shared.dto;

public class MustBeLoggedInException extends Exception {
	public MustBeLoggedInException() {
	}

	public MustBeLoggedInException(String message) {
		super(message);
	}
}
