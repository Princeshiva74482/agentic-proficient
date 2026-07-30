package com.agenticproficient.urlshortner.shortener.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import com.agenticproficient.urlshortner.config.UrlShortenerProperties;
import com.agenticproficient.urlshortner.exception.ApiErrorCode;
import com.agenticproficient.urlshortner.exception.DomainException;
import com.agenticproficient.urlshortner.shortener.dto.AnalyticsResponse;
import com.agenticproficient.urlshortner.shortener.dto.CreateShortUrlRequest;
import com.agenticproficient.urlshortner.shortener.dto.RecentClickResponse;
import com.agenticproficient.urlshortner.shortener.dto.RedirectTarget;
import com.agenticproficient.urlshortner.shortener.dto.UrlResponse;
import com.agenticproficient.urlshortner.shortener.entity.ClickEvent;
import com.agenticproficient.urlshortner.shortener.entity.ShortUrl;
import com.agenticproficient.urlshortner.shortener.mapper.UrlShortenerMapper;
import com.agenticproficient.urlshortner.shortener.model.ClickContext;
import com.agenticproficient.urlshortner.shortener.repository.ClickEventRepository;
import com.agenticproficient.urlshortner.shortener.repository.ShortUrlRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Application service that acts as the facade for URL shortener use cases.
 */
