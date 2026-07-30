package com.agenticproficient.urlshortner.agentic.service;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.agenticproficient.urlshortner.agentic.dto.ApprovalRequest;
import com.agenticproficient.urlshortner.agentic.dto.AuditEventResponse;
import com.agenticproficient.urlshortner.agentic.dto.SdlcExecutionResponse;
import com.agenticproficient.urlshortner.agentic.dto.StartSdlcExecutionRequest;
import com.agenticproficient.urlshortner.agentic.entity.AgenticAuditEvent;
import com.agenticproficient.urlshortner.agentic.entity.AgenticExecution;
import com.agenticproficient.urlshortner.agentic.mapper.AgenticResponseMapper;
import com.agenticproficient.urlshortner.agentic.model.AgentExecutionInput;
import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import com.agenticproficient.urlshortner.agentic.model.StepDefinition;
import com.agenticproficient.urlshortner.agentic.model.StepExecutionOutcome;
import com.agenticproficient.urlshortner.agentic.model.WorkflowContextDocument;
import com.agenticproficient.urlshortner.agentic.model.WorkflowEventType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStatus;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import com.agenticproficient.urlshortner.agentic.repository.AgenticAuditEventRepository;
import com.agenticproficient.urlshortner.agentic.repository.AgenticExecutionRepository;
import com.agenticproficient.urlshortner.config.AgenticWorkflowProperties;
import com.agenticproficient.urlshortner.exception.ApiErrorCode;
import com.agenticproficient.urlshortner.exception.DomainException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Application service that acts as the facade for SDLC workflow use cases.
 */
