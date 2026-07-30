package com.agenticproficient.urlshortner.shortener.service;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

import com.agenticproficient.urlshortner.config.UrlShortenerProperties;
import com.agenticproficient.urlshortner.exception.ApiErrorCode;
import com.agenticproficient.urlshortner.exception.DomainException;
import com.agenticproficient.urlshortner.shortener.dto.CreateShortUrlRequest;
import org.springframework.stereotype.Service;

@Service
public class UrlPolicyValidator {

	private static final Pattern ALIAS_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{4,32}$");

	private final Clock clock;
	private final UrlShortenerProperties properties;

	public UrlPolicyValidator(Clock clock, UrlShortenerProperties properties) {
		this.clock = clock;
		this.properties = properties;
	}

	public CreateShortUrlCommand toCommand(CreateShortUrlRequest request) {
		String originalUrl = validateAndNormalizeOriginalUrl(request.originalUrl());
		String customAlias = validateAndNormalizeAlias(request.customAlias());
		Instant expiresAt = request.expiresAt() == null
				? clock.instant().plusSeconds(properties.getDefaultTtlDays() * 24 * 60 * 60)
				: request.expiresAt();
		if (!expiresAt.isAfter(clock.instant())) {
			throw DomainException.badRequest(ApiErrorCode.VALIDATION_FAILED, "Expiration must be in the future");
		}
		return new CreateShortUrlCommand(originalUrl, customAlias, expiresAt);
	}

	public String validateLookupCode(String shortCode) {
		String normalized = validateAndNormalizeAlias(shortCode);
		if (normalized == null) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_ALIAS, "Short code is required");
		}
		return normalized;
	}

	private String validateAndNormalizeOriginalUrl(String originalUrl) {
		if (originalUrl == null || originalUrl.isBlank()) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_URL, "Original URL is required");
		}
		String trimmedUrl = originalUrl.trim();
		URI uri;
		try {
			uri = URI.create(trimmedUrl);
		}
		catch (IllegalArgumentException exception) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_URL, "Original URL is malformed");
		}
		String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
		if (!properties.getSecurity().getAllowedSchemes().contains(scheme)) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_URL, "Only HTTP and HTTPS URLs are allowed");
		}
		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_URL, "Original URL must include a host");
		}
		if (uri.getUserInfo() != null) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_URL, "URLs with embedded credentials are not allowed");
		}
		if (!properties.getSecurity().isAllowPrivateHosts() && isPrivateOrLocalHost(host)) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_URL, "Private or local hosts are not allowed");
		}
		return uri.normalize().toString();
	}

	private String validateAndNormalizeAlias(String alias) {
		if (alias == null || alias.isBlank()) {
			return null;
		}
		String normalized = alias.trim();
		if (!ALIAS_PATTERN.matcher(normalized).matches()) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_ALIAS,
					"Alias must contain 4-32 characters using letters, numbers, underscores, or hyphens");
		}
		String lowerAlias = normalized.toLowerCase(Locale.ROOT);
		boolean reserved = properties.getSecurity().getReservedAliases().stream()
				.map(value -> value.toLowerCase(Locale.ROOT))
				.anyMatch(lowerAlias::equals);
		if (reserved) {
			throw DomainException.badRequest(ApiErrorCode.INVALID_ALIAS, "Alias is reserved");
		}
		return normalized;
	}

	private boolean isPrivateOrLocalHost(String host) {
		String normalizedHost = host.toLowerCase(Locale.ROOT);
		if (normalizedHost.endsWith(".")) {
			normalizedHost = normalizedHost.substring(0, normalizedHost.length() - 1);
		}
		if ("localhost".equals(normalizedHost) || normalizedHost.endsWith(".localhost")) {
			return true;
		}
		if (normalizedHost.equals("::1") || normalizedHost.startsWith("fc") || normalizedHost.startsWith("fd")
				|| normalizedHost.startsWith("fe80")) {
			return true;
		}
		return isPrivateIpv4(normalizedHost);
	}

	private boolean isPrivateIpv4(String host) {
		String[] segments = host.split("\\.");
		if (segments.length != 4) {
			return false;
		}
		int[] octets = new int[4];
		try {
			for (int index = 0; index < segments.length; index++) {
				octets[index] = Integer.parseInt(segments[index]);
				if (octets[index] < 0 || octets[index] > 255) {
					return false;
				}
			}
		}
		catch (NumberFormatException exception) {
			return false;
		}
		return octets[0] == 10
				|| octets[0] == 127
				|| (octets[0] == 172 && octets[1] >= 16 && octets[1] <= 31)
				|| (octets[0] == 192 && octets[1] == 168)
				|| (octets[0] == 169 && octets[1] == 254)
				|| octets[0] == 0;
	}
}
