package com.agenticproficient.urlshortner.shortener.entity;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("click_events")
public class ClickEvent implements Persistable<UUID> {

	@Id
	@Column("id")
	private UUID id;

	@Column("short_url_id")
	private UUID shortUrlId;

	@Column("short_code")
	private String shortCode;

	@Column("user_agent")
	private String userAgent;

	@Column("referrer")
	private String referrer;

	@Column("ip_hash")
	private String ipHash;

	@Column("clicked_at")
	private Instant clickedAt;

	@Transient
	private boolean newEntity;

	public ClickEvent() {
	}

	public ClickEvent(UUID id, UUID shortUrlId, String shortCode, String userAgent, String referrer, String ipHash,
			Instant clickedAt) {
		this.id = id;
		this.shortUrlId = shortUrlId;
		this.shortCode = shortCode;
		this.userAgent = userAgent;
		this.referrer = referrer;
		this.ipHash = ipHash;
		this.clickedAt = clickedAt;
	}

	public static ClickEvent create(ShortUrl shortUrl, String userAgent, String referrer, String ipHash, Clock clock) {
		ClickEvent clickEvent = new ClickEvent(UUID.randomUUID(), shortUrl.getId(), shortUrl.getShortCode(), userAgent, referrer, ipHash,
				clock.instant());
		clickEvent.newEntity = true;
		return clickEvent;
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

	public UUID getShortUrlId() {
		return shortUrlId;
	}

	public void setShortUrlId(UUID shortUrlId) {
		this.shortUrlId = shortUrlId;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public String getIpHash() {
		return ipHash;
	}

	public void setIpHash(String ipHash) {
		this.ipHash = ipHash;
	}

	public Instant getClickedAt() {
		return clickedAt;
	}

	public void setClickedAt(Instant clickedAt) {
		this.clickedAt = clickedAt;
	}
}
