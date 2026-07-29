package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;
import java.util.List;

public record AnalyticsResponse(
		String shortCode,
		String originalUrl,
		long totalClicks,
		long uniqueVisitors,
		Instant lastAccessedAt,
		List<RecentClickResponse> recentClicks) {
}
