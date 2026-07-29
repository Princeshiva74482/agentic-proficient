package com.agenticproficient.urlshortner.agentic.dto;

public record ReliabilityMetricsResponse(
		double successRate,
		int retryCount,
		int rollbackCount,
		long mttrMillis,
		long endToEndLatencyMillis) {
}
