package com.agenticproficient.urlshortner.agentic.service;

import java.util.List;
import java.util.Locale;

import com.agenticproficient.urlshortner.agentic.entity.AgenticExecution;
import com.agenticproficient.urlshortner.agentic.model.WorkflowContextDocument;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PolicyGuardrailService {

	private static final List<String> BLOCKED_PHRASES = List.of(
			"bypass approval",
			"disable tests",
			"delete production",
			"exfiltrate",
			"hardcode secret",
			"ignore security");

	public Mono<Void> enforce(WorkflowStep step, AgenticExecution execution, WorkflowContextDocument context) {
		String requirement = execution.getRequirementText().toLowerCase(Locale.ROOT);
		return BLOCKED_PHRASES.stream()
				.filter(requirement::contains)
				.findFirst()
				.<Mono<Void>>map(phrase -> Mono.error(new PolicyViolationException(
						"Policy guardrail denied execution because requirement contains: " + phrase)))
				.orElseGet(() -> enforceReleaseGate(step, context));
	}

	private Mono<Void> enforceReleaseGate(WorkflowStep step, WorkflowContextDocument context) {
		if (step != WorkflowStep.RELEASE_READINESS) {
			return Mono.empty();
		}
		boolean testingComplete = context.getStageOutputs().containsKey(WorkflowStep.TEST_PLAN.name());
		boolean documentationComplete = context.getStageOutputs().containsKey(WorkflowStep.DOCUMENTATION.name());
		if (!testingComplete || !documentationComplete) {
			return Mono.error(new PolicyViolationException("Release readiness requires completed testing and documentation paths"));
		}
		return Mono.empty();
	}
}
