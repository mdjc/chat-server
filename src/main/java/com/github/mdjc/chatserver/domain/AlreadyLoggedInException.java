package com.github.mdjc.chatserver.domain;

public class AlreadyLoggedInException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AlreadyLoggedInException(String message) {
		super(message);
	}
}
