package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldStepCollectiveEvaluation;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldStepEvaluation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.AgentDatabaseRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.CycleDatabaseRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.DirtDatabaseRepresentation;

public class VacuumWorldDatatbaseStepEvaluationStrategy implements EvaluationStrategy<String> {

	private VacuumWorldStepCollectiveEvaluation evaluations = new VacuumWorldStepCollectiveEvaluation();
	private int lastCycleRead = 0;

	@Override
	public Evaluation evaluate(String actor, int startTime, int endTime) {
		// calculate time idle from last cycle read
		for(VacuumWorldStepEvaluation evaluation : this.evaluations.getEvaluations().values()) {
			evaluation.incIdle(this.lastCycleRead - evaluation.getCost());
		}
		
		return this.evaluations;
	}

	@Override
	public void update(Perception perception) {
		DatabasePerception p = (DatabasePerception) perception;
		
		for (AgentDatabaseRepresentation a : p.getAgents()) {
			doAgent(a, p.getDirts(), p.getLastCycleRead());
		}
	}

	private void doAgent(AgentDatabaseRepresentation a, Set<DirtDatabaseRepresentation> dirts, int lastCycleRead) {
		this.lastCycleRead = lastCycleRead;
		this.evaluations.getEvaluations().putIfAbsent(a.getId(), new VacuumWorldStepEvaluation());
		// The first c in the list is the last one from the previous read!
		CycleDatabaseRepresentation compare = a.getCycleList()[0];
		VacuumWorldStepEvaluation eval = this.evaluations.getEvaluations().get(a.getId());

		for (int i = 1; i < a.getCycleList().length; i++) {
			CycleDatabaseRepresentation c = a.getCycleList()[i];
			
			if (!compare.getDir().equals(c.getDir())) {
				eval.incTurns(1);
			}
			else if (compare.getX() != c.getX() || compare.getY() != c.getY()) {
				eval.incMoves(1);
			}
			else if (c.getSpeech() != null) {
				eval.incTotalSpeechActions(1);
			}
			compare = c;
		}

		// update dirts cleaned from field in AgentDatabaseRepresentation
		int currentCleaned = eval.getDirtsCleaned();
		int updatedCleaned = a.getSuccessfulCleans();
		eval.incDirtsCleaned(updatedCleaned - currentCleaned);
	}
}