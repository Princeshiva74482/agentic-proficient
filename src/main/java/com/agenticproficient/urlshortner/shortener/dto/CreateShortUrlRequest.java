package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to create a shortened URL. Omit customAlias to generate a random short code.")
public record CreateShortUrlRequest(
		@NotBlank @Size(max = 2048)
		@Schema(description = "Public HTTP or HTTPS URL to shorten", example = "https://example.com/docs",
				requiredMode = Schema.RequiredMode.REQUIRED)
		String originalUrl,
		@Size(min = 4, max = 32) @Pattern(regexp = "^[A-Za-z0-9_-]+$")
		@Schema(description = "Optional custom alias using letters, numbers, hyphen, or underscore", example = "docs2026")
		String customAlias,
		@Schema(description = "Optional UTC expiration. Defaults from application configuration when omitted.",
				example = "2027-12-31T23:59:59Z")
		Instant expiresAt) {
}
