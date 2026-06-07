package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BookFlowException {

	public UnauthorizedException(String message) {
		super(message, ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
	}

}
