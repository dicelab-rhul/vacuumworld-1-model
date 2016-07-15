package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.AgentRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldStepEvaluationStrategy implements
    EvaluationStrategy<String> {

  private VacuumWorldStepCollectiveEvaluation evaluations;
  private Map<String, SimpleAgentRepresentation> lastAgentStates;

  public VacuumWorldStepEvaluationStrategy() {
    this.evaluations = new VacuumWorldStepCollectiveEvaluation();
    this.lastAgentStates = new HashMap<>();
  }

  @Override
  public Evaluation evaluate(String actor, int startCycle, int endCycle) {
    Utils.log("EVALUATING");
    return evaluations;
  }

  @Override
  public void update(Perception perception) {
    //Utils.log("UPDATING EVALUATION MODEL");
    VacuumWorldSpaceRepresentation r = (VacuumWorldSpaceRepresentation) perception;
    // find the action that was performed by each agent.
    for (Entry<String, AgentRepresentation> entry : r.getAgents().entrySet()) {
      manageAgentRepresentation(entry);
    }
  }

  private void manageAgentRepresentation(
      Entry<String, AgentRepresentation> entry) {
    String id = entry.getKey();
    AgentRepresentation rep = entry.getValue();

    if (!this.evaluations.getEvaluations().containsKey(id)) {
      this.evaluations.addEvaluation(id, new VacuumWorldStepEvaluation());
      this.lastAgentStates.put(id, new SimpleAgentRepresentation(rep.getX(),
          rep.getY(), rep.getDirection(), rep.getLastSpeechAction()));
      return;
    }

    findAction(id, rep);
  }

  private void findAction(String id, AgentRepresentation rep) {
    if (checkTurn(id, rep)) {
    } else if (checkMove(id, rep)) {
    } else if (checkSpeak(id, rep)) {
    } else if (checkClean(id, rep)) {
    } else {
      this.evaluations.getEvaluations().get(id).incIdle();
    }
    replaceLastAgentState(id, rep);
  }

  private void replaceLastAgentState(String id, AgentRepresentation rep) {
    this.lastAgentStates.replace(id, new SimpleAgentRepresentation(rep.getX(),
        rep.getY(), rep.getDirection(), rep.getLastSpeechAction()));
  }

  private boolean checkSpeak(String id, AgentRepresentation rep) {
    if(rep.getLastSpeechAction() == null) {
      return false;
    }
    if (rep.getLastSpeechAction().equals(this.lastAgentStates.get(id).lastSpeechAction)) {
      return false;
    } else {
      this.evaluations.getEvaluations().get(id).incTotalSpeechActions();
      return true;
    }
  }

  private boolean checkMove(String id, AgentRepresentation rep) {
    SimpleAgentRepresentation srep = this.lastAgentStates.get(id);

    if (rep.getX() != srep.x || rep.getY() != srep.y) {
      this.evaluations.getEvaluations().get(id).incMoves();
      return true;
    } else {
      return false;
    }
  }

  private boolean checkTurn(String id, AgentRepresentation rep) {
    if (rep.getDirection().equals(this.lastAgentStates.get(id).dir)) {
      // A turn has not been done
      return false;
    } else {
      this.evaluations.getEvaluations().get(id).incTurns();
      return true;
    }
  }

  private boolean checkClean(String id, AgentRepresentation rep) {
    if (!rep.isClean()) {
      return false;
    } else if (rep.isSuccessfulClean()) {
      this.evaluations.getEvaluations().get(id).incDirtsCleaned();
      return true;
    } else {
      this.evaluations.getEvaluations().get(id).incFailedCleans();
      return true;
    }
  }

  private class SimpleAgentRepresentation {
    private int x;
    private int y;
    private AgentFacingDirection dir;
    private SpeechAction lastSpeechAction;

    private SimpleAgentRepresentation(int x, int y, AgentFacingDirection dir,
        SpeechAction lastSpeechAction) {
      this.x = x;
      this.y = y;
      this.dir = dir;
      this.lastSpeechAction = lastSpeechAction;
    }
  }
}