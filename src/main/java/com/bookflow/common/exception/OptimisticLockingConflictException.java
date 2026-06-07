package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class OptimisticLockingConflictException extends BookFlowException {

	public OptimisticLockingConflictException(String message) {
		super(message, ErrorCode.OPTIMISTIC_LOCK_CONFLICT, HttpStatus.CONFLICT);
	}

}
