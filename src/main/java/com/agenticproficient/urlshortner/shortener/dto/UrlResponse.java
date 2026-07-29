package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;
import java.util.UUID;

public record UrlResponse(
		UUID id,
		String originalUrl,
		String shortCode,
		String shortUrl,
		Instant createdAt,
		Instant expiresAt,
		boolean active,
		long accessCount) {
}
