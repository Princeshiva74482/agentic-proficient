package com.agenticproficient.urlshortner.shortener.controller;

import java.net.URI;

import com.agenticproficient.urlshortner.shortener.model.ClickContext;
import com.agenticproficient.urlshortner.shortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "Redirect", description = "Resolve short codes and redirect to original URLs.")
public class RedirectController {

	private final UrlShortenerService urlShortenerService;

	public RedirectController(UrlShortenerService urlShortenerService) {
		this.urlShortenerService = urlShortenerService;
	}

	@GetMapping("/{shortCode}")
	@Operation(summary = "Redirect short URL",
			description = "Resolves a short code, records a click event, and returns a 302 Location header.")
	@ApiResponses({
			@ApiResponse(responseCode = "302", description = "Redirect to original URL"),
			@ApiResponse(responseCode = "400", description = "Invalid short code",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "404", description = "Short URL not found",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "410", description = "Short URL expired",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Mono<ResponseEntity<Void>> redirect(
			@Parameter(description = "Short code or custom alias", example = "docs2026", required = true)
			@PathVariable String shortCode,
			ServerHttpRequest request) {

		return urlShortenerService.resolveRedirect(shortCode, ClickContext.from(request))
				.map(target -> ResponseEntity.status(HttpStatus.FOUND)
						.location(URI.create(target.originalUrl()))
						.<Void>build());
	}
}
