package com.bookflow.common.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
		boolean success,
		String message,
		String errorCode,
		List<ValidationErrorItem> details,
		Instant timestamp
) {

	public static ErrorResponse of(String message, String errorCode) {
		return of(message, errorCode, List.of());
	}

	public static ErrorResponse of(String message, String errorCode, List<ValidationErrorItem> details) {
		return new ErrorResponse(false, message, errorCode, List.copyOf(details), Instant.now());
	}

}
