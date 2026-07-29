package com.agenticproficient.urlshortner.agentic.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStatus;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("agentic_executions")
public class AgenticExecution implements Persistable<UUID> {

	@Id
	@Column("id")
	private UUID id;

	@Column("correlation_id")
	private String correlationId;

	@Column("scenario_type")
	private String scenarioType;

	@Column("requirement_text")
	private String requirementText;

	@Column("status")
	private String status;

	@Column("completed_steps")
	private String completedSteps;

	@Column("approved_steps")
	private String approvedSteps;

	@Column("pending_approval_step")
	private String pendingApprovalStep;

	@Column("context_json")
	private String contextJson;

	@Column("success_count")
	private int successCount;

	@Column("failure_count")
	private int failureCount;

	@Column("retry_count")
	private int retryCount;

	@Column("rollback_count")
	private int rollbackCount;

	@Column("mttr_ms")
	private long mttrMs;

	@Column("latency_ms")
	private long latencyMs;

	@Column("started_at")
	private Instant startedAt;

	@Column("completed_at")
	private Instant completedAt;

	@Column("updated_at")
	private Instant updatedAt;

	@Transient
	private boolean newEntity;

	public static AgenticExecution create(ScenarioType scenarioType, String requirementText, String contextJson,
			Instant now) {
		AgenticExecution execution = new AgenticExecution();
		execution.id = UUID.randomUUID();
		execution.correlationId = "sdlc-" + UUID.randomUUID().toString().substring(0, 8);
		execution.scenarioType = scenarioType.name();
		execution.requirementText = requirementText.strip();
		execution.status = WorkflowStatus.CREATED.name();
		execution.completedSteps = "";
		execution.approvedSteps = "";
		execution.contextJson = contextJson;
		execution.successCount = 0;
		execution.failureCount = 0;
		execution.retryCount = 0;
		execution.rollbackCount = 0;
		execution.mttrMs = 0;
		execution.latencyMs = 0;
		execution.startedAt = now;
		execution.updatedAt = now;
		execution.newEntity = true;
		return execution;
	}

	public void markPersisted() {
		this.newEntity = false;
	}

	public void markRunning(Instant now) {
		this.status = WorkflowStatus.RUNNING.name();
		this.pendingApprovalStep = null;
		this.updatedAt = now;
	}

	public void markAwaitingApproval(WorkflowStep step, Instant now) {
		this.status = WorkflowStatus.AWAITING_APPROVAL.name();
		this.pendingApprovalStep = step.name();
		this.updatedAt = now;
		this.latencyMs = elapsedMillis(now);
	}

	public void markCompleted(Instant now) {
		this.status = WorkflowStatus.COMPLETED.name();
		this.completedAt = now;
		this.updatedAt = now;
		this.latencyMs = elapsedMillis(now);
	}

	public void markSafeStopped(Instant now) {
		this.status = WorkflowStatus.SAFE_STOPPED.name();
		this.completedAt = now;
		this.updatedAt = now;
		this.latencyMs = elapsedMillis(now);
	}

	public void markRolledBack(Instant now) {
		this.status = WorkflowStatus.ROLLED_BACK.name();
		this.completedAt = now;
		this.updatedAt = now;
		this.latencyMs = elapsedMillis(now);
		this.mttrMs = latencyMs;
	}

	public Set<WorkflowStep> completedStepSet() {
		return parseStepSet(completedSteps);
	}

	public void setCompletedStepSet(Collection<WorkflowStep> steps) {
		this.completedSteps = joinSteps(steps);
	}

	public Set<WorkflowStep> approvedStepSet() {
		return parseStepSet(approvedSteps);
	}

	public void setApprovedStepSet(Collection<WorkflowStep> steps) {
		this.approvedSteps = joinSteps(steps);
	}

	public void incrementSuccessCount(int value) {
		this.successCount += value;
	}

	public void incrementFailureCount() {
		this.failureCount++;
	}

	public void incrementRetryCount(int value) {
		this.retryCount += value;
	}

	public void incrementRollbackCount() {
		this.rollbackCount++;
	}

	private long elapsedMillis(Instant now) {
		return startedAt == null ? 0 : Duration.between(startedAt, now).toMillis();
	}

	private Set<WorkflowStep> parseStepSet(String value) {
		if (value == null || value.isBlank()) {
			return new LinkedHashSet<>();
		}
		return Arrays.stream(value.split(","))
				.filter(step -> !step.isBlank())
				.map(WorkflowStep::valueOf)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private String joinSteps(Collection<WorkflowStep> steps) {
		return steps.stream().map(WorkflowStep::name).collect(Collectors.joining(","));
	}

	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return newEntity;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getScenarioType() {
		return scenarioType;
	}

	public void setScenarioType(String scenarioType) {
		this.scenarioType = scenarioType;
	}

	public String getRequirementText() {
		return requirementText;
	}

	public void setRequirementText(String requirementText) {
		this.requirementText = requirementText;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCompletedSteps() {
		return completedSteps;
	}

	public void setCompletedSteps(String completedSteps) {
		this.completedSteps = completedSteps;
	}

	public String getApprovedSteps() {
		return approvedSteps;
	}

	public void setApprovedSteps(String approvedSteps) {
		this.approvedSteps = approvedSteps;
	}

	public String getPendingApprovalStep() {
		return pendingApprovalStep;
	}

	public void setPendingApprovalStep(String pendingApprovalStep) {
		this.pendingApprovalStep = pendingApprovalStep;
	}

	public String getContextJson() {
		return contextJson;
	}

	public void setContextJson(String contextJson) {
		this.contextJson = contextJson;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public int getRollbackCount() {
		return rollbackCount;
	}

	public void setRollbackCount(int rollbackCount) {
		this.rollbackCount = rollbackCount;
	}

	public long getMttrMs() {
		return mttrMs;
	}

	public void setMttrMs(long mttrMs) {
		this.mttrMs = mttrMs;
	}

	public long getLatencyMs() {
		return latencyMs;
	}

	public void setLatencyMs(long latencyMs) {
		this.latencyMs = latencyMs;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Instant startedAt) {
		this.startedAt = startedAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Instant completedAt) {
		this.completedAt = completedAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
