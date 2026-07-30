package com.agenticproficient.urlshortner.agentic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to start a governed agentic SDLC execution.")
public record StartSdlcExecutionRequest(
		@NotNull
		@Schema(description = "Scenario type that controls the workflow graph",
				example = "GREENFIELD", requiredMode = Schema.RequiredMode.REQUIRED)
		ScenarioType scenarioType,
		@NotBlank @Size(max = 8000)
		@Schema(description = "Natural-language engineering requirement",
				example = "Build a production-grade reactive URL shortener with analytics.",
				requiredMode = Schema.RequiredMode.REQUIRED)
		String requirement) {
}
