package com.agenticproficient.urlshortner.shortener.facade;

import com.agenticproficient.urlshortner.config.UrlShortenerProperties;
import com.agenticproficient.urlshortner.shortener.dto.AnalyticsResponse;
import com.agenticproficient.urlshortner.shortener.dto.CreateShortUrlRequest;
import com.agenticproficient.urlshortner.shortener.dto.RedirectTarget;
import com.agenticproficient.urlshortner.shortener.dto.UrlResponse;
import com.agenticproficient.urlshortner.shortener.mapper.UrlShortenerMapper;
import com.agenticproficient.urlshortner.shortener.model.ClickContext;
import com.agenticproficient.urlshortner.shortener.service.UrlShortenerService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UrlShortenerFacadeImpl implements UrlShortenerFacade {

	private final UrlShortenerMapper mapper;
	private final UrlShortenerProperties properties;
	private final UrlShortenerService urlShortenerService;

	public UrlShortenerFacadeImpl(UrlShortenerMapper mapper, UrlShortenerProperties properties,
			UrlShortenerService urlShortenerService) {
		this.mapper = mapper;
		this.properties = properties;
		this.urlShortenerService = urlShortenerService;
	}

	@Override
	public Mono<UrlResponse> createShortUrl(CreateShortUrlRequest request) {
		return urlShortenerService.create(request)
				.map(shortUrl -> mapper.toResponse(shortUrl, normalizedBaseUrl()));
	}

	@Override
	public Mono<UrlResponse> getShortUrl(String shortCode) {
		return urlShortenerService.getByShortCode(shortCode)
				.map(shortUrl -> mapper.toResponse(shortUrl, normalizedBaseUrl()));
	}

	@Override
	public Mono<RedirectTarget> resolveRedirect(String shortCode, ClickContext clickContext) {
		return urlShortenerService.resolveRedirect(shortCode, clickContext);
	}

	@Override
	public Mono<AnalyticsResponse> getAnalytics(String shortCode) {
		return urlShortenerService.getAnalytics(shortCode);
	}

	private String normalizedBaseUrl() {
		String baseUrl = properties.getBaseUrl();
		while (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl;
	}
}
