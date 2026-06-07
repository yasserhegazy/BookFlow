package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleViolationException extends BookFlowException {

	public BusinessRuleViolationException(String message) {
		super(message, ErrorCode.BUSINESS_RULE_VIOLATION, HttpStatus.UNPROCESSABLE_ENTITY);
	}

}
