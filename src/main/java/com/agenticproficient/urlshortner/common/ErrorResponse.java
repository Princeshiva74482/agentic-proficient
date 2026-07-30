package com.agenticproficient.urlshortner.common;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	private Instant timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
	private Map<String, Object> details;

	public ErrorResponse() {
	}

	public ErrorResponse(int status, String error, String message, String path) {
		this.timestamp = Instant.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public ErrorResponse(int status, String error, String message, String path, Map<String, Object> details) {
		this.timestamp = Instant.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
		this.details = details;
	}

	@JsonProperty("timestamp")
	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	@JsonProperty("status")
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@JsonProperty("error")
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty("path")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@JsonProperty("details")
	public Map<String, Object> getDetails() {
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}
}
