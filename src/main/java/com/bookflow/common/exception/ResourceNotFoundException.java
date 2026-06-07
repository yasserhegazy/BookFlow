package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BookFlowException {

	public ResourceNotFoundException(String message) {
		super(message, ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
	}

}
