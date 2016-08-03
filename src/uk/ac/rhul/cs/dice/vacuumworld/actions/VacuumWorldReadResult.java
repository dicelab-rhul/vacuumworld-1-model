package uk.ac.rhul.cs.dice.vacuumworld.actions;

import java.util.HashSet;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.AgentDatabaseRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.DirtDatabaseRepresentation;

/**
 * An extension of {@link ReadResult} specific to Vacuum World. Contains two
 * sets of perceptions one for {@link DirtDatabaseRepresentation} and one for
 * {@link AgentDatabaseRepresentation}.
 * 
 * @author Ben Wilkins
 *
 */
public class VacuumWorldReadResult extends ReadResult {

  private HashSet<DirtDatabaseRepresentation> dirts;
  private HashSet<AgentDatabaseRepresentation> agents;

  /**
   * Constructor.
   * 
   * @param result
   * @param exception
   * @param agents
   *          a set containing only {@link AgentDatabaseRepresentation}
   * @param dirts
   *          a set containing only {@link DirtDatabaseRepresentation}
   * @param recipientIds
   */
  public VacuumWorldReadResult(ActionResult result, Exception exception,
      HashSet<AgentDatabaseRepresentation> agents,
      HashSet<DirtDatabaseRepresentation> dirts, List<String> recipientIds) {
    super(result, exception, null, recipientIds);
    this.dirts = dirts;
    this.agents = agents;
  }

  /**
   * Constructor.
   * 
   * @param result
   * @param agents
   *          a set containing only {@link AgentDatabaseRepresentation}
   * @param dirts
   *          a set containing only {@link DirtDatabaseRepresentation}
   * @param recipientIds
   */
  public VacuumWorldReadResult(ActionResult result,
      HashSet<AgentDatabaseRepresentation> agents,
      HashSet<DirtDatabaseRepresentation> dirts, List<String> recipientIds) {
    super(result, null, recipientIds);
    this.dirts = dirts;
    this.agents = agents;
  }

  @Override
  public HashSet<RefinedPerception> getPerceptions() {
    HashSet<RefinedPerception> all = new HashSet<>();
    all.addAll(this.agents);
    all.addAll(this.dirts);
    return all;
  }

  public HashSet<AgentDatabaseRepresentation> getAgents() {
    return this.agents;
  }

  public HashSet<DirtDatabaseRepresentation> getDirts() {
    return this.dirts;
  }
}
