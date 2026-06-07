package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BookFlowException {

	public ConflictException(String message) {
		super(message, ErrorCode.CONFLICT, HttpStatus.CONFLICT);
	}

	public ConflictException(String message, ErrorCode errorCode) {
		super(message, errorCode, HttpStatus.CONFLICT);
	}

}
