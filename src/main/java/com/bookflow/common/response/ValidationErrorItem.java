package com.bookflow.common.response;

public record ValidationErrorItem(
		String field,
		String message
) {
}
