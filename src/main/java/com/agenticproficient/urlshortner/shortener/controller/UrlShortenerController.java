package com.agenticproficient.urlshortner.shortener.controller;

import java.net.URI;

import jakarta.validation.Valid;

import com.agenticproficient.urlshortner.common.ApiPaths;
import com.agenticproficient.urlshortner.shortener.dto.AnalyticsResponse;
import com.agenticproficient.urlshortner.shortener.dto.CreateShortUrlRequest;
import com.agenticproficient.urlshortner.shortener.dto.UrlResponse;
import com.agenticproficient.urlshortner.shortener.facade.UrlShortenerFacade;
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
public class UrlShortenerController {

	private final UrlShortenerFacade urlShortenerFacade;

	public UrlShortenerController(UrlShortenerFacade urlShortenerFacade) {
		this.urlShortenerFacade = urlShortenerFacade;
	}

	@PostMapping
	public Mono<ResponseEntity<UrlResponse>> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
		return urlShortenerFacade.createShortUrl(request)
				.map(response -> ResponseEntity.created(URI.create(response.shortUrl())).body(response));
	}

	@GetMapping("/{shortCode}")
	public Mono<UrlResponse> getShortUrl(@PathVariable String shortCode) {
		return urlShortenerFacade.getShortUrl(shortCode);
	}

	@GetMapping("/{shortCode}/analytics")
	public Mono<AnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
		return urlShortenerFacade.getAnalytics(shortCode);
	}
}
