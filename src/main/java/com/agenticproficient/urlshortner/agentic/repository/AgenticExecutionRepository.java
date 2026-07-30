package com.agenticproficient.urlshortner.agentic.repository;

import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.entity.AgenticExecution;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AgenticExecutionRepository extends ReactiveCrudRepository<AgenticExecution, UUID> {

	@Query("SELECT * FROM \"agentic_executions\" ORDER BY \"updated_at\" DESC LIMIT :limit")
	Flux<AgenticExecution> findRecent(int limit);
}
