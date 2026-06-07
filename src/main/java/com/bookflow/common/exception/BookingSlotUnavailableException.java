package com.bookflow.common.exception;

import org.springframework.http.HttpStatus;

public class BookingSlotUnavailableException extends BookFlowException {

	public BookingSlotUnavailableException(String message) {
		super(message, ErrorCode.BOOKING_SLOT_UNAVAILABLE, HttpStatus.CONFLICT);
	}

}
