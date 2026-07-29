package com.agenticproficient.urlshortner.agentic.dto;

import java.time.Instant;
import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.model.WorkflowEventType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;

public record AuditEventResponse(
		UUID id,
		UUID executionId,
		WorkflowEventType eventType,
		WorkflowStep step,
		String message,
		String metadataJson,
		Instant createdAt) {
}
