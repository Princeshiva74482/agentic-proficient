package com.agenticproficient.urlshortner.shortener.service;

import java.security.SecureRandom;

import com.agenticproficient.urlshortner.common.ApplicationConstants;
import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

	private final SecureRandom secureRandom = new SecureRandom();

	public String generate(int length) {
		String alphabet = ApplicationConstants.BASE62_ALPHABET;
		StringBuilder builder = new StringBuilder(length);
		for (int index = 0; index < length; index++) {
			builder.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
		}
		return builder.toString();
	}
}