@Service
public class UrlShortenerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UrlShortenerService.class);

	private final ClickEventRepository clickEventRepository;
	private final Clock clock;
	private final Counter createdCounter;
	private final Counter redirectCounter;
	private final Counter redirectMissCounter;
	private final RequestFingerprintService fingerprintService;
	private final ShortCodeGenerator shortCodeGenerator;
	private final ShortUrlRepository shortUrlRepository;
	private final UrlPolicyValidator policyValidator;
	private final UrlShortenerMapper mapper;
	private final UrlShortenerProperties properties;

	public UrlShortenerService(ClickEventRepository clickEventRepository, Clock clock, MeterRegistry meterRegistry,
			RequestFingerprintService fingerprintService, ShortCodeGenerator shortCodeGenerator,
			ShortUrlRepository shortUrlRepository, UrlPolicyValidator policyValidator, UrlShortenerMapper mapper,
			UrlShortenerProperties properties) {
		this.clickEventRepository = clickEventRepository;
		this.clock = clock;
		this.createdCounter = meterRegistry.counter("urlshortener.urls.created");
		this.redirectCounter = meterRegistry.counter("urlshortener.redirects", "result", "success");
		this.redirectMissCounter = meterRegistry.counter("urlshortener.redirects", "result", "miss");
		this.fingerprintService = fingerprintService;
		this.shortCodeGenerator = shortCodeGenerator;
		this.shortUrlRepository = shortUrlRepository;
		this.policyValidator = policyValidator;
		this.mapper = mapper;
		this.properties = properties;
	}

	public Mono<UrlResponse> createShortUrl(CreateShortUrlRequest request) {
		return create(request)
				.map(shortUrl -> mapper.toResponse(shortUrl, normalizedBaseUrl()));
	}

	public Flux<UrlResponse> getAllShortUrls() {
		return shortUrlRepository.findAvailable(properties.getMaxListSize())
				.map(shortUrl -> mapper.toResponse(shortUrl, normalizedBaseUrl()));
	}

	public Mono<UrlResponse> getShortUrl(String shortCode) {
		return getByShortCode(shortCode)
				.map(shortUrl -> mapper.toResponse(shortUrl, normalizedBaseUrl()));
	}

	private Mono<ShortUrl> create(CreateShortUrlRequest request) {
		return Mono.fromCallable(() -> policyValidator.toCommand(request))
				.flatMap(command -> command.customAlias() == null
						? createWithGeneratedCode(command, 1)
						: createWithCustomAlias(command))
				.doOnSuccess(shortUrl -> createdCounter.increment());
	}

	public Mono<ShortUrl> getByShortCode(String shortCode) {
		String normalizedCode = policyValidator.validateLookupCode(shortCode);
		return shortUrlRepository.findByShortCode(normalizedCode)
				.switchIfEmpty(Mono.error(() -> notFound(normalizedCode)));
	}

	public Mono<RedirectTarget> resolveRedirect(String shortCode, ClickContext clickContext) {
		return getByShortCode(shortCode)
				.flatMap(shortUrl -> {
					if (!shortUrl.isActive()) {
						redirectMissCounter.increment();
						return Mono.error(notFound(shortCode));
					}
					if (shortUrl.isExpired(clock)) {
						redirectMissCounter.increment();
						return Mono.error(DomainException.gone(ApiErrorCode.URL_EXPIRED, "Short URL has expired"));
					}
					return trackClick(shortUrl, clickContext)
							.onErrorResume(exception -> {
								LOGGER.warn("Click tracking failed for short code {}", shortCode, exception);
								return Mono.empty();
							})
							.thenReturn(new RedirectTarget(shortUrl.getOriginalUrl(), shortUrl.getShortCode()))
							.doOnSuccess(target -> redirectCounter.increment());
				});
	}

	public Mono<AnalyticsResponse> getAnalytics(String shortCode) {
		return getByShortCode(shortCode)
				.flatMap(shortUrl -> {
					Mono<Long> uniqueVisitors = clickEventRepository.countUniqueVisitors(shortUrl.getId())
							.defaultIfEmpty(0L);
					Mono<Optional<Instant>> lastAccessedAt = clickEventRepository
							.findFirstByShortUrlIdOrderByClickedAtDesc(shortUrl.getId())
							.map(ClickEvent::getClickedAt)
							.map(Optional::of)
							.defaultIfEmpty(Optional.empty());
					Mono<java.util.List<RecentClickResponse>> recentClicks = clickEventRepository
							.findRecentClicks(shortUrl.getId())
							.map(mapper::toRecentClickResponse)
							.collectList();
					return Mono.zip(uniqueVisitors, lastAccessedAt, recentClicks)
							.map(tuple -> mapper.toAnalyticsResponse(shortUrl, tuple.getT1(), tuple.getT2().orElse(null),
									tuple.getT3()));
				});
	}

	private Mono<ShortUrl> createWithCustomAlias(CreateShortUrlCommand command) {
		return shortUrlRepository.existsByShortCode(command.customAlias())
				.flatMap(exists -> exists
						? Mono.error(DomainException.conflict(ApiErrorCode.ALIAS_UNAVAILABLE, "Alias is already in use"))
						: shortUrlRepository.save(ShortUrl.create(command.originalUrl(), command.customAlias(),
								command.expiresAt(), clock)))
				.onErrorMap(DataIntegrityViolationException.class,
						exception -> DomainException.conflict(ApiErrorCode.ALIAS_UNAVAILABLE, "Alias is already in use"));
	}

	private Mono<ShortUrl> createWithGeneratedCode(CreateShortUrlCommand command, int attempt) {
		if (attempt > properties.getMaxCodeGenerationAttempts()) {
			return Mono.error(DomainException.conflict(ApiErrorCode.ALIAS_UNAVAILABLE,
					"Unable to allocate a unique short code"));
		}
		return generateUniqueCode()
				.flatMap(shortCode -> shortUrlRepository.save(
						ShortUrl.create(command.originalUrl(), shortCode, command.expiresAt(), clock)))
				.onErrorResume(DataIntegrityViolationException.class,
						exception -> createWithGeneratedCode(command, attempt + 1));
	}

	private Mono<String> generateUniqueCode() {
		return Mono.defer(() -> {
			String shortCode = shortCodeGenerator.generate(properties.getCodeLength());
			return shortUrlRepository.existsByShortCode(shortCode)
					.flatMap(exists -> exists ? generateUniqueCode() : Mono.just(shortCode));
		});
	}

	private Mono<Void> trackClick(ShortUrl shortUrl, ClickContext clickContext) {
		String ipHash = fingerprintService.hashRemoteAddress(clickContext.remoteAddress());
		ClickEvent clickEvent = ClickEvent.create(shortUrl, clickContext.userAgent(), clickContext.referrer(), ipHash,
				clock);
		return Mono.when(clickEventRepository.save(clickEvent), shortUrlRepository.incrementAccessCount(shortUrl.getId()))
				.then();
	}

	private DomainException notFound(String shortCode) {
		return DomainException.notFound(ApiErrorCode.URL_NOT_FOUND, "Short URL not found: " + shortCode);
	}

	private String normalizedBaseUrl() {
		String baseUrl = properties.getBaseUrl();
		while (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl;
	}
}
