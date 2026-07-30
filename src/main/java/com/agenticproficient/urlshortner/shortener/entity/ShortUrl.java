package com.agenticproficient.urlshortner.shortener.entity;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("short_urls")
public class ShortUrl implements Persistable<UUID> {

	@Id
	@Column("id")
	private UUID id;

	@Column("original_url")
	private String originalUrl;

	@Column("short_code")
	private String shortCode;

	@Column("created_at")
	private Instant createdAt;

	@Column("updated_at")
	private Instant updatedAt;

	@Column("expires_at")
	private Instant expiresAt;

	@Column("active")
	private boolean active;

	@Column("access_count")
	private long accessCount;

	@Transient
	private boolean newEntity;

	public ShortUrl() {
	}

	public ShortUrl(UUID id, String originalUrl, String shortCode, Instant createdAt, Instant updatedAt,
			Instant expiresAt, boolean active, long accessCount) {
		this.id = id;
		this.originalUrl = originalUrl;
		this.shortCode = shortCode;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.expiresAt = expiresAt;
		this.active = active;
		this.accessCount = accessCount;
	}

	public static ShortUrl create(String originalUrl, String shortCode, Instant expiresAt, Clock clock) {
		Instant now = clock.instant();
		ShortUrl shortUrl = new ShortUrl(UUID.randomUUID(), originalUrl, shortCode, now, now, expiresAt, true, 0);
		shortUrl.newEntity = true;
		return shortUrl;
	}

	public boolean isExpired(Clock clock) {
		return expiresAt != null && !expiresAt.isAfter(clock.instant());
	}

	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return newEntity;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getAccessCount() {
		return accessCount;
	}

	public void setAccessCount(long accessCount) {
		this.accessCount = accessCount;
	}
}
