package com.agenticproficient.urlshortner.agentic.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import com.agenticproficient.urlshortner.agentic.dto.ApprovalRequest;
import com.agenticproficient.urlshortner.agentic.dto.AuditEventResponse;
import com.agenticproficient.urlshortner.agentic.dto.SdlcExecutionResponse;
import com.agenticproficient.urlshortner.agentic.dto.StartSdlcExecutionRequest;
import com.agenticproficient.urlshortner.agentic.service.AgenticWorkflowService;
import com.agenticproficient.urlshortner.common.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
@RestController
@Tag(name = "Agentic SDLC Orchestration",
		description = "Governed requirement-to-release workflow with graph execution, approvals, audit, and metrics.")
public class SdlcOrchestrationController {

	private final AgenticWorkflowService workflowService;

	public SdlcOrchestrationController(AgenticWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	@PostMapping(path = ApiPaths.SDLC_EXECUTIONS)
	@Operation(summary = "Start an SDLC execution",
			description = "Creates an agentic workflow execution and advances until completion or the next approval gate.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Execution created",
					content = @Content(schema = @Schema(implementation = SdlcExecutionResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid scenario type or requirement",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Mono<ResponseEntity<SdlcExecutionResponse>> start(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Workflow start request", required = true,
					content = @Content(schema = @Schema(implementation = StartSdlcExecutionRequest.class)))
			@Valid @RequestBody StartSdlcExecutionRequest request) {
		return workflowService.start(request)
				.map(response -> ResponseEntity.created(URI.create(ApiPaths.SDLC_EXECUTIONS + "/"
						+ response.executionId())).body(response));
	}

	@GetMapping(path = ApiPaths.SDLC_EXECUTIONS)
	@Operation(summary = "List SDLC executions",
			description = "Returns recent SDLC executions, newest updated first, so callers can discover valid execution IDs.")
	@ApiResponse(responseCode = "200", description = "Recent executions",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = SdlcExecutionResponse.class))))
	public Flux<SdlcExecutionResponse> getAll() {
		return workflowService.getAll();
	}

	@GetMapping(path = ApiPaths.SDLC_EXECUTIONS + "/{executionId}")
	@Operation(summary = "Get SDLC execution state",
			description = "Returns current execution status, completed steps, pending gate, context, and reliability metrics.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Execution state",
					content = @Content(schema = @Schema(implementation = SdlcExecutionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Execution not found",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Mono<SdlcExecutionResponse> get(
			@Parameter(description = "Workflow execution id returned by POST /api/v1/sdlc/executions or GET /api/v1/sdlc/executions",
					example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
					required = true)
			@PathVariable UUID executionId) {
		return workflowService.get(executionId);
	}

	@PostMapping(path = ApiPaths.SDLC_EXECUTIONS + "/{executionId}/approvals")
	@Operation(summary = "Approve or reject pending workflow gate",
			description = "Approves the current human checkpoint and resumes execution, or rejects it and safe-stops.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Approval decision applied",
					content = @Content(schema = @Schema(implementation = SdlcExecutionResponse.class))),
			@ApiResponse(responseCode = "400", description = "Execution is not waiting for approval or payload is invalid",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "404", description = "Execution not found",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Mono<SdlcExecutionResponse> approve(
			@Parameter(description = "Workflow execution id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
					required = true)
			@PathVariable UUID executionId,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(
					description = "Human approval decision", required = true,
					content = @Content(schema = @Schema(implementation = ApprovalRequest.class)))
			@Valid @RequestBody ApprovalRequest request) {
		return workflowService.approve(executionId, request);
	}

	@GetMapping(path = ApiPaths.SDLC_EXECUTIONS + "/{executionId}/audit")
	@Operation(summary = "Get SDLC audit events",
			description = "Returns ordered audit-grade event history for a workflow execution.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Audit events",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuditEventResponse.class)))),
			@ApiResponse(responseCode = "404", description = "Execution not found",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public Flux<AuditEventResponse> audit(
			@Parameter(description = "Workflow execution id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
					required = true)
			@PathVariable UUID executionId) {
		return workflowService.audit(executionId);
	}
}
