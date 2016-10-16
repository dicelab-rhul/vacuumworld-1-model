package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.AgentRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldStepEvaluationStrategy implements EvaluationStrategy<String> {

	private VacuumWorldStepCollectiveEvaluation evaluations;
	private Map<String, SimpleAgentRepresentation> lastAgentStates;

	public VacuumWorldStepEvaluationStrategy() {
		this.evaluations = new VacuumWorldStepCollectiveEvaluation();
		this.lastAgentStates = new HashMap<>();
	}

	@Override
	public Evaluation evaluate(String actor, int startCycle, int endCycle) {
		Utils.logWithClass(this.getClass().getSimpleName(), "Evaluation done, returning it...");
		return this.evaluations;
	}

	@Override
	public void update(Perception perception) {
		VacuumWorldSpaceRepresentation r = (VacuumWorldSpaceRepresentation) perception;
		// find the action that was performed by each agent.
		for (Entry<String, AgentRepresentation> entry : r.getAgents().entrySet()) {
			manageAgentRepresentation(entry);
		}
	}

	private void manageAgentRepresentation(Entry<String, AgentRepresentation> entry) {
		String id = entry.getKey();
		AgentRepresentation rep = entry.getValue();

		if (!this.evaluations.getEvaluations().containsKey(id)) {
			this.evaluations.addEvaluation(id, new VacuumWorldStepEvaluation());
			this.lastAgentStates.put(id, new SimpleAgentRepresentation(rep.getX(), rep.getY(), rep.getDirection(), rep.getLastSpeechAction()));
		}
		else {
			findAction(id, rep);
		}
	}

	private void findAction(String id, AgentRepresentation rep) {
		if (checkTurn(id, rep)) {
			//TODO
		}
		else if (checkMove(id, rep)) {
			//TODO
		}
		else if (checkSpeak(id, rep)) {
			//TODO
		}
		else if (checkClean(id, rep)) {
			//TODO
		}
		else {
			this.evaluations.getEvaluations().get(id).incIdle(1);
		}
		
		replaceLastAgentState(id, rep);
	}

	private void replaceLastAgentState(String id, AgentRepresentation rep) {
		this.lastAgentStates.replace(id, new SimpleAgentRepresentation(rep.getX(), rep.getY(), rep.getDirection(), rep.getLastSpeechAction()));
	}

	private boolean checkSpeak(String id, AgentRepresentation rep) {
		if (rep.getLastSpeechAction() == null) {
			return false;
		}
		if (rep.getLastSpeechAction().equals(this.lastAgentStates.get(id).lastSpeechAction)) {
			return false;
		}
		else {
			this.evaluations.getEvaluations().get(id).incTotalSpeechActions(1);
			return true;
		}
	}

	private boolean checkMove(String id, AgentRepresentation rep) {
		SimpleAgentRepresentation srep = this.lastAgentStates.get(id);

		if (rep.getX() != srep.x || rep.getY() != srep.y) {
			this.evaluations.getEvaluations().get(id).incMoves(1);
			return true;
		}
		else {
			return false;
		}
	}

	private boolean checkTurn(String id, AgentRepresentation rep) {
		if (rep.getDirection().equals(this.lastAgentStates.get(id).dir)) {
			// A turn has not been done
			return false;
		}
		else {
			this.evaluations.getEvaluations().get(id).incTurns(1);
			return true;
		}
	}

	private boolean checkClean(String id, AgentRepresentation rep) {
		if (!rep.isClean()) {
			return false;
		}
		else if (rep.isSuccessfulClean()) {
			this.evaluations.getEvaluations().get(id).incDirtsCleaned(1);
			return true;
		}
		else {
			this.evaluations.getEvaluations().get(id).incIdle(1);
			return true;
		}
	}

	private class SimpleAgentRepresentation {
		private int x;
		private int y;
		private ActorFacingDirection dir;
		private SpeechAction lastSpeechAction;

		private SimpleAgentRepresentation(int x, int y, ActorFacingDirection dir, SpeechAction lastSpeechAction) {
			this.x = x;
			this.y = y;
			this.dir = dir;
			this.lastSpeechAction = lastSpeechAction;
		}
	}
}