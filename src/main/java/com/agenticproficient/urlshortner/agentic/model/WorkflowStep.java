package com.agenticproficient.urlshortner.agentic.model;

public enum WorkflowStep {
	REQUIREMENT_ANALYSIS("Requirement Understanding"),
	CLARIFICATION_REVIEW("Clarification Review"),
	IMPACT_ANALYSIS("Brownfield Impact Analysis"),
	ARCHITECTURE_DESIGN("Architecture Design"),
	IMPLEMENTATION_PLAN("Implementation Planning"),
	TEST_PLAN("Validation Planning"),
	DOCUMENTATION("Documentation"),
	RELEASE_READINESS("Release Readiness");

	private final String label;

	WorkflowStep(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
