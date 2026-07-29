package com.agenticproficient.urlshortner.shortener.repository;

import java.time.Instant;
import java.util.UUID;

import com.agenticproficient.urlshortner.shortener.entity.ClickEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClickEventRepository extends ReactiveCrudRepository<ClickEvent, UUID> {

	@Query("SELECT COUNT(DISTINCT \"ip_hash\") FROM \"click_events\" WHERE \"short_url_id\" = :shortUrlId AND \"ip_hash\" IS NOT NULL")
	Mono<Long> countUniqueVisitors(UUID shortUrlId);

	Mono<ClickEvent> findFirstByShortUrlIdOrderByClickedAtDesc(UUID shortUrlId);

	@Query("SELECT * FROM \"click_events\" WHERE \"short_url_id\" = :shortUrlId ORDER BY \"clicked_at\" DESC LIMIT 10")
	Flux<ClickEvent> findRecentClicks(UUID shortUrlId);
}
