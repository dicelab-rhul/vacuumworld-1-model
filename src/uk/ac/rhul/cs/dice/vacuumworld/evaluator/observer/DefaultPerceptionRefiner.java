package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.monitor.common.PerceptionRefiner;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;

/**
 * A {@link PerceptionRefiner} that does nothing. Perceptions do not need to be
 * refined in VacuumWorld because the {@link VacuumWorldRepresentation} used in
 * {@link VacuumWorldMonitoringContainer} is already refined - the
 * {@link ObserverAgent ObserverAgents} using this class do not need to refine
 * their {@link Perception} because they perceive a {@link RefinedPerception}.
 * 
 * @author Ben Wilkins
 *
 */
public class DefaultPerceptionRefiner implements PerceptionRefiner {

  @Override
  public RefinedPerception refinePerception(Perception perception) {
    return (RefinedPerception) perception;
  }
}
