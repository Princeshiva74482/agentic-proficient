package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Short URL metadata returned after creation or lookup.")
public record UrlResponse(
		@Schema(description = "Internal URL identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		UUID id,
		@Schema(description = "Original destination URL", example = "https://example.com/docs")
		String originalUrl,
		@Schema(description = "Short code used in redirect URL", example = "docs2026")
		String shortCode,
		@Schema(description = "Fully-qualified shortened URL", example = "http://localhost:8080/docs2026")
		String shortUrl,
		@Schema(description = "Creation timestamp", example = "2026-07-29T17:00:00Z")
		Instant createdAt,
		@Schema(description = "Expiration timestamp", example = "2027-12-31T23:59:59Z")
		Instant expiresAt,
		@Schema(description = "Whether the short URL is currently active", example = "true")
		boolean active,
		@Schema(description = "Total successful redirect count", example = "42")
		long accessCount) {
}
