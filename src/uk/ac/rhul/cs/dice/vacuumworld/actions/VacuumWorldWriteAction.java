package uk.ac.rhul.cs.dice.vacuumworld.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.monitor.actions.WriteAction;
import uk.ac.rhul.cs.dice.monitor.actions.WriteResult;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.VacuumWorldMongoBridge;

public class VacuumWorldWriteAction extends WriteAction {

  private CollectionRepresentation dirtCollection;

  /**
   * Constructor.
   * 
   * @param dirtCollection
   *          to write {@link uk.ac.rhul.cs.dice.vacuumworld.common.Dirt Dirt} related
   *          information to
   * @param agentCollection
   *          to write {@link uk.ac.rhul.cs.dice.gawl.interfaces.entities.Agent Agent}
   *          related information to
   * @param perception
   *          to write
   */
  public VacuumWorldWriteAction(CollectionRepresentation dirtCollection,
      CollectionRepresentation agentCollection, RefinedPerception perception) {
    super(agentCollection, perception);
    this.dirtCollection = dirtCollection;
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
  
  @Override
  public Result perform(Physics databasePhysics, Space database) {
    VacuumWorldMongoBridge bridge = (VacuumWorldMongoBridge) databasePhysics;
    WriteResult result = bridge.write(this);
    return result;
  } 
}
