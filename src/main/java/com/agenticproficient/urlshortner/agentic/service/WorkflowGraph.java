package com.agenticproficient.urlshortner.agentic.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import com.agenticproficient.urlshortner.agentic.model.StepDefinition;
import com.agenticproficient.urlshortner.agentic.model.WorkflowContextDocument;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import org.springframework.stereotype.Component;

@Component
public class WorkflowGraph {

	public List<StepDefinition> definitionsFor(ScenarioType scenarioType, WorkflowContextDocument context) {
		List<StepDefinition> definitions = new ArrayList<>();
		boolean ambiguous = scenarioType == ScenarioType.AMBIGUOUS;
		boolean brownfield = scenarioType == ScenarioType.BROWNFIELD || containsBrownfieldSignal(context);

		definitions.add(new StepDefinition(WorkflowStep.REQUIREMENT_ANALYSIS, Set.of(), false,
				"Input requirement is captured", "Normalized problem and acceptance criteria exist"));

		if (ambiguous) {
			definitions.add(new StepDefinition(WorkflowStep.CLARIFICATION_REVIEW,
					Set.of(WorkflowStep.REQUIREMENT_ANALYSIS), true,
					"Ambiguities were found", "Human accepts assumptions or stops execution"));
		}

		if (brownfield) {
			definitions.add(new StepDefinition(WorkflowStep.IMPACT_ANALYSIS,
					Set.of(WorkflowStep.REQUIREMENT_ANALYSIS), false,
					"Existing system context is available", "Impacted modules and regression risks are identified"));
		}

		Set<WorkflowStep> architectureDependencies = new LinkedHashSet<>();
		architectureDependencies.add(WorkflowStep.REQUIREMENT_ANALYSIS);
		if (ambiguous) {
			architectureDependencies.add(WorkflowStep.CLARIFICATION_REVIEW);
		}
		if (brownfield) {
			architectureDependencies.add(WorkflowStep.IMPACT_ANALYSIS);
		}

		definitions.add(new StepDefinition(WorkflowStep.ARCHITECTURE_DESIGN, architectureDependencies, false,
				"Requirements and impact context are stable", "Architecture, data flow, and key decisions are documented"));
		definitions.add(new StepDefinition(WorkflowStep.IMPLEMENTATION_PLAN, Set.of(WorkflowStep.ARCHITECTURE_DESIGN),
				true, "Design is available", "Implementation tasks are reviewable before code changes"));
		definitions.add(new StepDefinition(WorkflowStep.TEST_PLAN, Set.of(WorkflowStep.IMPLEMENTATION_PLAN), false,
				"Implementation plan is approved", "Validation strategy covers unit, integration, and risk tests"));
		definitions.add(new StepDefinition(WorkflowStep.DOCUMENTATION, Set.of(WorkflowStep.IMPLEMENTATION_PLAN), false,
				"Implementation plan is approved", "Setup, API, architecture, and scenario docs are planned"));
		definitions.add(new StepDefinition(WorkflowStep.RELEASE_READINESS,
				Set.of(WorkflowStep.TEST_PLAN, WorkflowStep.DOCUMENTATION), true,
				"Validation and documentation paths are synchronized", "Release gates and known limitations are explicit"));
		return definitions;
	}

	public List<StepDefinition> readySteps(List<StepDefinition> definitions, Set<WorkflowStep> completedSteps) {
		return definitions.stream()
				.filter(definition -> !completedSteps.contains(definition.step()))
				.filter(definition -> completedSteps.containsAll(definition.dependencies()))
				.toList();
	}

	public boolean isComplete(List<StepDefinition> definitions, Set<WorkflowStep> completedSteps) {
		return definitions.stream().allMatch(definition -> completedSteps.contains(definition.step()));
	}

	private boolean containsBrownfieldSignal(WorkflowContextDocument context) {
		String problem = context.getNormalizedProblem() == null ? "" : context.getNormalizedProblem().toLowerCase(Locale.ROOT);
		return problem.contains("existing") || problem.contains("brownfield") || problem.contains("refactor");
	}
}
