package com.agenticproficient.urlshortner.shortener.service;

import java.time.Instant;

public record CreateShortUrlCommand(String originalUrl, String customAlias, Instant expiresAt) {
}
