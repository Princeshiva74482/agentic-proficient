package com.agenticproficient.urlshortner.shortener.model;

import java.net.InetSocketAddress;

import com.agenticproficient.urlshortner.common.ApplicationConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

public record ClickContext(String userAgent, String referrer, String remoteAddress) {

	public static ClickContext from(ServerHttpRequest request) {
		HttpHeaders headers = request.getHeaders();
		return new ClickContext(
				headers.getFirst(ApplicationConstants.USER_AGENT),
				headers.getFirst(ApplicationConstants.REFERER),
				extractRemoteAddress(request));
	}

	private static String extractRemoteAddress(ServerHttpRequest request) {
		String forwardedFor = request.getHeaders().getFirst(ApplicationConstants.X_FORWARDED_FOR);
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		InetSocketAddress remoteAddress = request.getRemoteAddress();
		if (remoteAddress == null || remoteAddress.getAddress() == null) {
			return ApplicationConstants.UNKNOWN;
		}
		return remoteAddress.getAddress().getHostAddress();
	}
}
