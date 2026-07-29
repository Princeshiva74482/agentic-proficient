package com.agenticproficient.urlshortner.agentic.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkflowContextDocument {

	private String normalizedProblem;
	private Map<String, StageOutput> stageOutputs = new LinkedHashMap<>();
	private List<String> assumptions = new ArrayList<>();
	private List<String> decisions = new ArrayList<>();
	private List<String> risks = new ArrayList<>();
	private List<String> validationChecks = new ArrayList<>();

	public static WorkflowContextDocument fromRequirement(String requirement) {
		WorkflowContextDocument document = new WorkflowContextDocument();
		document.setNormalizedProblem(requirement == null ? "" : requirement.strip());
		return document;
	}

	public void addOutput(StageOutput output) {
		stageOutputs.put(output.step().name(), output);
		decisions.addAll(output.decisions());
		risks.addAll(output.risks());
		validationChecks.addAll(output.validationChecks());
	}

	public String getNormalizedProblem() {
		return normalizedProblem;
	}

	public void setNormalizedProblem(String normalizedProblem) {
		this.normalizedProblem = normalizedProblem;
	}

	public Map<String, StageOutput> getStageOutputs() {
		return stageOutputs;
	}

	public void setStageOutputs(Map<String, StageOutput> stageOutputs) {
		this.stageOutputs = stageOutputs;
	}

	public List<String> getAssumptions() {
		return assumptions;
	}

	public void setAssumptions(List<String> assumptions) {
		this.assumptions = assumptions;
	}

	public List<String> getDecisions() {
		return decisions;
	}

	public void setDecisions(List<String> decisions) {
		this.decisions = decisions;
	}

	public List<String> getRisks() {
		return risks;
	}

	public void setRisks(List<String> risks) {
		this.risks = risks;
	}

	public List<String> getValidationChecks() {
		return validationChecks;
	}

	public void setValidationChecks(List<String> validationChecks) {
		this.validationChecks = validationChecks;
	}
}
