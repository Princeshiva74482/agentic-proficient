package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated analytics for a short URL.")
public record AnalyticsResponse(
		@Schema(description = "Short code", example = "docs2026")
		String shortCode,
		@Schema(description = "Original destination URL", example = "https://example.com/docs")
		String originalUrl,
		@Schema(description = "Total tracked clicks", example = "42")
		long totalClicks,
		@Schema(description = "Approximate unique visitors based on salted IP hashes", example = "12")
		long uniqueVisitors,
		@Schema(description = "Most recent click timestamp", example = "2026-07-29T17:10:00Z")
		Instant lastAccessedAt,
		@Schema(description = "Last 10 tracked click events")
		List<RecentClickResponse> recentClicks) {
}
