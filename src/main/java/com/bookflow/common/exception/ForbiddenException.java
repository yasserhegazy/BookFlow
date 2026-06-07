package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BookFlowException {

	public ForbiddenException(String message) {
		super(message, ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN);
	}

}
