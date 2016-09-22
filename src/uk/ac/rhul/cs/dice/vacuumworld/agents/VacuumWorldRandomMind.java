package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;

public class VacuumWorldRandomMind extends VacuumWorldDefaultMind {
  private Random rng;
  
  
  public VacuumWorldRandomMind() {
    super();
    this.rng = new Random();
  }


  @Override
  public void perceive(Object perceptionWrapper) {
    while(this.getLastCyclePerceptions().isEmpty()) {
      notifyObservers(null, VacuumWorldDefaultBrain.class);
    }
  }

  @Override
  public EnvironmentalAction decide(Object... parameters) {
    this.setAvailableActions(new ArrayList<>());
    this.getAvailableActions().addAll(this.getActions());
    
    if (this.getLastCyclePerceptions().isEmpty()) {
      this.setNextAction(new PerceiveAction(this.getPerceptionRange(), this.isCanSeeBehind()));
    }
    else {
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
    //this completely ignores the speeches
    
    for(DefaultActionResult result : this.getLastCyclePerceptions()) {
      if(result instanceof VacuumWorldActionResult) {
        VacuumWorldPerception p = ((VacuumWorldActionResult) result).getPerception();
        
        if(p != null) {
          return decideAction(p);
        }
      }
    }
    
    return decideAction();
  }

  private EnvironmentalAction decideAction(VacuumWorldPerception perception) {
    if (perception != null) {
      updateAvailableActions(perception);
    }
    return decideAction();
  }

  private EnvironmentalAction decideAction() {
    int size = this.getAvailableActions().size();
    int randomNumber = this.rng.nextInt(size);
    Class<? extends EnvironmentalAction> actionPrototype = this.getAvailableActions().get(randomNumber);

    return buildNewAction(actionPrototype);
  }

  private EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
    if (actionPrototype.equals(SpeechAction.class)) {
      return buildSpeechAction(null, getRecipientIds(), getPayload());
    } else {
      return buildPhysicalAction(actionPrototype);
    }
  }

  private VacuumWorldSpeechPayload getPayload() {
    return new VacuumWorldSpeechPayload("Hello World");
  }

  private ArrayList<String> getRecipientIds() {
    return new ArrayList<String>(0);
  }

  private void updateAvailableActions(VacuumWorldPerception perception) {
    updateMoveActionIfNecessary(perception);
    updateCleaningActionIfNecessary(perception);
  }

  private void updateCleaningActionIfNecessary(VacuumWorldPerception perception) {
    VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
    VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);

    if (agentLocation.isDirtPresent()) {
      addCleanIfNecessary();
    } else {
      removeCleanIfNecessary();
    }
  }

  private void removeCleanIfNecessary() {
    List<Class<CleanAction>> toRemove = new ArrayList<>();

    for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
      if (a.getClass().isAssignableFrom(CleanAction.class)) {
        toRemove.add(CleanAction.class);
      }
    }
    this.getAvailableActions().removeAll(toRemove);
  }

  private void addCleanIfNecessary() {
    for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
      if (a.getClass().isAssignableFrom(CleanAction.class)) {
        return;
      }
    }
    this.getAvailableActions().add(CleanAction.class);
  }

  private void addMoveIfNecessary() {
    for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
      if (a.getClass().isAssignableFrom(MoveAction.class)) {
        return;
      }
    }
    this.getAvailableActions().add(MoveAction.class);
  }

  private void removeMoveIfNecessary() {
    List<Class<MoveAction>> toRemove = new ArrayList<>();

    for (Class<? extends EnvironmentalAction> a : this.getAvailableActions()) {
      if (a.getClass().isAssignableFrom(MoveAction.class)) {
        toRemove.add(MoveAction.class);
      }
    }
    this.getAvailableActions().removeAll(toRemove);
  }

  private void updateMoveActionIfNecessary(VacuumWorldPerception perception) {
    VacuumWorldCoordinates agentCoordinates = perception.getAgentCoordinates();
    VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);
    VacuumWorldCleaningAgent agent = agentLocation.getAgent();

    if (agent != null) {
      AgentFacingDirection facingDirection = agent.getFacingDirection();

      if (agentLocation.getNeighborLocation(facingDirection) == VacuumWorldLocationType.WALL) {
        removeMoveIfNecessary();
      } else {
        addMoveIfNecessary();
      }
    }
  }
}
