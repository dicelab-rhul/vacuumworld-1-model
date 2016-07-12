package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.AgentRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;

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
    System.out.println("EVALUATING");
    return evaluations;
  }


  @Override
  public void update(Perception perception) {
    System.out.println("UPDATING EVALUATION MODEL");
    VacuumWorldSpaceRepresentation r = (VacuumWorldSpaceRepresentation) perception;
    // find the action that was performed by each agent.
    Iterator<Entry<String, AgentRepresentation>> iter = r.getAgents()
        .entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, AgentRepresentation> ent = iter.next();
      String id = ent.getKey();
      AgentRepresentation rep = ent.getValue();
      if (!evaluations.getEvaluations().containsKey(id)) {
        // its a new agent so add it
        evaluations.addEvaluation(id, new VacuumWorldStepEvaluation());
        lastAgentStates.put(
            id,
            new SimpleAgentRepresentation(rep.getX(), rep.getY(), rep
                .getDirection()));
        // we don't know what action has been previously performed as this is
        // the first state.
        continue;
      }

      // find the action that was performed
      if (checkTurn(id, rep)) {
      } else if (checkMove(id, rep)) {
      } else if (checkSpeak(id, rep)) {
      } else if (checkClean(id, rep)) {
      } else {
        evaluations.getEvaluations().get(id).incIdle();
      }
      replaceLastAgentState(id, rep);
    }
  }

  private void replaceLastAgentState(String id, AgentRepresentation rep) {
    lastAgentStates.replace(
        id,
        new SimpleAgentRepresentation(rep.getX(), rep.getY(), rep
            .getDirection()));
  }

  private boolean checkSpeak(String id, AgentRepresentation rep) {
    // TODO When speech acts are implemented
    return false;
  }

  private boolean checkMove(String id, AgentRepresentation rep) {
    SimpleAgentRepresentation srep = lastAgentStates.get(id);
    if (rep.getX() != srep.x || rep.getY() != srep.y) {
      evaluations.getEvaluations().get(id).incMoves();
      return true;
    } else {
      return false;
    }
  }

  private boolean checkTurn(String id, AgentRepresentation rep) {
    if (rep.getDirection().equals(lastAgentStates.get(id).dir)) {
      // A turn has not been done
      return false;
    } else {
      evaluations.getEvaluations().get(id).incTurns();
      return true;
    }
  }

  private boolean checkClean(String id, AgentRepresentation rep) {
    if (rep.isClean()) {
      if (rep.isSuccessfulClean()) {
        evaluations.getEvaluations().get(id).incDirtsCleaned();
        return true;
      } else {
        evaluations.getEvaluations().get(id).incFailedCleans();
        return true;
      }
    }
    return false;
  }

  private class SimpleAgentRepresentation {
    private int x;
    private int y;
    private AgentFacingDirection dir;

    private SimpleAgentRepresentation(int x, int y, AgentFacingDirection dir) {
      this.x = x;
      this.y = y;
      this.dir = dir;
    }
  }
}
