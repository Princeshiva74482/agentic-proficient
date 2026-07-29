package com.agenticproficient.urlshortner.agentic.model;

import java.util.Set;

public record StepDefinition(
		WorkflowStep step,
		Set<WorkflowStep> dependencies,
		boolean approvalRequired,
		String entryGate,
		String exitGate) {
}
