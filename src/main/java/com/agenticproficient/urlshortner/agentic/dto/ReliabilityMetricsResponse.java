package com.agenticproficient.urlshortner.agentic.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reliability metrics captured for a workflow execution.")
public record ReliabilityMetricsResponse(
		@Schema(description = "Completed steps divided by total successful and failed outcomes", example = "1.0")
		double successRate,
		@Schema(description = "Total retry attempts", example = "0")
		int retryCount,
		@Schema(description = "Total rollback events", example = "0")
		int rollbackCount,
		@Schema(description = "Mean time to recovery in milliseconds", example = "0")
		long mttrMillis,
		@Schema(description = "End-to-end workflow latency in milliseconds", example = "1250")
		long endToEndLatencyMillis) {
}
