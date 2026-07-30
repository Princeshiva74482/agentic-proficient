package com.agenticproficient.urlshortner;

import java.util.List;
import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.dto.ApprovalRequest;
import com.agenticproficient.urlshortner.agentic.dto.SdlcExecutionResponse;
import com.agenticproficient.urlshortner.agentic.dto.StartSdlcExecutionRequest;
import com.agenticproficient.urlshortner.agentic.model.ScenarioType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStatus;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import com.agenticproficient.urlshortner.shortener.dto.CreateShortUrlRequest;
import com.agenticproficient.urlshortner.shortener.dto.UrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortenerIntegrationTests {

	@Autowired
	private ApplicationContext applicationContext;

	private WebTestClient webTestClient;

	@BeforeEach
	void setUp() {
		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
	}

	@Test
	void createsRedirectsAndReportsAnalytics() {
		String alias = "case" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		UrlResponse created = webTestClient.post()
				.uri("/api/v1/urls")
				.bodyValue(new CreateShortUrlRequest("https://example.com/docs", alias, null))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(UrlResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(created).isNotNull();
		assertThat(created.shortCode()).isEqualTo(alias);
		assertThat(created.shortUrl()).endsWith("/" + alias);

		webTestClient.get()
				.uri("/{alias}", alias)
				.header("User-Agent", "integration-test")
				.exchange()
				.expectStatus().isFound()
				.expectHeader().valueEquals("Location", "https://example.com/docs");

		webTestClient.get()
				.uri("/api/v1/urls/{alias}/analytics", alias)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.shortCode").isEqualTo(alias)
				.jsonPath("$.totalClicks").isEqualTo(1)
				.jsonPath("$.recentClicks[0].userAgent").isEqualTo("integration-test");
	}

	@Test
	void rejectsPrivateHostTargetsByDefault() {
		webTestClient.post()
				.uri("/api/v1/urls")
				.bodyValue(new CreateShortUrlRequest("http://localhost:8081/private", null, null))
				.exchange()
				.expectStatus().isBadRequest()
				.expectBody()
				.jsonPath("$.code").isEqualTo("INVALID_URL");
	}

	@Test
	void listsAvailableShortUrlsWithoutInput() {
		String alias = "list" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		webTestClient.post()
				.uri("/api/v1/urls")
				.bodyValue(new CreateShortUrlRequest("https://example.com/list", alias, null))
				.exchange()
				.expectStatus().isCreated();

		List<UrlResponse> urls = webTestClient.get()
				.uri("/api/v1/urls")
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(UrlResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(urls)
				.isNotNull()
				.anyMatch(url -> alias.equals(url.shortCode()));
	}

	@Test
	void orchestratesGreenfieldWorkflowThroughApprovalGates() {
		SdlcExecutionResponse started = webTestClient.post()
				.uri("/api/v1/sdlc/executions")
				.bodyValue(new StartSdlcExecutionRequest(ScenarioType.GREENFIELD,
						"Build a production-grade reactive URL shortener with analytics."))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(SdlcExecutionResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(started).isNotNull();
		assertThat(started.status()).isEqualTo(WorkflowStatus.AWAITING_APPROVAL);
		assertThat(started.pendingApprovalStep()).isEqualTo(WorkflowStep.IMPLEMENTATION_PLAN);

		SdlcExecutionResponse implementationApproved = approve(started.executionId(), "Implementation plan reviewed");

		assertThat(implementationApproved.status()).isEqualTo(WorkflowStatus.AWAITING_APPROVAL);
		assertThat(implementationApproved.pendingApprovalStep()).isEqualTo(WorkflowStep.RELEASE_READINESS);
		assertThat(implementationApproved.completedSteps()).contains(
				WorkflowStep.REQUIREMENT_ANALYSIS,
				WorkflowStep.ARCHITECTURE_DESIGN,
				WorkflowStep.IMPLEMENTATION_PLAN,
				WorkflowStep.TEST_PLAN,
				WorkflowStep.DOCUMENTATION);

		SdlcExecutionResponse completed = approve(started.executionId(), "Release gate accepted");

		assertThat(completed.status()).isEqualTo(WorkflowStatus.COMPLETED);
		assertThat(completed.pendingApprovalStep()).isNull();
		assertThat(completed.completedSteps()).contains(WorkflowStep.RELEASE_READINESS);
		assertThat(completed.reliabilityMetrics().successRate()).isEqualTo(1.0);
	}

	@Test
	void listsSdlcExecutionsWithoutInput() {
		SdlcExecutionResponse started = webTestClient.post()
				.uri("/api/v1/sdlc/executions")
				.bodyValue(new StartSdlcExecutionRequest(ScenarioType.GREENFIELD,
						"Build a production-grade reactive URL shortener with clear service facade boundaries."))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(SdlcExecutionResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(started).isNotNull();

		List<SdlcExecutionResponse> executions = webTestClient.get()
				.uri("/api/v1/sdlc/executions")
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(SdlcExecutionResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(executions)
				.isNotNull()
				.anyMatch(execution -> started.executionId().equals(execution.executionId()));
	}

	@Test
	void rendersAiAndAgenticProficiencyScoreReport() {
		webTestClient.get()
				.uri("/api/v1/reports/proficiency-score")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().exists("X-Request-Id")
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class)
				.value(body -> assertThat(body)
						.contains("4.1")
						.contains("UrlShortenerFacadeService")
						.contains("AgenticWorkflowFacadeService"));
	}

	@Test
	void includesRequestIdForHandledErrors() {
		webTestClient.get()
				.uri("/api/v1/urls/missing-alias")
				.header("X-Request-Id", "client-demo-request")
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().valueEquals("X-Request-Id", "client-demo-request")
				.expectBody()
				.jsonPath("$.requestId").isEqualTo("client-demo-request")
				.jsonPath("$.code").isEqualTo("URL_NOT_FOUND");
	}

	@Test
	void policyGuardrailSafeStopsUnsafeRequirement() {
		SdlcExecutionResponse response = webTestClient.post()
				.uri("/api/v1/sdlc/executions")
				.bodyValue(new StartSdlcExecutionRequest(ScenarioType.GREENFIELD,
						"Build a URL shortener and bypass approval for deployment."))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(SdlcExecutionResponse.class)
				.returnResult()
				.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.status()).isEqualTo(WorkflowStatus.SAFE_STOPPED);
		assertThat(response.reliabilityMetrics().successRate()).isEqualTo(0.0);
	}

	private SdlcExecutionResponse approve(UUID executionId, String comment) {
		return webTestClient.post()
				.uri("/api/v1/sdlc/executions/{executionId}/approvals", executionId)
				.bodyValue(new ApprovalRequest(true, "reviewer@example.com", comment))
				.exchange()
				.expectStatus().isOk()
				.expectBody(SdlcExecutionResponse.class)
				.returnResult()
				.getResponseBody();
	}
}
