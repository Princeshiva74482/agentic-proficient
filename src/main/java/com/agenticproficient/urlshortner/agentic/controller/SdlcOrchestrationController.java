package com.agenticproficient.urlshortner.agentic.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import com.agenticproficient.urlshortner.agentic.dto.ApprovalRequest;
import com.agenticproficient.urlshortner.agentic.dto.AuditEventResponse;
import com.agenticproficient.urlshortner.agentic.dto.SdlcExecutionResponse;
import com.agenticproficient.urlshortner.agentic.dto.StartSdlcExecutionRequest;
import com.agenticproficient.urlshortner.agentic.facade.AgenticOrchestrationFacade;
import com.agenticproficient.urlshortner.common.ApiPaths;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping(ApiPaths.SDLC_EXECUTIONS)
public class SdlcOrchestrationController {

	private final AgenticOrchestrationFacade orchestrationFacade;

	public SdlcOrchestrationController(AgenticOrchestrationFacade orchestrationFacade) {
		this.orchestrationFacade = orchestrationFacade;
	}

	@PostMapping
	public Mono<ResponseEntity<SdlcExecutionResponse>> start(@Valid @RequestBody StartSdlcExecutionRequest request) {
		return orchestrationFacade.start(request)
				.map(response -> ResponseEntity.created(URI.create(ApiPaths.SDLC_EXECUTIONS + "/"
						+ response.executionId())).body(response));
	}

	@GetMapping("/{executionId}")
	public Mono<SdlcExecutionResponse> get(@PathVariable UUID executionId) {
		return orchestrationFacade.get(executionId);
	}

	@PostMapping("/{executionId}/approvals")
	public Mono<SdlcExecutionResponse> approve(@PathVariable UUID executionId,
			@Valid @RequestBody ApprovalRequest request) {
		return orchestrationFacade.approve(executionId, request);
	}

	@GetMapping("/{executionId}/audit")
	public Flux<AuditEventResponse> audit(@PathVariable UUID executionId) {
		return orchestrationFacade.audit(executionId);
	}
}
