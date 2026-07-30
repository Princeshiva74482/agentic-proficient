package com.agenticproficient.urlshortner.exception;

public enum ApiErrorCode {

	ALIAS_UNAVAILABLE("ALIAS_UNAVAILABLE"),
	INTERNAL_ERROR("INTERNAL_ERROR"),
	INVALID_ALIAS("INVALID_ALIAS"),
	INVALID_STATE("INVALID_STATE"),
	INVALID_URL("INVALID_URL"),
	POLICY_VIOLATION("POLICY_VIOLATION"),
	URL_EXPIRED("URL_EXPIRED"),
	URL_NOT_FOUND("URL_NOT_FOUND"),
	VALIDATION_FAILED("VALIDATION_FAILED"),
	WORKFLOW_NOT_FOUND("WORKFLOW_NOT_FOUND");

	private final String value;

	ApiErrorCode(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
