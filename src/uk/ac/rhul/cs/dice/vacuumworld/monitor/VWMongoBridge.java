package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.Collection;
import java.util.Iterator;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.DatabaseEvent;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.actions.WriteResult;
import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgent;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorActuator;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverActuator;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoConnector;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;

public class VWMongoBridge extends AbstractMongoBridge {

  private MongoConnector connector;
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Constructor.
   * 
   * @param connector
   *          to the Mongo database
   */
  public VWMongoBridge(MongoConnector connector,
      VacuumWorldSpaceRepresentation representation,
      CollectionRepresentation collectionRepresentation) {
    this.connector = connector;
    this.initialiseCollection(collectionRepresentation, representation);
  }

  @Override
  public WriteResult insert(CollectionRepresentation collectionRep,
      RefinedPerception perception) {
    VacuumWorldSpaceRepresentation p = (VacuumWorldSpaceRepresentation) perception;
    // check the agents to see if they exist, if they don't - add them
    Iterator<AgentRepresentation> iter = p.getAgents().values().iterator();
    while (iter.hasNext()) {
      AgentRepresentation value = iter.next();
      // update the cycle list.
      connector
          .pushToList(
              collectionRep.getCollectionName(),
              value.get_id(),
              "cycleList",
              createNewCycleObject(value.getX(), value.getY(),
                  value.getDirection()),
              new BasicDBObject("_id", value.get_id()));
    }
    // connector.insertDocument(collectionRep.getCollectionName(), json);
    // if they do exist - update them

    // check the dirt to see if they exist, if they don't - add them

    // if they do exist - update them

    return new WriteResult(ActionResult.ACTION_DONE, null);
  }

  private void initialiseCollection(
      CollectionRepresentation collectionRepresentation,
      VacuumWorldSpaceRepresentation representation) {
    insertAgents(collectionRepresentation, representation.getAgents().values());
  }

  public void insertAgents(CollectionRepresentation collectionRepresentation,
      Collection<AgentRepresentation> agents) {
    String json;
    Iterator<AgentRepresentation> iter = agents.iterator();
    while (iter.hasNext()) {
      try {
        AgentRepresentation agent = iter.next();
        json = mapper.writeValueAsString(new AgentDatabaseRepresentation(agent
            .get_id(), agent.getType(), agent.getSensors(), agent
            .getActuators(), 0, 0,
            new CycleDataRepresentation[] {}));
        System.out.println(json);
        connector.insertDocument(collectionRepresentation.getCollectionName(),
            json);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
  }

  // creates a new cycle object that should be pushed to the cycle list
  private BasicDBObject createNewCycleObject(int x, int y,
      AgentFacingDirection dir) {
    BasicDBObject cycle = new BasicDBObject();
    cycle.append("x", x);
    cycle.append("y", y);
    cycle.append("dir", dir.toString());
    return cycle;
  }

  @Override
  public ReadResult get(CollectionRepresentation collectionRep) {
    return null;
  }

  @Override
  public Result attempt(Event event, Space context) {
    return event.getAction().attempt(this, context);
  }

  @Override
  public void update(CustomObservable o, Object arg) {
    if (o instanceof ObserverActuator || o instanceof EvaluatorActuator) {
      if (arg instanceof DatabaseEvent) {
        System.out.println("UPDATE " + this.getClass().getSimpleName()
            + " FROM " + o.getClass().getSimpleName() + " " + arg);
        DatabaseEvent event = (DatabaseEvent) arg;
        Result result = event
            .attempt(this, event.getCollectionRepresentation());
        result.setRecipientId((String) ((DatabaseAgent) event.getActor())
            .getId());
        notifyObservers(result);
      }
    }
  }
}
