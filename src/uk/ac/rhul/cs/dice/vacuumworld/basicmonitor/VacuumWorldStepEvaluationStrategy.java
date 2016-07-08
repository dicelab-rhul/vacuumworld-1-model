package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.environment.AgentRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;
import util.Utils;

public class VacuumWorldStepEvaluationStrategy implements
    EvaluationStrategy<String> {

  private Map<String, ArrayList<ActionRepresentation>> actionModel;
  private Map<String, SimpleAgentRepresentation> lastAgentStates;

  private Logger logger;

  public VacuumWorldStepEvaluationStrategy() {
    this.actionModel = new HashMap<>();
    this.lastAgentStates = new HashMap<>();
    logger = Utils
        .fileLogger("C:/Users/Ben/workspace/vacuumworldmodel/logs/eval/evaluation.log");
  }

  @Override
  public Evaluation evaluate(String actor, int startCycle, int endCycle) {
    System.out.println("EVALUATING");
    // logData();
    if (actor == null) {
      return evaluateAll(startCycle, endCycle);
    } else {
      return null; //TODO evaluation of specific agent
    }
  }

  public Evaluation evaluateAll(int start, int end) {
    VacuumWorldStepCollectiveEvaluation eval = new VacuumWorldStepCollectiveEvaluation();
    Iterator<Entry<String, ArrayList<ActionRepresentation>>> iter = actionModel
        .entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, ArrayList<ActionRepresentation>> ent = iter.next();
      VacuumWorldStepEvaluation e = new VacuumWorldStepEvaluation();
      e.setId(ent.getKey());
      e.setStartCycle(start);
      e.setEndCycle(end);
      for (ActionRepresentation a : ent.getValue()) {
        switch (a) {
        case TURN:
          e.setTurns(e.getTurns() + 1);
          e.setTotalSteps(e.getTotalSteps() + 1);
        case MOVE:
          e.setMoves(e.getMoves() + 1);
          e.setTotalSteps(e.getTotalSteps() + 1);
        case SPEAK:
          e.setSpeech(e.getSpeech() + 1);
          e.setTotalSteps(e.getTotalSteps() + 1);
        case CLEAN:
          e.setIdle(e.getIdle() + 1);
        case SUCCESSFULCLEAN:
          e.setDirtsCleaned(e.getDirtsCleaned() + 1);
          e.setTotalSteps(e.getTotalSteps() + 1);
        case NONE:
          e.setIdle(e.getIdle() + 1);
        default:
        }
      }
      eval.addEvaluation(e.getId(), e);
    }
    return eval;
  }

  public void logData() {
    Iterator<Entry<String, ArrayList<ActionRepresentation>>> iter = actionModel
        .entrySet().iterator();
    StringBuilder builder = new StringBuilder();
    while (iter.hasNext()) {
      Entry<String, ArrayList<ActionRepresentation>> ent = iter.next();
      builder.append(ent.getKey() + ":\n");
      for (ActionRepresentation a : ent.getValue()) {
        builder.append(a + "\n");
      }
    }
    logger.info(builder.toString());
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
      if (!actionModel.containsKey(id)) {
        // its a new agent so add it
        actionModel.put(id, new ArrayList<ActionRepresentation>());
        lastAgentStates.put(
            id,
            new SimpleAgentRepresentation(rep.getX(), rep.getY(), rep
                .getDirection()));
        // we don't know what action has been previously performed as this is
        // the first state.
        continue;
      }

      // find the action that was performed
      if (verifyAndUpdate(id, checkTurn(id, rep), rep))
        continue;
      if (verifyAndUpdate(id, checkMove(id, rep), rep))
        continue;
      if (verifyAndUpdate(id, checkSpeak(id, rep), rep))
        continue;
      if (verifyAndUpdate(id, checkClean(id, rep), rep))
        continue;
      // no action was performed.
      actionModel.get(id).add(ActionRepresentation.NONE);
      replaceLastAgentState(id, rep);
    }
  }

  private void replaceLastAgentState(String id, AgentRepresentation rep) {
    lastAgentStates.replace(
        id,
        new SimpleAgentRepresentation(rep.getX(), rep.getY(), rep
            .getDirection()));
  }

  private ActionRepresentation checkSpeak(String id, AgentRepresentation rep) {
    // TODO When speech acts are implemented
    return null;
  }

  private ActionRepresentation checkMove(String id, AgentRepresentation rep) {
    SimpleAgentRepresentation srep = lastAgentStates.get(id);
    if (rep.getX() != srep.x || rep.getY() != srep.y) {
      return ActionRepresentation.MOVE;
    } else {
      return null;
    }
  }

  private ActionRepresentation checkTurn(String id, AgentRepresentation rep) {
    if (rep.getDirection().equals(lastAgentStates.get(id).dir)) {
      // A turn has not been done
      return null;
    } else {
      return ActionRepresentation.TURN;
    }
  }

  private ActionRepresentation checkClean(String id, AgentRepresentation rep) {
    if (rep.isClean()) {
      if (rep.isSuccessfulClean()) {
        return ActionRepresentation.SUCCESSFULCLEAN;
      } else {
        return ActionRepresentation.CLEAN;
      }
    }
    return null;
  }

  private boolean verifyAndUpdate(String id, ActionRepresentation action,
      AgentRepresentation rep) {
    if (action == null) {
      return false;
    } else {
      actionModel.get(id).add(action);
      replaceLastAgentState(id, rep);
      return true;
    }
  }

  private enum ActionRepresentation {
    CLEAN, SUCCESSFULCLEAN, MOVE, TURN, SPEAK, NONE;
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
