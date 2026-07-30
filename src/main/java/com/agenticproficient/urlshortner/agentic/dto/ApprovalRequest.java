package com.agenticproficient.urlshortner.agentic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Human approval decision for the currently pending workflow gate.")
public record ApprovalRequest(
		@NotNull
		@Schema(description = "Approve when true; reject and safe-stop when false", example = "true",
				requiredMode = Schema.RequiredMode.REQUIRED)
		Boolean approved,
		@NotBlank @Size(max = 120)
		@Schema(description = "Human reviewer identity", example = "reviewer@example.com",
				requiredMode = Schema.RequiredMode.REQUIRED)
		String approver,
		@Size(max = 1000)
		@Schema(description = "Reviewer comment captured in audit metadata",
				example = "Implementation plan reviewed and approved.")
		String comment) {
}
