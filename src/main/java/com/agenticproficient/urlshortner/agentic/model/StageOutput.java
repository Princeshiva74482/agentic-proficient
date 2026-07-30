package com.agenticproficient.urlshortner.agentic.model;

import java.time.Instant;
import java.util.List;

public record StageOutput(
		WorkflowStep step,
		String summary,
		List<String> artifacts,
		List<String> decisions,
		List<String> risks,
		List<String> validationChecks,
		List<String> nextActions,
		Instant producedAt) {
}
