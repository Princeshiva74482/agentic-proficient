package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Recent click event metadata with privacy-preserving omission of raw IP address.")
public record RecentClickResponse(
		@Schema(description = "Click timestamp", example = "2026-07-29T17:10:00Z")
		Instant clickedAt,
		@Schema(description = "HTTP referrer header when present", example = "https://search.example.com")
		String referrer,
		@Schema(description = "User-Agent header when present", example = "Mozilla/5.0")
		String userAgent) {
}
