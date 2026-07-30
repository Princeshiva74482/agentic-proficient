package com.agenticproficient.urlshortner.agentic.service;

import java.time.Clock;
import java.util.List;

import com.agenticproficient.urlshortner.agentic.model.AgentExecutionInput;
import com.agenticproficient.urlshortner.agentic.model.StageOutput;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AgenticStepExecutor {

	private final Clock clock;

	public AgenticStepExecutor(Clock clock) {
		this.clock = clock;
	}

	public Mono<StageOutput> execute(WorkflowStep step, AgentExecutionInput input) {
		return Mono.fromCallable(() -> {
			if (input.execution().getRequirementText().contains("[fail:" + step.name() + "]")) {
				throw new IllegalStateException("Injected step failure for resilience validation");
			}
			return switch (step) {
				case REQUIREMENT_ANALYSIS -> requirementAnalysis(input);
				case CLARIFICATION_REVIEW -> clarificationReview(input);
				case IMPACT_ANALYSIS -> impactAnalysis(input);
				case ARCHITECTURE_DESIGN -> architectureDesign(input);
				case IMPLEMENTATION_PLAN -> implementationPlan(input);
				case TEST_PLAN -> testPlan(input);
				case DOCUMENTATION -> documentation(input);
				case RELEASE_READINESS -> releaseReadiness(input);
			};
		});
	}

	public Mono<StageOutput> fallback(WorkflowStep step, AgentExecutionInput input, Throwable failure) {
		return Mono.just(output(step,
				"Fallback output generated after bounded retries failed: " + failure.getMessage(),
				List.of("Fallback checklist", "Manual review packet"),
				List.of("Execution moved to conservative fallback instead of continuing with partial automated output"),
				List.of("Fallback may reduce implementation specificity and requires stronger human review"),
				List.of("Human reviewer must verify fallback artifact before release"),
				List.of("Review fallback artifact", "Decide whether to retry with refined input")));
	}

	private StageOutput requirementAnalysis(AgentExecutionInput input) {
		return output(WorkflowStep.REQUIREMENT_ANALYSIS,
				"Normalized the assignment into a reactive URL shortener plus governed SDLC orchestration system.",
				List.of("Problem statement", "Acceptance criteria", "Ambiguity register"),
				List.of("Use WebFlux/R2DBC for non-blocking APIs", "Use H2/Flyway for reproducible prototype storage",
						"Expose reviewable orchestration state through APIs"),
				List.of("H2 is ephemeral and not appropriate as production storage", "Ambiguous requirements can cause unsafe autonomy"),
				List.of("All API outputs must be testable", "Human checkpoints are required before high-impact stages"),
				List.of("Design architecture", "Create implementation plan"));
	}

	private StageOutput clarificationReview(AgentExecutionInput input) {
		return output(WorkflowStep.CLARIFICATION_REVIEW,
				"Captured assumptions for ambiguous work and stopped for human approval before downstream execution.",
				List.of("Clarification questions", "Assumption ledger"),
				List.of("Proceed only after assumptions are accepted"),
				List.of("Incorrect assumptions can invalidate design decisions"),
				List.of("Approval record is required before architecture execution"),
				List.of("Approve assumptions or reject execution"));
	}

	private StageOutput impactAnalysis(AgentExecutionInput input) {
		return output(WorkflowStep.IMPACT_ANALYSIS,
				"Identified brownfield impact areas across API, persistence, validation, tests, and documentation.",
				List.of("Impacted module map", "Regression-risk register"),
				List.of("Controllers depend on application services", "Persistence remains isolated behind repositories"),
				List.of("Existing behavior may regress if aliases or redirects change"),
				List.of("Integration tests must cover create, redirect, analytics, and workflow approvals"),
				List.of("Use architecture findings to update design"));
	}

	private StageOutput architectureDesign(AgentExecutionInput input) {
		return output(WorkflowStep.ARCHITECTURE_DESIGN,
				"Designed a modular WebFlux application with service-level use cases, reactive repositories, and explicit orchestration graph.",
				List.of("Component model", "Dependency graph", "Control-flow decisions"),
				List.of("Application services are the controller boundary", "MapStruct maps persistence entities to API responses",
						"Audit events preserve decision lineage"),
				List.of("Reactive H2 is suitable for prototype only", "Redirect analytics must fail open to protect availability"),
				List.of("Graph gates must prevent unapproved implementation and release actions"),
				List.of("Prepare implementation plan"));
	}

	private StageOutput implementationPlan(AgentExecutionInput input) {
		return output(WorkflowStep.IMPLEMENTATION_PLAN,
				"Decomposed implementation into URL APIs, persistence migrations, orchestration execution, tests, and docs.",
				List.of("Task breakdown", "Change-control checklist"),
				List.of("Use bounded retries and fallback for workflow steps",
						"Persist execution state and audit events in H2 for traceability"),
				List.of("Code generation must not bypass validation, tests, or approvals"),
				List.of("Compile, unit, integration, and API validation are required before release"),
				List.of("Run testing and documentation paths in parallel"));
	}

	private StageOutput testPlan(AgentExecutionInput input) {
		return output(WorkflowStep.TEST_PLAN,
				"Defined validation across URL behavior, policy guardrails, workflow gates, and persistence migrations.",
				List.of("Unit-test plan", "WebFlux integration-test plan", "Risk-based validation checklist"),
				List.of("Test through public APIs instead of internal implementation details"),
				List.of("Environment without a JDK cannot execute Gradle validation"),
				List.of("Create URL, redirect tracking, analytics, approval, and safe-stop cases must pass"),
				List.of("Execute automated test suite when JDK is available"));
	}

	private StageOutput documentation(AgentExecutionInput input) {
		return output(WorkflowStep.DOCUMENTATION,
				"Prepared deliverable documentation for setup, architecture, scenarios, risks, and API contracts.",
				List.of("README", "Architecture overview", "Scenario walkthroughs", "OpenAPI contract"),
				List.of("Document known limits explicitly rather than hiding prototype constraints"),
				List.of("Documentation can drift unless included in review gates"),
				List.of("Docs must include setup, testing, trade-offs, and assumptions"),
				List.of("Finalize release summary"));
	}

	private StageOutput releaseReadiness(AgentExecutionInput input) {
		return output(WorkflowStep.RELEASE_READINESS,
				"Prepared release readiness summary with completed gates, known limitations, and validation evidence.",
				List.of("Release checklist", "Risk acceptance record"),
				List.of("Human owns final quality decision", "Agents operate inside approved autonomy boundaries"),
				List.of("Prototype storage and local environment constraints remain release limitations"),
				List.of("All completed stages must have audit events and traceable outputs"),
				List.of("Deliver final engineering summary"));
	}

	private StageOutput output(WorkflowStep step, String summary, List<String> artifacts, List<String> decisions,
			List<String> risks, List<String> validationChecks, List<String> nextActions) {
		return new StageOutput(step, summary, artifacts, decisions, risks, validationChecks, nextActions, clock.instant());
	}
}
