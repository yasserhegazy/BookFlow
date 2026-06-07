package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BookFlowException {

	public BadRequestException(String message) {
		super(message, ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
	}

}
