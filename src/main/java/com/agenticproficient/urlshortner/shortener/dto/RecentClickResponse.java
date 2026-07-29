package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;

public record RecentClickResponse(
		Instant clickedAt,
		String referrer,
		String userAgent) {
}
