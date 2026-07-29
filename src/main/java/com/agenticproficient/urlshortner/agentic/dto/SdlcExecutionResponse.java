package com.agenticproficient.urlshortner.agentic.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowContextDocument;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStatus;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;

public record SdlcExecutionResponse(
		UUID executionId,
		String correlationId,
		ScenarioType scenarioType,
		WorkflowStatus status,
		List<WorkflowStep> completedSteps,
		WorkflowStep pendingApprovalStep,
		WorkflowContextDocument context,
		ReliabilityMetricsResponse reliabilityMetrics,
		Instant startedAt,
		Instant completedAt) {
}
