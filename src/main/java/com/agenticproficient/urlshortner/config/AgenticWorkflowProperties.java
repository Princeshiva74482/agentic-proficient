package com.agenticproficient.urlshortner.config;

import java.time.Duration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "agentic.workflow")
public class AgenticWorkflowProperties {

	@Min(0)
	@Max(5)
	private int maxRetries = 2;

	@NotNull
	private Duration stepTimeout = Duration.ofSeconds(3);

	@Min(1)
	@Max(16)
	private int parallelism = 4;

	@Min(1)
	@Max(500)
	private int maxListSize = 50;

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public Duration getStepTimeout() {
		return stepTimeout;
	}

	public void setStepTimeout(Duration stepTimeout) {
		this.stepTimeout = stepTimeout;
	}

	public int getParallelism() {
		return parallelism;
	}

	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}

	public int getMaxListSize() {
		return maxListSize;
	}

	public void setMaxListSize(int maxListSize) {
		this.maxListSize = maxListSize;
	}
}