@Service
public class AgenticWorkflowService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgenticWorkflowService.class);

	private final AgenticAuditEventRepository auditEventRepository;
	private final AgenticExecutionRepository executionRepository;
	private final AgenticResponseMapper mapper;
	private final AgenticStepExecutor stepExecutor;
	private final AgenticWorkflowProperties properties;
	private final Clock clock;
	private final MeterRegistry meterRegistry;
	private final ObjectMapper objectMapper;
	private final PolicyGuardrailService policyGuardrailService;
	private final WorkflowGraph workflowGraph;

	public AgenticWorkflowService(AgenticAuditEventRepository auditEventRepository,
			AgenticExecutionRepository executionRepository, AgenticResponseMapper mapper,
			AgenticStepExecutor stepExecutor, AgenticWorkflowProperties properties, Clock clock,
			MeterRegistry meterRegistry, ObjectMapper objectMapper, PolicyGuardrailService policyGuardrailService,
			WorkflowGraph workflowGraph) {
		this.auditEventRepository = auditEventRepository;
		this.executionRepository = executionRepository;
		this.mapper = mapper;
		this.stepExecutor = stepExecutor;
		this.properties = properties;
		this.clock = clock;
		this.meterRegistry = meterRegistry;
		this.objectMapper = objectMapper;
		this.policyGuardrailService = policyGuardrailService;
		this.workflowGraph = workflowGraph;
	}

	public Mono<SdlcExecutionResponse> start(StartSdlcExecutionRequest request) {
		WorkflowContextDocument context = WorkflowContextDocument.fromRequirement(request.requirement());
		AgenticExecution execution = AgenticExecution.create(request.scenarioType(), request.requirement(),
				writeContext(context), clock.instant());
		return executionRepository.save(execution)
				.doOnNext(AgenticExecution::markPersisted)
				.flatMap(saved -> recordEvent(saved, WorkflowEventType.RUN_CREATED, null, "SDLC execution created",
						json(Map.of("scenarioType", request.scenarioType().name())))
						.then(runUntilBlockedOrComplete(saved)))
				.map(mapper::toResponse)
				.doOnSuccess(response -> LOGGER.info("Started SDLC execution executionId={} status={}",
						response.executionId(), response.status()));
	}

	public Mono<SdlcExecutionResponse> approve(UUID executionId, ApprovalRequest request) {
		return executionRepository.findById(executionId)
				.switchIfEmpty(Mono.error(() -> notFound(executionId)))
				.flatMap(execution -> {
					if (execution.getPendingApprovalStep() == null || execution.getPendingApprovalStep().isBlank()) {
						return Mono.error(DomainException.badRequest(ApiErrorCode.INVALID_STATE,
								"Execution is not waiting for approval"));
					}
					WorkflowStep pendingStep = WorkflowStep.valueOf(execution.getPendingApprovalStep());
					if (!Boolean.TRUE.equals(request.approved())) {
						execution.incrementFailureCount();
						execution.markSafeStopped(clock.instant());
						return executionRepository.save(execution)
								.flatMap(saved -> recordEvent(saved, WorkflowEventType.APPROVAL_REJECTED, pendingStep,
										"Human reviewer rejected the pending gate", approvalMetadata(request))
										.then(recordEvent(saved, WorkflowEventType.RUN_SAFE_STOPPED, pendingStep,
												"Execution safe-stopped after approval rejection", "{}"))
										.thenReturn(saved));
					}
					Set<WorkflowStep> approvedSteps = new LinkedHashSet<>(execution.approvedStepSet());
					approvedSteps.add(pendingStep);
					execution.setApprovedStepSet(approvedSteps);
					execution.markRunning(clock.instant());
					return executionRepository.save(execution)
							.flatMap(saved -> recordEvent(saved, WorkflowEventType.APPROVAL_GRANTED, pendingStep,
									"Human reviewer approved the pending gate", approvalMetadata(request))
									.then(runUntilBlockedOrComplete(saved)));
				})
				.map(mapper::toResponse);
	}

	public Mono<SdlcExecutionResponse> get(UUID executionId) {
		return executionRepository.findById(executionId)
				.switchIfEmpty(Mono.error(() -> notFound(executionId)))
				.map(mapper::toResponse);
	}

	public Flux<SdlcExecutionResponse> getAll() {
		return executionRepository.findRecent(properties.getMaxListSize())
				.map(mapper::toResponse)
				.doOnSubscribe(subscription -> LOGGER.info("Listing recent SDLC executions limit={}",
						properties.getMaxListSize()));
	}

	public Flux<AuditEventResponse> audit(UUID executionId) {
		return executionRepository.existsById(executionId)
				.flatMapMany(exists -> exists
						? auditEventRepository.findByExecutionIdOrderByCreatedAtAsc(executionId)
								.map(mapper::toResponse)
						: Flux.error(notFound(executionId)));
	}

	private Mono<AgenticExecution> runUntilBlockedOrComplete(AgenticExecution execution) {
		if (isTerminal(execution)) {
			return Mono.just(execution);
		}
		WorkflowContextDocument context = readContext(execution.getContextJson());
		ScenarioType scenarioType = ScenarioType.valueOf(execution.getScenarioType());
		List<StepDefinition> definitions = workflowGraph.definitionsFor(scenarioType, context);
		Set<WorkflowStep> completedSteps = execution.completedStepSet();

		if (workflowGraph.isComplete(definitions, completedSteps)) {
			return complete(execution);
		}

		List<StepDefinition> readySteps = workflowGraph.readySteps(definitions, completedSteps);
		if (readySteps.isEmpty()) {
			return rollback(execution, new IllegalStateException("Dependency graph has no executable path"));
		}

		List<StepDefinition> executableSteps = readySteps.stream()
				.filter(step -> !step.approvalRequired() || execution.approvedStepSet().contains(step.step()))
				.toList();

		if (!executableSteps.isEmpty()) {
			return executeReadySteps(execution, context, definitions, executableSteps)
					.flatMap(updatedExecution -> runUntilBlockedOrComplete(updatedExecution));
		}

		return readySteps.stream()
				.filter(step -> step.approvalRequired())
				.findFirst()
				.map(step -> awaitApproval(execution, step))
				.orElseGet(() -> rollback(execution, new IllegalStateException("No executable or approvable step found")));
	}

	private boolean isTerminal(AgenticExecution execution) {
		WorkflowStatus status = WorkflowStatus.valueOf(execution.getStatus());
		return status == WorkflowStatus.COMPLETED
				|| status == WorkflowStatus.SAFE_STOPPED
				|| status == WorkflowStatus.ROLLED_BACK;
	}

	private Mono<AgenticExecution> executeReadySteps(AgenticExecution execution, WorkflowContextDocument context,
			List<StepDefinition> definitions, List<StepDefinition> readySteps) {
		execution.markRunning(clock.instant());
		return executionRepository.save(execution)
				.flatMap(saved -> Flux.fromIterable(readySteps)
						.flatMapSequential(step -> executeStep(saved, context, definitions, step), properties.getParallelism())
						.collectList()
						.flatMap(outcomes -> applyOutcomes(saved, context, outcomes))
						.onErrorResume(PolicyViolationException.class, exception -> safeStop(saved, exception))
						.onErrorResume(exception -> rollback(saved, exception)));
	}

	private Mono<StepExecutionOutcome> executeStep(AgenticExecution execution, WorkflowContextDocument context,
			List<StepDefinition> definitions, StepDefinition stepDefinition) {
		AgentExecutionInput input = new AgentExecutionInput(execution, context, definitions);
		return recordEvent(execution, WorkflowEventType.STEP_STARTED, stepDefinition.step(),
				"Started " + stepDefinition.step().getLabel(), stepMetadata(stepDefinition))
				.then(policyGuardrailService.enforce(stepDefinition.step(), execution, context))
				.then(executeWithRetry(execution, input, stepDefinition, 0))
				.flatMap(outcome -> {
					Mono<Void> fallbackEvent = outcome.fallbackApplied()
							? recordEvent(execution, WorkflowEventType.FALLBACK_APPLIED, stepDefinition.step(),
									"Fallback output accepted after retry exhaustion", "{}")
							: Mono.empty();
					return fallbackEvent
							.then(recordEvent(execution, WorkflowEventType.STEP_COMPLETED, stepDefinition.step(),
									"Completed " + stepDefinition.step().getLabel(),
									json(Map.of("retryCount", outcome.retryCount(),
											"fallbackApplied", outcome.fallbackApplied()))))
							.thenReturn(outcome);
				});
	}

	private Mono<StepExecutionOutcome> executeWithRetry(AgenticExecution execution, AgentExecutionInput input,
			StepDefinition stepDefinition, int retryCount) {
		return stepExecutor.execute(stepDefinition.step(), input)
				.timeout(properties.getStepTimeout())
				.map(output -> new StepExecutionOutcome(output, retryCount, false))
				.onErrorResume(exception -> {
					if (exception instanceof PolicyViolationException) {
						return Mono.error(exception);
					}
					if (retryCount < properties.getMaxRetries()) {
						return recordEvent(execution, WorkflowEventType.RETRY_SCHEDULED, stepDefinition.step(),
								"Retry scheduled after step failure",
								json(Map.of("retryNumber", retryCount + 1, "reason", exception.getMessage())))
								.then(executeWithRetry(execution, input, stepDefinition, retryCount + 1));
					}
					return stepExecutor.fallback(stepDefinition.step(), input, exception)
							.timeout(properties.getStepTimeout())
							.map(output -> new StepExecutionOutcome(output, retryCount, true));
				});
	}

	private Mono<AgenticExecution> applyOutcomes(AgenticExecution execution, WorkflowContextDocument context,
			List<StepExecutionOutcome> outcomes) {
		Set<WorkflowStep> completedSteps = new LinkedHashSet<>(execution.completedStepSet());
		int retryCount = 0;
		for (StepExecutionOutcome outcome : outcomes) {
			context.addOutput(outcome.output());
			completedSteps.add(outcome.output().step());
			retryCount += outcome.retryCount();
			meterRegistry.counter("agentic.workflow.steps", "step", outcome.output().step().name(), "status", "completed")
					.increment();
		}
		execution.setCompletedStepSet(completedSteps);
		execution.setContextJson(writeContext(context));
		execution.incrementSuccessCount(outcomes.size());
		execution.incrementRetryCount(retryCount);
		execution.markRunning(clock.instant());
		return executionRepository.save(execution);
	}

	private Mono<AgenticExecution> awaitApproval(AgenticExecution execution, StepDefinition stepDefinition) {
		execution.markAwaitingApproval(stepDefinition.step(), clock.instant());
		return executionRepository.save(execution)
				.flatMap(saved -> recordEvent(saved, WorkflowEventType.APPROVAL_REQUIRED, stepDefinition.step(),
						"Human approval required before " + stepDefinition.step().getLabel(), stepMetadata(stepDefinition))
						.thenReturn(saved));
	}

	private Mono<AgenticExecution> complete(AgenticExecution execution) {
		execution.markCompleted(clock.instant());
		return executionRepository.save(execution)
				.flatMap(saved -> recordEvent(saved, WorkflowEventType.RUN_COMPLETED, null,
						"SDLC execution completed successfully", "{}")
						.thenReturn(saved))
				.doOnSuccess(this::recordTerminalMetrics);
	}

	private Mono<AgenticExecution> safeStop(AgenticExecution execution, PolicyViolationException exception) {
		execution.incrementFailureCount();
		execution.markSafeStopped(clock.instant());
		return executionRepository.save(execution)
				.flatMap(saved -> recordEvent(saved, WorkflowEventType.POLICY_DENIED, null, exception.getMessage(), "{}")
						.then(recordEvent(saved, WorkflowEventType.RUN_SAFE_STOPPED, null,
								"Execution safe-stopped by policy guardrail", "{}"))
						.thenReturn(saved))
				.doOnSuccess(this::recordTerminalMetrics);
	}

	private Mono<AgenticExecution> rollback(AgenticExecution execution, Throwable exception) {
		execution.incrementFailureCount();
		execution.incrementRollbackCount();
		execution.markRolledBack(clock.instant());
		return executionRepository.save(execution)
				.flatMap(saved -> recordEvent(saved, WorkflowEventType.ROLLBACK_TRIGGERED, null,
						"Execution rolled back after unrecoverable failure: " + exception.getMessage(), "{}")
						.thenReturn(saved))
				.doOnSuccess(this::recordTerminalMetrics);
	}

	private Mono<Void> recordEvent(AgenticExecution execution, WorkflowEventType eventType, WorkflowStep step,
			String message, String metadataJson) {
		return auditEventRepository.save(AgenticAuditEvent.create(execution, eventType, step, message, metadataJson, clock))
				.then();
	}

	private void recordTerminalMetrics(AgenticExecution execution) {
		meterRegistry.counter("agentic.workflow.runs", "status", execution.getStatus()).increment();
		Timer.builder("agentic.workflow.latency")
				.tag("status", execution.getStatus())
				.register(meterRegistry)
				.record(execution.getLatencyMs(), TimeUnit.MILLISECONDS);
	}

	private DomainException notFound(UUID executionId) {
		LOGGER.warn("SDLC execution not found executionId={}", executionId);
		return DomainException.notFound(ApiErrorCode.WORKFLOW_NOT_FOUND, "Workflow execution not found: " + executionId);
	}

	private WorkflowContextDocument readContext(String contextJson) {
		try {
			return objectMapper.readValue(contextJson, WorkflowContextDocument.class);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Unable to read workflow context", exception);
		}
	}

	private String writeContext(WorkflowContextDocument context) {
		return json(context);
	}

	private String stepMetadata(StepDefinition stepDefinition) {
		return json(Map.of(
				"dependencies", stepDefinition.dependencies().stream().map(WorkflowStep::name).toList(),
				"approvalRequired", stepDefinition.approvalRequired(),
				"entryGate", stepDefinition.entryGate(),
				"exitGate", stepDefinition.exitGate()));
	}

	private String approvalMetadata(ApprovalRequest request) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("approver", request.approver());
		metadata.put("comment", request.comment());
		return json(metadata);
	}

	private String json(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Unable to serialize workflow metadata", exception);
		}
	}
}
