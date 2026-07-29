package com.agenticproficient.urlshortner.agentic.entity;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.agenticproficient.urlshortner.agentic.model.WorkflowEventType;
import com.agenticproficient.urlshortner.agentic.model.WorkflowStep;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("agentic_audit_events")
public class AgenticAuditEvent implements Persistable<UUID> {

	@Id
	@Column("id")
	private UUID id;

	@Column("execution_id")
	private UUID executionId;

	@Column("event_type")
	private String eventType;

	@Column("step_id")
	private String stepId;

	@Column("message")
	private String message;

	@Column("metadata_json")
	private String metadataJson;

	@Column("created_at")
	private Instant createdAt;

	@Transient
	private boolean newEntity;

	public static AgenticAuditEvent create(AgenticExecution execution, WorkflowEventType eventType, WorkflowStep step,
			String message, String metadataJson, Clock clock) {
		AgenticAuditEvent event = new AgenticAuditEvent();
		event.id = UUID.randomUUID();
		event.executionId = execution.getId();
		event.eventType = eventType.name();
		event.stepId = step == null ? null : step.name();
		event.message = message;
		event.metadataJson = metadataJson;
		event.createdAt = clock.instant();
		event.newEntity = true;
		return event;
	}

	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return newEntity;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getExecutionId() {
		return executionId;
	}

	public void setExecutionId(UUID executionId) {
		this.executionId = executionId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getStepId() {
		return stepId;
	}

	public void setStepId(String stepId) {
		this.stepId = stepId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMetadataJson() {
		return metadataJson;
	}

	public void setMetadataJson(String metadataJson) {
		this.metadataJson = metadataJson;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
