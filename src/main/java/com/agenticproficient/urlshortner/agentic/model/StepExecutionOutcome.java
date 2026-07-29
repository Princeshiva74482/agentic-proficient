package com.agenticproficient.urlshortner.agentic.model;

public record StepExecutionOutcome(StageOutput output, int retryCount, boolean fallbackApplied) {
}
