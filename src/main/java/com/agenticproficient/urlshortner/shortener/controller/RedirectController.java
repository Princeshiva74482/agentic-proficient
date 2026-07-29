package com.agenticproficient.urlshortner.shortener.controller;

import java.net.URI;

import com.agenticproficient.urlshortner.shortener.facade.UrlShortenerFacade;
import com.agenticproficient.urlshortner.shortener.model.ClickContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class RedirectController {

	private final UrlShortenerFacade urlShortenerFacade;

	public RedirectController(UrlShortenerFacade urlShortenerFacade) {
		this.urlShortenerFacade = urlShortenerFacade;
	}

	@GetMapping("/{shortCode}")
	public Mono<ResponseEntity<Void>> redirect(@PathVariable String shortCode, ServerHttpRequest request) {
		return urlShortenerFacade.resolveRedirect(shortCode, ClickContext.from(request))
				.map(target -> ResponseEntity.status(HttpStatus.FOUND)
						.location(URI.create(target.originalUrl()))
						.<Void>build());
	}
}
