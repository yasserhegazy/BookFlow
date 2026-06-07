package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BookFlowException extends RuntimeException {

	private final ErrorCode errorCode;
	private final HttpStatus status;

	protected BookFlowException(String message, ErrorCode errorCode, HttpStatus status) {
		super(message);
		this.errorCode = errorCode;
		this.status = status;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
