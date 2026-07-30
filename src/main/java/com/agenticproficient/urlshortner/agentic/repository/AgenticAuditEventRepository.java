package com.agenticproficient.urlshortner.agentic.repository;

import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.entity.AgenticAuditEvent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AgenticAuditEventRepository extends ReactiveCrudRepository<AgenticAuditEvent, UUID> {

	Flux<AgenticAuditEvent> findByExecutionIdOrderByCreatedAtAsc(UUID executionId);
}
