CREATE TABLE "agentic_executions" (
    "id" UUID PRIMARY KEY,
    "correlation_id" VARCHAR(64) NOT NULL,
    "scenario_type" VARCHAR(32) NOT NULL,
    "requirement_text" CLOB NOT NULL,
    "status" VARCHAR(32) NOT NULL,
    "completed_steps" VARCHAR(512),
    "approved_steps" VARCHAR(512),
    "pending_approval_step" VARCHAR(64),
    "context_json" CLOB NOT NULL,
    "success_count" INT NOT NULL DEFAULT 0,
    "failure_count" INT NOT NULL DEFAULT 0,
    "retry_count" INT NOT NULL DEFAULT 0,
    "rollback_count" INT NOT NULL DEFAULT 0,
    "mttr_ms" BIGINT NOT NULL DEFAULT 0,
    "latency_ms" BIGINT NOT NULL DEFAULT 0,
    "started_at" TIMESTAMP NOT NULL,
    "completed_at" TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX "uk_agentic_executions_correlation_id" ON "agentic_executions"("correlation_id");
CREATE INDEX "idx_agentic_executions_status" ON "agentic_executions"("status");

CREATE TABLE "agentic_audit_events" (
    "id" UUID PRIMARY KEY,
    "execution_id" UUID NOT NULL,
    "event_type" VARCHAR(64) NOT NULL,
    "step_id" VARCHAR(64),
    "message" VARCHAR(1024) NOT NULL,
    "metadata_json" CLOB,
    "created_at" TIMESTAMP NOT NULL,
    CONSTRAINT "fk_agentic_audit_events_execution"
        FOREIGN KEY ("execution_id") REFERENCES "agentic_executions"("id")
);

CREATE INDEX "idx_agentic_audit_execution_created_at"
    ON "agentic_audit_events"("execution_id", "created_at");
