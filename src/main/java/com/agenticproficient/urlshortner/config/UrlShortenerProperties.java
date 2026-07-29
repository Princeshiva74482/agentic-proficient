package com.agenticproficient.urlshortner.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "url-shortener")
public class UrlShortenerProperties {

	@NotBlank
	private String baseUrl = "http://localhost:8080";

	@Min(4)
	@Max(16)
	private int codeLength = 8;

	@Min(1)
	@Max(20)
	private int maxCodeGenerationAttempts = 6;

	@Min(1)
	@Max(3650)
	private long defaultTtlDays = 365;

	@Valid
	private Security security = new Security();

	@Valid
	private Privacy privacy = new Privacy();

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public int getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(int codeLength) {
		this.codeLength = codeLength;
	}

	public int getMaxCodeGenerationAttempts() {
		return maxCodeGenerationAttempts;
	}

	public void setMaxCodeGenerationAttempts(int maxCodeGenerationAttempts) {
		this.maxCodeGenerationAttempts = maxCodeGenerationAttempts;
	}

	public long getDefaultTtlDays() {
		return defaultTtlDays;
	}

	public void setDefaultTtlDays(long defaultTtlDays) {
		this.defaultTtlDays = defaultTtlDays;
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public Privacy getPrivacy() {
		return privacy;
	}

	public void setPrivacy(Privacy privacy) {
		this.privacy = privacy;
	}

	public static class Security {

		private boolean allowPrivateHosts;

		@NotEmpty
		private List<String> allowedSchemes = new ArrayList<>(List.of("http", "https"));

		@NotEmpty
		private List<String> reservedAliases = new ArrayList<>(
				List.of("api", "actuator", "health", "docs", "swagger-ui", "h2-console", "favicon.ico"));

		public boolean isAllowPrivateHosts() {
			return allowPrivateHosts;
		}

		public void setAllowPrivateHosts(boolean allowPrivateHosts) {
			this.allowPrivateHosts = allowPrivateHosts;
		}

		public List<String> getAllowedSchemes() {
			return allowedSchemes;
		}

		public void setAllowedSchemes(List<String> allowedSchemes) {
			this.allowedSchemes = allowedSchemes;
		}

		public List<String> getReservedAliases() {
			return reservedAliases;
		}

		public void setReservedAliases(List<String> reservedAliases) {
			this.reservedAliases = reservedAliases;
		}
	}

	public static class Privacy {

		@NotBlank
		private String ipHashSalt = "change-me-in-production";

		public String getIpHashSalt() {
			return ipHashSalt;
		}

		public void setIpHashSalt(String ipHashSalt) {
			this.ipHashSalt = ipHashSalt;
		}
	}
}
