package com.agenticproficient.urlshortner.config;

import java.util.UUID;

import com.agenticproficient.urlshortner.common.ApplicationConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RequestIdWebFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String requestId = exchange.getRequest().getHeaders().getFirst(ApplicationConstants.X_REQUEST_ID);
		if (requestId == null || requestId.isBlank()) {
			requestId = UUID.randomUUID().toString();
		}
		exchange.getAttributes().put(ApplicationConstants.REQUEST_ID_ATTRIBUTE, requestId);
		exchange.getResponse().getHeaders().set(ApplicationConstants.X_REQUEST_ID, requestId);
		return chain.filter(exchange);
	}
}
