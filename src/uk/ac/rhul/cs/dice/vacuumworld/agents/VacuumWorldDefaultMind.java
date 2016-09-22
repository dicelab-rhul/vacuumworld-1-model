package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public abstract class VacuumWorldDefaultMind extends AbstractAgentMind {

  private int perceptionRange;
  private boolean canSeeBehind;

  private Set<Class<? extends AbstractAction>> actions;
  private List<Class<? extends EnvironmentalAction>> availableActions;

  private List<DefaultActionResult> lastCyclePerceptions;
  private EnvironmentalAction nextAction;

  public VacuumWorldDefaultMind() {
    this.setLastCyclePerceptions(new ArrayList<>());
  }
  
  @Override
  public final void update(CustomObservable o, Object arg) {
    if (o instanceof VacuumWorldDefaultBrain && arg instanceof List<?>) {
      for(Object result : (List<?>) arg) {
        if(result instanceof DefaultActionResult) {
          this.getLastCyclePerceptions().add((DefaultActionResult) result);
        }
      }
    }
  }

  protected final EnvironmentalAction buildPhysicalAction(
      Class<? extends EnvironmentalAction> actionPrototype) {
    try {
      return actionPrototype.newInstance();
    } catch (Exception e) {
      Utils.log(e);
      return null;
    }
  }

  protected final SpeechAction buildSpeechAction(String senderId,
      ArrayList<String> recipientIds, VacuumWorldSpeechPayload payload) {
    try {
      Constructor<SpeechAction> constructor = SpeechAction.class
          .getConstructor(String.class, List.class, Payload.class);
      return constructor.newInstance(senderId, recipientIds, payload);
    } catch (Exception e) {
      Utils.log(e);
      return null;
    }
  }

  public void setAvailableActions(Set<Class<? extends AbstractAction>> actions) {
    this.setActions(actions);
  }

  public void setCanSeeBehind(boolean canSeeBehind) {
    this.canSeeBehind = canSeeBehind;
  }

  public void setPerceptionRange(int preceptionRange) {
    this.perceptionRange = preceptionRange;
  }

  public List<Class<? extends EnvironmentalAction>> getAvailableActions() {
    return availableActions;
  }

  public void setAvailableActions(
      List<Class<? extends EnvironmentalAction>> availableActions) {
    this.availableActions = availableActions;
  }

  public int getPerceptionRange() {
    return perceptionRange;
  }

  public boolean isCanSeeBehind() {
    return canSeeBehind;
  }

  public EnvironmentalAction getNextAction() {
    return nextAction;
  }

  public void setNextAction(EnvironmentalAction nextAction) {
    this.nextAction = nextAction;
  }

  public List<DefaultActionResult> getLastCyclePerceptions() {
    return lastCyclePerceptions;
  }

  public void setLastCyclePerceptions(
      List<DefaultActionResult> lastCyclePerceptions) {
    this.lastCyclePerceptions = lastCyclePerceptions;
  }

  public Set<Class<? extends AbstractAction>> getActions() {
    return actions;
  }

  public void setActions(Set<Class<? extends AbstractAction>> actions) {
    this.actions = actions;
  }
}