package com.agenticproficient.urlshortner.agentic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.agenticproficient.urlshortner.agentic.model.ScenarioType;

public record StartSdlcExecutionRequest(
		@NotNull ScenarioType scenarioType,
		@NotBlank @Size(max = 8000) String requirement) {
}
