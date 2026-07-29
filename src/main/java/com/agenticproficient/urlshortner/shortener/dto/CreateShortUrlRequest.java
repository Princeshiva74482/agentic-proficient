package com.agenticproficient.urlshortner.shortener.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(
		@NotBlank @Size(max = 2048) String originalUrl,
		@Size(min = 4, max = 32) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String customAlias,
		Instant expiresAt) {
}
