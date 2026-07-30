package com.agenticproficient.urlshortner.exception;

import java.time.Clock;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final Clock clock;

	public GlobalExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(DomainException.class)
	public ResponseEntity<ProblemDetail> handleDomainException(DomainException exception, ServerWebExchange exchange) {
		LOGGER.warn("Handled API error status={} code={} path={} detail={}",
				exception.getStatus().value(),
				exception.getErrorCode().getValue(),
				exchange.getRequest().getPath().pathWithinApplication().value(),
				exception.getMessage());
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
		problemDetail.setTitle(exception.getErrorCode().getValue());
		problemDetail.setProperty("code", exception.getErrorCode().getValue());
		problemDetail.setProperty("timestamp", clock.instant());
		return ResponseEntity.status(exception.getStatus()).body(problemDetail);
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ProblemDetail> handleValidationException(WebExchangeBindException exception) {
		List<String> errors = exception.getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();
		ProblemDetail problemDetail = baseProblem(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,
				"Request validation failed");
		problemDetail.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problemDetail);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException exception) {
		ProblemDetail problemDetail = baseProblem(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,
				"Constraint validation failed");
		problemDetail.setProperty("errors", exception.getConstraintViolations().stream()
				.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
				.toList());
		return ResponseEntity.badRequest().body(problemDetail);
	}

	@ExceptionHandler(ServerWebInputException.class)
	public ResponseEntity<ProblemDetail> handleInputException(ServerWebInputException exception) {
		return ResponseEntity.badRequest()
				.body(baseProblem(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED, exception.getReason()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
		LOGGER.error("Unexpected API error", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(baseProblem(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR,
						"Unexpected server error"));
	}

	private ProblemDetail baseProblem(HttpStatus status, ApiErrorCode errorCode, String detail) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(errorCode.getValue());
		problemDetail.setProperty("code", errorCode.getValue());
		problemDetail.setProperty("timestamp", clock.instant());
		return problemDetail;
	}
}
