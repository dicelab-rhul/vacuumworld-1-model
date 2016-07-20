package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.AgentAwarenessRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

public class VacuumWorldSmartRandomSocialMind extends VacuumWorldDefaultMind {

  private Set<String> agentsIKnow = new HashSet<String>();
  private AgentAwarenessRepresentation me;

  public VacuumWorldSmartRandomSocialMind(AgentAwarenessRepresentation me) {
    this.me = me;
  }

  @Override
  public void perceive(Object perceptionWrapper) {
    while (this.getLastCyclePerceptions().isEmpty()) {
      notifyObservers(null, VacuumWorldDefaultBrain.class);
    }
  }

  @Override
  public EnvironmentalAction decide(Object... parameters) {
    this.setAvailableActions(new ArrayList<>());
    this.getAvailableActions().addAll(this.getActions());
    if (this.getLastCyclePerceptions().isEmpty()) {
      this.setNextAction(new PerceiveAction(this.getPerceptionRange(), this
          .isCanSeeBehind()));
    } else {
      this.setNextAction(decideFromPerceptions());
    }
    return this.getNextAction();
  }

  @Override
  public void execute(EnvironmentalAction action) {
    this.getLastCyclePerceptions().clear();
    notifyObservers(this.getNextAction(), VacuumWorldDefaultBrain.class);
  }

  private EnvironmentalAction decideFromPerceptions() {
    VacuumWorldActionResult result = null;
    for (DefaultActionResult r : this.getLastCyclePerceptions()) {
      if (r instanceof VacuumWorldSpeechActionResult) {
        VacuumWorldSpeechActionResult s = (VacuumWorldSpeechActionResult) r;
        System.out.println(me.getMyid() + " Recieved: "
            + s.getClass().getSimpleName() + " : " + s.getSender() + ", "
            + s.getPayload().getPayload());
      } else if (r instanceof VacuumWorldActionResult) {
        System.out.println(r.getClass().getSimpleName() + ":"
            + r.getActionResult());
        result = (VacuumWorldActionResult) r;
      }
    }
    if (result != null) {
      return decideFromSpacePerception(((VacuumWorldActionResult) result)
          .getPerception());
    } else {
      Logger.getGlobal().log(Level.SEVERE,
          "NO RESULT WAS GIVEN TO: " + this.me.getMyid());
      return new PerceiveAction(this.getPerceptionRange(),
          this.isCanSeeBehind());
    }
  }

  private EnvironmentalAction decideFromSpacePerception(
      VacuumWorldPerception perception) {
    System.out.println("DECIDING FROM PERCEPTION");
    // clean if we are on some dirt!
    EnvironmentalAction a = cleanAction(perception);
    if (a != null) {
      return a;
    }
    ArrayList<String> newAgents = updateAgentsMet(perception);
    // if there are new agents we want to greet them!
    if (!newAgents.isEmpty()) {
      agentsIKnow.addAll(newAgents);
      return this.buildSpeechAction(this.me.getMyid(), newAgents,
          new VacuumWorldSpeechPayload("Hello!"));
    } else {
      return randomAction();
    }
  }

  private EnvironmentalAction cleanAction(VacuumWorldPerception perception) {
    Iterator<Entry<VacuumWorldCoordinates, VacuumWorldLocation>> iter = perception
        .getPerceivedMap().entrySet().iterator();
    while (iter.hasNext()) {
      Entry<VacuumWorldCoordinates, VacuumWorldLocation> ent = iter.next();

      if (ent.getValue().isAnAgentPresent()) {
        String id = (String) ent.getValue().getAgent().getId();
        if (me.getMyid().equals(id)) {
          // check if im on to of some dirt
          if (ent.getValue().isDirtPresent()) {
            if (DirtType.agentAndDirtCompatible(((DirtAppearance) ent
                .getValue().getDirt().getExternalAppearance()).getDirtType(),
                me.getType())) {
              // clean the dirt!
              return new CleanAction();
            }
          }

        }
      }
    }
    return null;
  }

  private EnvironmentalAction randomAction() {
    Random rand = new Random();
    int r = rand.nextInt(5);
    if (r < 2) {
      return turnAction();
    } else if (r < 4) {
      return moveAction();
    } else {
      return broadcastAction();
    }

  }

  private EnvironmentalAction broadcastAction() {
    return new SpeechAction(this.me.getMyid(), null,
        new VacuumWorldSpeechPayload("Hello everyone!"));
  }

  private EnvironmentalAction moveAction() {
    return new MoveAction();
  }

  private EnvironmentalAction turnAction() {
    Random rand = new Random();
    if(rand.nextBoolean()) {
      return new TurnRightAction();
    } else {
      return new TurnLeftAction();
    }
  }

  private ArrayList<String> updateAgentsMet(VacuumWorldPerception perception) {
    ArrayList<String> newAgents = new ArrayList<>();
    perception.getPerceivedMap().forEach(
        new BiConsumer<VacuumWorldCoordinates, VacuumWorldLocation>() {
          @Override
          public void accept(VacuumWorldCoordinates t, VacuumWorldLocation u) {
            if (u.isAnAgentPresent()) {
              String id = (String) u.getAgent().getId();
              if (!me.getMyid().equals(id) && !agentsIKnow.contains(id)) {
                newAgents.add(id);
              }
            }
          }
        });
    return newAgents;
  }
}
