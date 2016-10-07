package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;

public class VacuumWorldStepCollectiveEvaluation implements Evaluation {
	private Map<String, VacuumWorldStepEvaluation> evaluations;

	public VacuumWorldStepCollectiveEvaluation() {
		setEvaluations(new HashMap<>());
	}

	public String represent() {
		StringBuilder b = new StringBuilder();
		
		for(Entry<String, VacuumWorldStepEvaluation> entry : this.evaluations.entrySet()) {
			b.append(entry.getKey());
			b.append(entry.getValue().represent());
		}
		
		return b.toString();
	}

	public void addEvaluation(String id, VacuumWorldStepEvaluation eval) {
		this.evaluations.put(id, eval);
	}

	public Map<String, VacuumWorldStepEvaluation> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(Map<String, VacuumWorldStepEvaluation> evaluation) {
		this.evaluations = evaluation;
	}
}