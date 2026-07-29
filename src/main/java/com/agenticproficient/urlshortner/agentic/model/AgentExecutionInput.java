package com.agenticproficient.urlshortner.agentic.model;

import java.util.List;

import com.agenticproficient.urlshortner.agentic.entity.AgenticExecution;

public record AgentExecutionInput(
		AgenticExecution execution,
		WorkflowContextDocument context,
		List<StepDefinition> dependencyGraph) {
}
