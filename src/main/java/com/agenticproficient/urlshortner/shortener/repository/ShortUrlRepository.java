package com.agenticproficient.urlshortner.shortener.repository;

import java.util.UUID;

import com.agenticproficient.urlshortner.shortener.entity.ShortUrl;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShortUrlRepository extends ReactiveCrudRepository<ShortUrl, UUID> {

	Mono<ShortUrl> findByShortCode(String shortCode);

	Mono<Boolean> existsByShortCode(String shortCode);

	@Query("SELECT * FROM \"short_urls\" WHERE \"active\" = TRUE AND (\"expires_at\" IS NULL OR \"expires_at\" > CURRENT_TIMESTAMP) ORDER BY \"created_at\" DESC LIMIT :limit")
	Flux<ShortUrl> findAvailable(int limit);

	@Modifying
	@Query("UPDATE \"short_urls\" SET \"access_count\" = \"access_count\" + 1, \"updated_at\" = CURRENT_TIMESTAMP WHERE \"id\" = :id")
	Mono<Integer> incrementAccessCount(UUID id);
}
