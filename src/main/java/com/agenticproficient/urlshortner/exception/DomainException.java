package com.agenticproficient.urlshortner.exception;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {

	private final ApiErrorCode errorCode;
	private final HttpStatus status;

	public DomainException(ApiErrorCode errorCode, HttpStatus status, String message) {
		super(message);
		this.errorCode = errorCode;
		this.status = status;
	}

	public ApiErrorCode getErrorCode() {
		return errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public static DomainException badRequest(ApiErrorCode errorCode, String message) {
		return new DomainException(errorCode, HttpStatus.BAD_REQUEST, message);
	}

	public static DomainException conflict(ApiErrorCode errorCode, String message) {
		return new DomainException(errorCode, HttpStatus.CONFLICT, message);
	}

	public static DomainException forbidden(ApiErrorCode errorCode, String message) {
		return new DomainException(errorCode, HttpStatus.FORBIDDEN, message);
	}

	public static DomainException gone(ApiErrorCode errorCode, String message) {
		return new DomainException(errorCode, HttpStatus.GONE, message);
	}

	public static DomainException notFound(ApiErrorCode errorCode, String message) {
		return new DomainException(errorCode, HttpStatus.NOT_FOUND, message);
	}
}
