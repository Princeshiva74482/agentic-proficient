package com.agenticproficient.urlshortner.agentic.mapper;

import java.util.ArrayList;

import com.agenticproficient.urlshortner.agentic.dto.AuditEventResponse;
import com.agenticproficient.urlshortner.agentic.dto.ReliabilityMetricsResponse;
import com.agenticproficient.urlshortner.agentic.dto.SdlcExecutionResponse;
import com.agenticproficient.urlshortner.agentic.entity.AgenticAuditEvent;
import com.agenticproficient.urlshortner.agentic.entity.AgenticExecution;
import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowContextDocument;
import com.agenticproficient.urlshortner.agentic.model.WorkflowEventType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStatus;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AgenticResponseMapper {

	private final ObjectMapper objectMapper;

	public AgenticResponseMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public SdlcExecutionResponse toResponse(AgenticExecution execution) {
		return new SdlcExecutionResponse(
				execution.getId(),
				execution.getCorrelationId(),
				ScenarioType.valueOf(execution.getScenarioType()),
				WorkflowStatus.valueOf(execution.getStatus()),
				new ArrayList<>(execution.completedStepSet()),
				execution.getPendingApprovalStep() == null ? null : WorkflowStep.valueOf(execution.getPendingApprovalStep()),
				readContext(execution.getContextJson()),
				toReliabilityMetrics(execution),
				execution.getStartedAt(),
				execution.getCompletedAt());
	}

	public AuditEventResponse toResponse(AgenticAuditEvent event) {
		return new AuditEventResponse(
				event.getId(),
				event.getExecutionId(),
				WorkflowEventType.valueOf(event.getEventType()),
				event.getStepId() == null ? null : WorkflowStep.valueOf(event.getStepId()),
				event.getMessage(),
				event.getMetadataJson(),
				event.getCreatedAt());
	}

	private ReliabilityMetricsResponse toReliabilityMetrics(AgenticExecution execution) {
		int totalOutcomes = execution.getSuccessCount() + execution.getFailureCount();
		double successRate = totalOutcomes == 0 ? 0 : (double) execution.getSuccessCount() / totalOutcomes;
		return new ReliabilityMetricsResponse(successRate, execution.getRetryCount(), execution.getRollbackCount(),
				execution.getMttrMs(), execution.getLatencyMs());
	}

	private WorkflowContextDocument readContext(String contextJson) {
		try {
			return objectMapper.readValue(contextJson, WorkflowContextDocument.class);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Unable to read workflow context", exception);
		}
	}
}
