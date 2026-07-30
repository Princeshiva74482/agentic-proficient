package com.agenticproficient.urlshortner.shortener.controller;

import java.net.URI;

import jakarta.validation.Valid;

import com.agenticproficient.urlshortner.common.ApiPaths;
import com.agenticproficient.urlshortner.shortener.dto.AnalyticsResponse;
import com.agenticproficient.urlshortner.shortener.dto.CreateShortUrlRequest;
import com.agenticproficient.urlshortner.shortener.dto.UrlResponse;
import com.agenticproficient.urlshortner.shortener.facade.UrlShortenerFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(ApiPaths.URLS)
@Tag(name = "URL Shortener", description = "Create, inspect, and analyze shortened URLs.")
public class UrlShortenerController {

	private final UrlShortenerFacade urlShortenerFacade;

	public UrlShortenerController(UrlShortenerFacade urlShortenerFacade) {
		this.urlShortenerFacade = urlShortenerFacade;
	}

	@PostMapping
	@Operation(summary = "Create a short URL",
			description = "Creates a shortened URL using either a caller-provided custom alias or a generated code.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Short URL created",
					content = @Content(schema = @Schema(implementation = UrlResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid URL, alias, or expiration",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "409", description = "Alias already exists or code allocation failed",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Mono<ResponseEntity<UrlResponse>> createShortUrl(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "URL creation request", required = true,
					content = @Content(schema = @Schema(implementation = CreateShortUrlRequest.class)))
			@Valid @RequestBody CreateShortUrlRequest request) {
		return urlShortenerFacade.createShortUrl(request)
				.map(response -> ResponseEntity.created(URI.create(response.shortUrl())).body(response));
	}

	@GetMapping("/{shortCode}")
	@Operation(summary = "Get short URL metadata",
			description = "Returns destination, short code, lifecycle state, expiration, and access count.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Short URL metadata",
					content = @Content(schema = @Schema(implementation = UrlResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid short code",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "404", description = "Short URL not found",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Mono<UrlResponse> getShortUrl(
			@Parameter(description = "Short code or custom alias", example = "docs2026", required = true)
			@PathVariable String shortCode) {
		return urlShortenerFacade.getShortUrl(shortCode);
	}

	@GetMapping("/{shortCode}/analytics")
	@Operation(summary = "Get short URL analytics",
			description = "Returns total clicks, approximate unique visitors, last access time, and recent click events.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Analytics returned",
					content = @Content(schema = @Schema(implementation = AnalyticsResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid short code",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "404", description = "Short URL not found",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Mono<AnalyticsResponse> getAnalytics(
			@Parameter(description = "Short code or custom alias", example = "docs2026", required = true)
			@PathVariable String shortCode) {
		return urlShortenerFacade.getAnalytics(shortCode);
	}
}
