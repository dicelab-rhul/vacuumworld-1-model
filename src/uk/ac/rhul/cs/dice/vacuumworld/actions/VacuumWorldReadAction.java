package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.monitor.actions.ReadAction;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;

public class VacuumWorldReadAction extends ReadAction {

  private int lastCycleRead = 0;
  private CollectionRepresentation dirtCollection;

  /**
   * Constructor.
   * 
   * @param dirtCollection
   *          to read {@link uk.ac.rhul.cs.dice.vacuumworld.common.Dirt Dirt}
   *          related information from
   * @param agentCollection
   *          to read {@link uk.ac.rhul.cs.dice.gawl.interfaces.entities.Agent
   *          Agent} related information from
   * @param lastCycleRead
   *          the last cycle that data was read
   */
  public VacuumWorldReadAction(CollectionRepresentation dirtCollection,
      CollectionRepresentation agentCollection, int lastCycleRead) {
    super(agentCollection);
    this.lastCycleRead = lastCycleRead;
    this.dirtCollection = dirtCollection;
  }

  public int getLastCycleRead() {
    return lastCycleRead;
  }

  public CollectionRepresentation getDirtCollection() {
    return dirtCollection;
  }

  public CollectionRepresentation getAgentCollection() {
    return super.getCollection();
  }

  /**
   * @deprecated should use {@link #getAgentCollection()} or
   *             {@link #getDirtCollection()} instead
   */
  @Override
  @Deprecated
  public CollectionRepresentation getCollection() {
    throw new UnsupportedOperationException();
  }
}
