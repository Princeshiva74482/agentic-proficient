# Scenario Walkthroughs

## Greenfield Scenario

Requirement:

> Build a production-grade reactive URL shortener with analytics.

Decomposition:

1. Normalize requirements into URL creation, redirect, analytics, reliability, and documentation tasks.
2. Design WebFlux/R2DBC components and Flyway schema.
3. Require human approval before implementation planning.
4. Execute test planning and documentation in parallel after implementation approval.
5. Require release-readiness approval before completion.

Expected workflow:

- `REQUIREMENT_ANALYSIS`
- `ARCHITECTURE_DESIGN`
- Approval gate: `IMPLEMENTATION_PLAN`
- Parallel branch: `TEST_PLAN` and `DOCUMENTATION`
- Approval gate: `RELEASE_READINESS`
- `COMPLETED`

Validation:

- Create short URL.
- Resolve redirect.
- Verify analytics increments.
- Verify audit events and reliability metrics.

## Brownfield Scenario

Requirement:

> Enhance an existing URL shortener to add analytics and stronger URL validation.

Decomposition:

1. Normalize the enhancement requirement.
2. Run impact analysis across controllers, services, repositories, migrations, tests, and docs.
3. Identify regression risks before architecture design.
4. Require implementation approval before changing behavior.
5. Synchronize testing and documentation before release review.

Expected workflow:

- `REQUIREMENT_ANALYSIS`
- `IMPACT_ANALYSIS`
- `ARCHITECTURE_DESIGN`
- Approval gate: `IMPLEMENTATION_PLAN`
- Parallel branch: `TEST_PLAN` and `DOCUMENTATION`
- Approval gate: `RELEASE_READINESS`
- `COMPLETED`

Validation:

- Backward-compatible redirect behavior.
- Duplicate custom alias conflict handling.
- Private host rejection.
- Migration reproducibility.
- Regression tests around analytics updates.

## Ambiguous Scenario

Requirement:

> Make URL sharing better for enterprise users.

Decomposition:

1. Detect ambiguity in target users, scale, security, analytics, and approval expectations.
2. Stop for clarification review before downstream design assumptions become implementation decisions.
3. Continue only when a reviewer approves the captured assumptions.
4. Carry accepted assumptions through architecture, implementation planning, tests, documentation, and release readiness.

Expected workflow:

- `REQUIREMENT_ANALYSIS`
- Approval gate: `CLARIFICATION_REVIEW`
- `ARCHITECTURE_DESIGN`
- Approval gate: `IMPLEMENTATION_PLAN`
- Parallel branch: `TEST_PLAN` and `DOCUMENTATION`
- Approval gate: `RELEASE_READINESS`
- `COMPLETED`

Validation:

- Rejected clarification approval results in `SAFE_STOPPED`.
- Approved clarification creates audit history and continues execution.
- Policy guardrails still apply after clarification.

## Dynamic Re-Planning

The graph is recalculated after each execution wave from the current `WorkflowContextDocument`. This allows upstream context changes, such as brownfield signals or ambiguity decisions, to influence downstream steps while preserving audit history and approval gates.
