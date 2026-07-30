package com.agenticproficient.urlshortner.agentic.dto;

import java.time.Instant;
import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.model.WorkflowEventType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Immutable workflow audit event.")
public record AuditEventResponse(
		@Schema(description = "Audit event id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		UUID id,
		@Schema(description = "Owning workflow execution id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		UUID executionId,
		@Schema(description = "Audit event type", example = "STEP_COMPLETED")
		WorkflowEventType eventType,
		@Schema(description = "Workflow step associated with the event", example = "REQUIREMENT_ANALYSIS")
		WorkflowStep step,
		@Schema(description = "Audit event message", example = "Completed Requirement Understanding")
		String message,
		@Schema(description = "JSON metadata captured for the event", example = "{\"retryCount\":0}")
		String metadataJson,
		@Schema(description = "Event creation timestamp", example = "2026-07-29T17:00:00Z")
		Instant createdAt) {
}
