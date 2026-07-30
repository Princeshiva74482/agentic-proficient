CREATE INDEX "idx_short_urls_active_created_at"
    ON "short_urls"("active", "created_at");

CREATE INDEX "idx_agentic_executions_updated_at"
    ON "agentic_executions"("updated_at");
