CREATE TABLE "short_urls" (
    "id" UUID PRIMARY KEY,
    "original_url" VARCHAR(2048) NOT NULL,
    "short_code" VARCHAR(64) NOT NULL,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP NOT NULL,
    "expires_at" TIMESTAMP,
    "active" BOOLEAN NOT NULL DEFAULT TRUE,
    "access_count" BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX "uk_short_urls_short_code" ON "short_urls"("short_code");
CREATE INDEX "idx_short_urls_active_expires" ON "short_urls"("active", "expires_at");

CREATE TABLE "click_events" (
    "id" UUID PRIMARY KEY,
    "short_url_id" UUID NOT NULL,
    "short_code" VARCHAR(64) NOT NULL,
    "user_agent" VARCHAR(512),
    "referrer" VARCHAR(1024),
    "ip_hash" VARCHAR(128),
    "clicked_at" TIMESTAMP NOT NULL,
    CONSTRAINT "fk_click_events_short_url"
        FOREIGN KEY ("short_url_id") REFERENCES "short_urls"("id")
);

CREATE INDEX "idx_click_events_short_url_clicked_at" ON "click_events"("short_url_id", "clicked_at" DESC);
CREATE INDEX "idx_click_events_short_code" ON "click_events"("short_code");
