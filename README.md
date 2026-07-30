# Agentic URL Shortener

Reactive Spring Boot assessment application for a production-style URL shortener with analytics and an agentic SDLC orchestration layer. It uses WebFlux, Spring Data R2DBC, H2, Flyway, MapStruct, validation, metrics, request tracing, OpenAPI, and facade services.

## What It Delivers

- URL shortening with generated codes or custom aliases.
- Public redirect endpoint with click tracking and privacy-preserving IP hashing.
- Analytics for total clicks, approximate unique visitors, last access time, and recent clicks.
- List endpoint for available short URLs.
- Agentic SDLC orchestration with graph execution, approval gates, audit events, retries, fallback, rollback, safe-stop, and reliability metrics.
- HTML proficiency score report for client review.
- Request tracing through `X-Request-Id` headers and error payloads.
- Flyway-managed H2 schema for repeatable local startup.

## Architecture Summary

The application uses a simple facade-service approach:

- `UrlShortenerController` depends on `UrlShortenerFacadeService`.
- `RedirectController` depends on `UrlShortenerFacadeService`.
- `SdlcOrchestrationController` depends on `AgenticWorkflowFacadeService`.
- Facade services hide validation, code generation, repositories, mappers, metrics, click tracking, workflow graph execution, guardrails, retries, fallback, audit persistence, and response mapping.

This keeps the code easy to explain to a client: controllers expose APIs, facade services coordinate use cases, repositories handle persistence, and mappers convert entities to API responses.

## Prerequisites

- Java 21 JDK.
- Gradle wrapper included in the repository.

## Run

```powershell
.\gradlew.bat bootRun
```

The service starts on `http://localhost:8080` by default.

## Test

```powershell
.\gradlew.bat test
```

The test suite covers URL creation, redirects, analytics, list endpoints, private-host validation, SDLC orchestration approval gates, policy safe-stop behavior, request IDs, and the HTML score report.

## Client Review URLs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Proficiency report: `http://localhost:8080/api/v1/reports/proficiency-score`

## URL Shortener APIs

Create a short URL:

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com/docs","customAlias":"docs2026"}'
```

List available short URLs:

```bash
curl http://localhost:8080/api/v1/urls
```

Get short URL metadata:

```bash
curl http://localhost:8080/api/v1/urls/docs2026
```

Redirect:

```bash
curl -i http://localhost:8080/docs2026
```

Analytics:

```bash
curl http://localhost:8080/api/v1/urls/docs2026/analytics
```

## Agentic SDLC APIs

Start an execution:

```bash
curl -X POST http://localhost:8080/api/v1/sdlc/executions \
  -H "Content-Type: application/json" \
  -d '{"scenarioType":"GREENFIELD","requirement":"Build a reactive URL shortener with analytics."}'
```

List recent executions:

```bash
curl http://localhost:8080/api/v1/sdlc/executions
```

Get execution state:

```bash
curl http://localhost:8080/api/v1/sdlc/executions/{executionId}
```

Approve or reject a pending gate:

```bash
curl -X POST http://localhost:8080/api/v1/sdlc/executions/{executionId}/approvals \
  -H "Content-Type: application/json" \
  -d '{"approved":true,"approver":"reviewer@example.com","comment":"Reviewed and approved."}'
```

Get audit events:

```bash
curl http://localhost:8080/api/v1/sdlc/executions/{executionId}/audit
```

Use an `executionId` returned by `POST /api/v1/sdlc/executions` or `GET /api/v1/sdlc/executions`. Swagger example UUIDs are placeholders and return `404` unless they exist in the current H2 database.

## Proficiency Report

The HTML report is stored as a real resource file:

- File: `src/main/resources/reports/proficiency-score.html`
- Endpoint: `GET /api/v1/reports/proficiency-score`
- Current estimated score: `4.1 / 5`

The score reflects API design, facade-service clarity, agentic workflow design, auditability, reliability controls, validation, testing, and cost-awareness strategy.

## Configuration

Key settings live in `src/main/resources/application.yml`:

- `URL_SHORTENER_BASE_URL`: public base URL used in API responses.
- `URL_SHORTENER_IP_HASH_SALT`: salt for privacy-preserving IP hashing.
- `url-shortener.code-length`: generated alias length.
- `url-shortener.max-list-size`: maximum short URLs returned by the list endpoint.
- `url-shortener.security.allow-private-hosts`: SSRF protection switch.
- `agentic.workflow.max-retries`: bounded retry count per workflow step.
- `agentic.workflow.step-timeout`: timeout per agentic step.
- `agentic.workflow.parallelism`: maximum concurrent ready steps.
- `agentic.workflow.max-list-size`: maximum SDLC executions returned by the list endpoint.

## Request Tracing

Every response includes `X-Request-Id`.

- If the client sends `X-Request-Id`, the same value is returned.
- If the client does not send it, the application generates one.
- Error responses include `requestId` in the response body.

Example:

```bash
curl -i http://localhost:8080/api/v1/urls/missing-alias \
  -H "X-Request-Id: client-demo-request"
```

## Important Limits

- H2 is used intentionally for assessment repeatability; data resets when the in-memory database restarts.
- The agentic executor is deterministic and local; it models governed SDLC automation without external LLM calls.
- Approval gates are API-enforced but not integrated with an identity provider.
- Authentication, rate limiting, deployment profiles, and real LLM model routing are natural next steps for a production deployment.
