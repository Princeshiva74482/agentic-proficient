package com.agenticproficient.urlshortner.shortener.facade;

import com.agenticproficient.urlshortner.shortener.dto.AnalyticsResponse;
import com.agenticproficient.urlshortner.shortener.dto.CreateShortUrlRequest;
import com.agenticproficient.urlshortner.shortener.dto.RedirectTarget;
import com.agenticproficient.urlshortner.shortener.dto.UrlResponse;
import com.agenticproficient.urlshortner.shortener.model.ClickContext;
import reactor.core.publisher.Mono;

public interface UrlShortenerFacade {

	Mono<UrlResponse> createShortUrl(CreateShortUrlRequest request);

	Mono<UrlResponse> getShortUrl(String shortCode);

	Mono<RedirectTarget> resolveRedirect(String shortCode, ClickContext clickContext);

	Mono<AnalyticsResponse> getAnalytics(String shortCode);
}
