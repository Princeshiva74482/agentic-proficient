# Engineering Summary

## Plan And Rationale

- Implement a modular WebFlux service with reactive persistence and explicit facade boundaries.
- Use Flyway migrations to make the H2 prototype repeatable.
- Use MapStruct to keep API mapping deterministic and compile-time checked.
- Model the agentic SDLC as a dependency graph with persisted state instead of a simple in-memory sequence.
- Make high-impact autonomy explicit through approval gates, policy guardrails, audit events, and reliability metrics.

## Artifacts

- URL shortener code: `src/main/java/com/agenticproficient/urlshortner/shortener`.
- Agentic orchestration code: `src/main/java/com/agenticproficient/urlshortner/agentic`.
- Database migrations: `src/main/resources/db/migration`.
- Integration tests: `src/test/java/com/agenticproficient/urlshortner/UrlShortenerIntegrationTests.java`.
- API contract: `docs/openapi.yml`.

## Assumptions

- H2 is acceptable for the assignment prototype and local integration tests.
- A modular monolith is sufficient; microservices would add deployment overhead without improving the prototype outcome.
- The local agentic executor generates deterministic stage outputs rather than calling an external LLM.
- Human approval identity is represented by request payload fields, not external SSO.

## Risks And Trade-Offs

- H2 does not provide production-grade durability or operational behavior.
- Public redirect endpoints must preserve availability, so click tracking failures are logged and do not block redirects.
- Alias enumeration can be mitigated further with rate limiting and abuse detection outside this prototype.
- Approval enforcement is application-level; production deployment should integrate with IAM, RBAC, and signed audit trails.
- Generated fallback artifacts are conservative and require manual review before release.

## Validation

- `UrlShortenerIntegrationTests` validates core create, redirect, analytics, validation, policy, and approval behavior.
- Flyway validates schema creation at application startup.
- MapStruct uses `unmappedTargetPolicy=ERROR` to prevent accidental DTO mapping drift.
- Actuator exposes health and metrics endpoints for operational checks.

## Limitations

- No external cache is included; high-scale redirect traffic would benefit from Redis or CDN edge caching.
- No rate limiter is included; production should add per-IP and per-account quotas.
- No authentication is included; production should protect management and orchestration APIs.
- The execution environment used for this implementation did not have a Java runtime available, so automated Gradle validation must be run after installing/configuring JDK 21.
