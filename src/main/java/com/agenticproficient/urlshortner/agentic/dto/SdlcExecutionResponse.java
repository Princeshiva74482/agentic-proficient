package com.agenticproficient.urlshortner.agentic.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowContextDocument;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStatus;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current state and traceable context of an agentic SDLC execution.")
public record SdlcExecutionResponse(
		@Schema(description = "Workflow execution identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
		UUID executionId,
		@Schema(description = "Human-readable correlation id", example = "sdlc-a1b2c3d4")
		String correlationId,
		@Schema(description = "Scenario type", example = "GREENFIELD")
		ScenarioType scenarioType,
		@Schema(description = "Current workflow status", example = "AWAITING_APPROVAL")
		WorkflowStatus status,
		@Schema(description = "Workflow steps that have passed their exit gates")
		List<WorkflowStep> completedSteps,
		@Schema(description = "Step waiting for human approval, if any", example = "IMPLEMENTATION_PLAN")
		WorkflowStep pendingApprovalStep,
		@Schema(description = "Cross-stage context, decisions, risks, outputs, and validation lineage")
		WorkflowContextDocument context,
		@Schema(description = "Reliability and recovery metrics for this execution")
		ReliabilityMetricsResponse reliabilityMetrics,
		@Schema(description = "Execution start time", example = "2026-07-29T17:00:00Z")
		Instant startedAt,
		@Schema(description = "Execution completion time for terminal states", example = "2026-07-29T17:10:00Z")
		Instant completedAt) {
}
