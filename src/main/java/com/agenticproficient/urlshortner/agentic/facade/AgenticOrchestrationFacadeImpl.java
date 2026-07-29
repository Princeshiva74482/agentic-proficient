package com.agenticproficient.urlshortner.agentic.facade;

import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.dto.ApprovalRequest;
import com.agenticproficient.urlshortner.agentic.dto.AuditEventResponse;
import com.agenticproficient.urlshortner.agentic.dto.SdlcExecutionResponse;
import com.agenticproficient.urlshortner.agentic.dto.StartSdlcExecutionRequest;
import com.agenticproficient.urlshortner.agentic.mapper.AgenticResponseMapper;
import com.agenticproficient.urlshortner.agentic.service.AgenticWorkflowService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AgenticOrchestrationFacadeImpl implements AgenticOrchestrationFacade {

	private final AgenticResponseMapper mapper;
	private final AgenticWorkflowService workflowService;

	public AgenticOrchestrationFacadeImpl(AgenticResponseMapper mapper, AgenticWorkflowService workflowService) {
		this.mapper = mapper;
		this.workflowService = workflowService;
	}

	@Override
	public Mono<SdlcExecutionResponse> start(StartSdlcExecutionRequest request) {
		return workflowService.start(request).map(mapper::toResponse);
	}

	@Override
	public Mono<SdlcExecutionResponse> approve(UUID executionId, ApprovalRequest request) {
		return workflowService.approve(executionId, request).map(mapper::toResponse);
	}

	@Override
	public Mono<SdlcExecutionResponse> get(UUID executionId) {
		return workflowService.get(executionId).map(mapper::toResponse);
	}

	@Override
	public Flux<AuditEventResponse> audit(UUID executionId) {
		return workflowService.audit(executionId).map(mapper::toResponse);
	}
}
