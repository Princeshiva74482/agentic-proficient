package com.agenticproficient.urlshortner.shortener.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.agenticproficient.urlshortner.config.UrlShortenerProperties;
import org.springframework.stereotype.Service;

@Service
public class RequestFingerprintService {

	private final UrlShortenerProperties properties;

	public RequestFingerprintService(UrlShortenerProperties properties) {
		this.properties = properties;
	}

	public String hashRemoteAddress(String remoteAddress) {
		if (remoteAddress == null || remoteAddress.isBlank()) {
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String value = properties.getPrivacy().getIpHashSalt() + ":" + remoteAddress.trim();
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 hashing is unavailable", exception);
		}
	}
}
