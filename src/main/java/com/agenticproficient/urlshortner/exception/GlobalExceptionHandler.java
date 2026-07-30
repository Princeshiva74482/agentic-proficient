package com.agenticproficient.urlshortner.exception;

import java.time.Clock;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import com.agenticproficient.urlshortner.common.ApplicationConstants;
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
		String requestId = requestId(exchange);
		LOGGER.warn("Handled API error requestId={} status={} code={} path={} detail={}",
				requestId,
				exception.getStatus().value(),
				exception.getErrorCode().getValue(),
				exchange.getRequest().getPath().pathWithinApplication().value(),
				exception.getMessage());
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
		problemDetail.setTitle(exception.getErrorCode().getValue());
		problemDetail.setProperty("code", exception.getErrorCode().getValue());
		problemDetail.setProperty("timestamp", clock.instant());
		problemDetail.setProperty(ApplicationConstants.REQUEST_ID_ATTRIBUTE, requestId);
		return ResponseEntity.status(exception.getStatus()).body(problemDetail);
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ProblemDetail> handleValidationException(WebExchangeBindException exception,
			ServerWebExchange exchange) {
		List<String> errors = exception.getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();
		ProblemDetail problemDetail = baseProblem(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,
				"Request validation failed", exchange);
		problemDetail.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problemDetail);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException exception,
			ServerWebExchange exchange) {
		ProblemDetail problemDetail = baseProblem(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,
				"Constraint validation failed", exchange);
		problemDetail.setProperty("errors", exception.getConstraintViolations().stream()
				.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
				.toList());
		return ResponseEntity.badRequest().body(problemDetail);
	}

	@ExceptionHandler(ServerWebInputException.class)
	public ResponseEntity<ProblemDetail> handleInputException(ServerWebInputException exception,
			ServerWebExchange exchange) {
		return ResponseEntity.badRequest()
				.body(baseProblem(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED, exception.getReason(),
						exchange));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception, ServerWebExchange exchange) {
		LOGGER.error("Unexpected API error requestId={}", requestId(exchange), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(baseProblem(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR,
						"Unexpected server error", exchange));
	}

	private ProblemDetail baseProblem(HttpStatus status, ApiErrorCode errorCode, String detail,
			ServerWebExchange exchange) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
		problemDetail.setTitle(errorCode.getValue());
		problemDetail.setProperty("code", errorCode.getValue());
		problemDetail.setProperty("timestamp", clock.instant());
		problemDetail.setProperty(ApplicationConstants.REQUEST_ID_ATTRIBUTE, requestId(exchange));
		return problemDetail;
	}

	private String requestId(ServerWebExchange exchange) {
		return exchange.getAttributeOrDefault(ApplicationConstants.REQUEST_ID_ATTRIBUTE, ApplicationConstants.UNKNOWN);
	}
}
