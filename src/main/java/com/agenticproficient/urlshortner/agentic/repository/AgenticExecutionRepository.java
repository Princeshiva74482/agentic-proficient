package com.agenticproficient.urlshortner.agentic.repository;

import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.entity.AgenticExecution;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AgenticExecutionRepository extends ReactiveCrudRepository<AgenticExecution, UUID> {
}
