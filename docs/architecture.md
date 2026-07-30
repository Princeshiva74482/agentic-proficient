# Architecture Overview

## Component Model

- `shortener.controller`: WebFlux REST endpoints for URL management and redirects.
- `shortener.facade`: Facade boundary used by controllers to keep API orchestration separate from domain logic.
- `shortener.service`: URL validation, code generation, click tracking, and analytics coordination.
- `shortener.repository`: Reactive R2DBC repositories for `short_urls` and `click_events`.
- `shortener.mapper`: MapStruct mapper from persistence entities to API DTOs.
- `agentic.controller`: REST endpoints for SDLC execution, approvals, and audit retrieval.
- `agentic.facade`: Facade boundary for orchestration use cases.
- `agentic.service`: Dependency-graph orchestration, guardrails, retries, fallback, rollback, and safe-stop logic.
- `agentic.repository`: Reactive persistence for workflow state and audit-grade event history.

## URL Shortener Flow

1. `POST /api/v1/urls` validates URL policy, custom alias rules, expiration, and reserved aliases.
2. The service generates a Base62 code when no custom alias is supplied.
3. `ShortUrlRepository` persists the URL through R2DBC.
4. MapStruct creates the public `UrlResponse`.
5. `GET /{shortCode}` resolves the target, records a click event, increments access count, and returns `302 Found`.
6. Analytics aggregate click events by URL and expose recent activity.

## Agentic Orchestration Model

The SDLC workflow is intentionally graph-based rather than a linear chain.

- `WorkflowGraph` creates a scenario-aware dependency graph.
- Each `StepDefinition` has dependencies, an entry gate, an exit gate, and an approval flag.
- Ready independent steps execute concurrently up to `agentic.workflow.parallelism`.
- Synchronization happens when downstream steps depend on multiple completed branches.
- `WorkflowContextDocument` preserves normalized requirements, stage outputs, assumptions, decisions, risks, and validation checks.
- `AgenticAuditEvent` records every important transition for traceability.

## Governance Controls

- Human approval is required before high-impact steps such as implementation planning and release readiness.
- Ambiguous scenarios require explicit clarification approval.
- Unsafe instructions such as bypassing approvals or ignoring security are denied by policy guardrails.
- Bounded retries protect against transient step failures.
- Fallback output is generated after retry exhaustion so reviewers receive a conservative recovery artifact.
- Rollback marks unrecoverable workflow failures as `ROLLED_BACK`.
- Safe-stop marks policy-denied or rejected workflows as `SAFE_STOPPED`.

## Reliability Metrics

Each execution tracks:

- Success rate.
- Retry frequency.
- Rollback frequency.
- MTTR in milliseconds.
- End-to-end latency in milliseconds.

Micrometer metrics are also emitted through the Spring Boot actuator metrics endpoint.

## Key Decisions

- A modular monolith is used instead of separate microservices because the assignment requires a working prototype and the bounded domain is cohesive.
- WebFlux and R2DBC are used end-to-end to avoid blocking request processing.
- Flyway manages schema evolution even for H2 so the prototype follows production migration discipline.
- MapStruct is used for deterministic DTO mapping and compile-time mapping validation.
- Controllers only depend on facades to preserve clean boundaries and improve testability.
