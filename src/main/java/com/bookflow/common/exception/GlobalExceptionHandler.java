package com.bookflow.common.exception;

import java.util.Comparator;
import java.util.List;

import com.bookflow.common.response.ErrorResponse;
import com.bookflow.common.response.ValidationErrorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
		List<ValidationErrorItem> details = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.sorted(Comparator.comparing(FieldError::getField))
				.map(error -> new ValidationErrorItem(error.getField(), error.getDefaultMessage()))
				.toList();

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of("Validation failed", ErrorCode.VALIDATION_ERROR.name(), details));
	}

	@ExceptionHandler(BookFlowException.class)
	ResponseEntity<ErrorResponse> handleBookFlowException(BookFlowException exception) {
		return ResponseEntity
				.status(exception.getStatus())
				.body(ErrorResponse.of(exception.getMessage(), exception.getErrorCode().name()));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(ErrorResponse.of("Resource not found.", ErrorCode.RESOURCE_NOT_FOUND.name()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
		log.warn("Data integrity violation", exception);

		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of(
						"Data integrity violation.",
						ErrorCode.DUPLICATE_RESOURCE.name()));
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	ResponseEntity<ErrorResponse> handleObjectOptimisticLockingFailureException(
			ObjectOptimisticLockingFailureException exception) {
		log.warn("Optimistic locking conflict", exception);

		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of(
						"Resource was changed by another request.",
						ErrorCode.OPTIMISTIC_LOCK_CONFLICT.name()));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
		log.error("Unexpected error", exception);

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(
						"An unexpected error occurred.",
						ErrorCode.INTERNAL_SERVER_ERROR.name()));
	}

}
