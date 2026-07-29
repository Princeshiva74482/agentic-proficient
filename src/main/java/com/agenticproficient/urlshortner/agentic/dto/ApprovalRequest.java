package com.agenticproficient.urlshortner.agentic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApprovalRequest(
		@NotNull Boolean approved,
		@NotBlank @Size(max = 120) String approver,
		@Size(max = 1000) String comment) {
}
