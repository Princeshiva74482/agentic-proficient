package com.agenticproficient.urlshortner.agentic.facade;

import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.dto.ApprovalRequest;
import com.agenticproficient.urlshortner.agentic.dto.AuditEventResponse;
import com.agenticproficient.urlshortner.agentic.dto.SdlcExecutionResponse;
import com.agenticproficient.urlshortner.agentic.dto.StartSdlcExecutionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AgenticOrchestrationFacade {

	Mono<SdlcExecutionResponse> start(StartSdlcExecutionRequest request);

	Mono<SdlcExecutionResponse> approve(UUID executionId, ApprovalRequest request);

	Mono<SdlcExecutionResponse> get(UUID executionId);

	Flux<AuditEventResponse> audit(UUID executionId);
}
