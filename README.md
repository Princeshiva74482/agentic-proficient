# Agentic URL Shortener

Reactive Spring Boot prototype for a production-style URL shortener with analytics and an agentic SDLC orchestration layer. The application uses WebFlux, Spring Data R2DBC, H2, Flyway, MapStruct, facade boundaries, validation, metrics, and explicit human approval gates.

## What It Delivers

- URL shortening API with generated or custom aliases.
- Redirect endpoint with click tracking and privacy-preserving IP hashing.
- Analytics API with total clicks, unique visitors, last access time, and recent clicks.
- Agentic orchestration API that converts requirements into governed SDLC execution state.
- Explicit dependency graph, entry/exit gates, approvals, audit events, retries, fallback, rollback, safe-stop, and reliability metrics.
- Flyway-managed H2 schema for repeatable local startup.

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

The tests run against an in-memory H2 database and cover URL creation, redirect analytics, private-host validation, policy guardrails, and orchestration approval gates.

## Core API Examples

Create a short URL:

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com/docs","customAlias":"docs2026"}'
```

Redirect:

```bash
curl -i http://localhost:8080/docs2026
```

Analytics:

```bash
curl http://localhost:8080/api/v1/urls/docs2026/analytics
```

Start an agentic SDLC execution:

```bash
curl -X POST http://localhost:8080/api/v1/sdlc/executions \
  -H "Content-Type: application/json" \
  -d '{"scenarioType":"GREENFIELD","requirement":"Build a reactive URL shortener with analytics."}'
```

Approve a pending gate:

```bash
curl -X POST http://localhost:8080/api/v1/sdlc/executions/{executionId}/approvals \
  -H "Content-Type: application/json" \
  -d '{"approved":true,"approver":"reviewer@example.com","comment":"Reviewed and approved."}'
```

## Documentation

- `docs/architecture.md` explains components, orchestration model, control flow, and decisions.
- `docs/scenarios.md` covers greenfield, brownfield, and ambiguous requirement scenarios.
- `docs/openapi.yml` provides a reviewable API/schema contract.
- `docs/engineering-summary.md` captures assumptions, risks, validation, limitations, and release readiness.

## Configuration

Key settings live in `src/main/resources/application.yml`:

- `url-shortener.base-url`: public base URL used in API responses.
- `url-shortener.code-length`: generated alias length.
- `url-shortener.security.allow-private-hosts`: SSRF protection switch.
- `url-shortener.privacy.ip-hash-salt`: salt for click IP hashing.
- `agentic.workflow.max-retries`: bounded retry count per workflow step.
- `agentic.workflow.step-timeout`: timeout per agentic step.
- `agentic.workflow.parallelism`: maximum concurrent ready steps.

## Important Limits

- H2 is used for prototype repeatability; use PostgreSQL or another durable database for production.
- The agentic step executor is deterministic and local; it models governed SDLC automation without calling external LLM APIs.
- Approval gates are API-enforced but not identity-provider-backed in this prototype.
