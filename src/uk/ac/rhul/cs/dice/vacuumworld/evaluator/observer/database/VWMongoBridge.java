package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.environment.AgentRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.DirtRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpaceRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;

/**
 * Bridge to the database specifically for Vacuum World. Extends
 * {@link AbstractMongoBridge} and so contains -
 * {@link AbstractMongoBridge#insert(CollectionRepresentation, RefinedPerception)}
 * and {@link AbstractMongoBridge#get(CollectionRepresentation)} functionality.
 * These methods are specific for Vacuum World and so involve writing and
 * reading Dirt and Agents to and from a Mongo Database in a refined form.
 * Specified by: {@link AgentRepresentation}, {@link DirtRepresentation} also
 * see: {@link CycleDataRepresentation}. These Objects are represented in the
 * database as: {@link AgentDatabaseRepresentation},
 * {@link DirtDatabaseRepresentation}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWMongoBridge extends AbstractMongoBridge {
	// Connector to the MongoDB
	private MongoConnector connector;
	// Used to map java objects to JSON Strings for read and write operations
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Constructor.
	 * 
	 * @param connector
	 *            to the Mongo Database
	 * @param representation
	 *            of Vacuum World that will be used to construct the Mongo
	 *            Database initial state
	 * @param collectionRepresentation
	 *            a representation of the collection that will be used in the
	 *            database
	 */
	public VWMongoBridge(MongoConnector connector, VacuumWorldSpaceRepresentation representation, CollectionRepresentation collectionRepresentation) {
		this.connector = connector;
		this.initialiseCollection(collectionRepresentation, representation);
	}

	/**
	 * Insert method specific to Vacuum World.
	 */
	@Override
	public synchronized WriteResult insert(CollectionRepresentation collectionRep, RefinedPerception perception) {
		VacuumWorldSpaceRepresentation p = (VacuumWorldSpaceRepresentation) perception;
		checkForAgents(collectionRep, p);
		checkForDirt(collectionRep, p);
		
		return new WriteResult(ActionResult.ACTION_DONE, null);
	}

	private void checkForDirt(CollectionRepresentation collectionRep, VacuumWorldSpaceRepresentation p) {
		if (!p.getRemovedDirts().isEmpty()) {
			Utils.log("SOME DIRT WAS REMOVED!");
			checkForDirts(collectionRep, p);
		}
	}

	private void checkForDirts(CollectionRepresentation collectionRep, VacuumWorldSpaceRepresentation p) {
		for(DirtRepresentation rep : p.getRemovedDirts()) {
			this.connector.incrementSingleValueInDocument(collectionRep.getCollectionName(), "_id", rep.get_id(), "endCycle", Utils.getCycleNumber());
			p.getRemovedDirts().remove(rep);
		}
	}

	private void checkForAgents(CollectionRepresentation collectionRep, VacuumWorldSpaceRepresentation p) {
		// check the agents to see if they exist, if they don't - add them
		for(AgentRepresentation value : p.getAgents().values()) {
			// update the cycle list.
			this.connector.pushToList(collectionRep.getCollectionName(), value.get_id(), "cycleList", createNewCycleObject(value.getX(), value.getY(), value.getDirection()), new BasicDBObject("_id", value.get_id()));
			checkForClean(collectionRep, value);
		}
	}

	private void checkForClean(CollectionRepresentation collectionRep, AgentRepresentation value) {
		if (value.isClean()) {
			// increment total cleans.
			this.connector.incrementSingleValueInDocument(collectionRep.getCollectionName(), "_id", value.get_id(), "totalCleans", 1);
			checkForSuccessfulClean(collectionRep, value);
		}
	}

	private void checkForSuccessfulClean(CollectionRepresentation collectionRep, AgentRepresentation value) {
		if (value.isSuccessfulClean()) {
			this.connector.incrementSingleValueInDocument(collectionRep.getCollectionName(), "_id", value.get_id(), "successfulCleans", 1);
		}
	}

	/*
	 * Creates the initial collection and all the dirts and agents that are in
	 * the initial state of Vacuum World.
	 */
	private void initialiseCollection(CollectionRepresentation collectionRepresentation, VacuumWorldSpaceRepresentation representation) {
		insertAgents(collectionRepresentation, representation.getAgents().values());
		insertDirts(collectionRepresentation, representation.getDirts());
	}

	/**
	 * Insert some {@link Dirt} represented by {@link DirtRepresentation} into
	 * the database. This method should only be called for {@link Dirt Dirts}
	 * that haven't already been added to the MongoDB - the {@link Dirt} must be
	 * new.
	 * 
	 * @param collectionRepresentation
	 *            collection to use
	 * @param map
	 *            a mapping of {@link VacuumWorldCoordinates} to
	 *            {@link DirtRepresentation} specifying new {@link Dirt} to add
	 */
	public synchronized void insertDirts(CollectionRepresentation collectionRepresentation, Map<VacuumWorldCoordinates, DirtRepresentation> map) {
		String json;
		
		for(Entry<VacuumWorldCoordinates, DirtRepresentation> entry : map.entrySet()) {
			VacuumWorldCoordinates coord = entry.getKey();
			DirtRepresentation value = entry.getValue();
			
			try {
				json = this.mapper.writeValueAsString(new DirtDatabaseRepresentation(value.get_id(), value.getType(), coord.getX(), coord.getY(), 0, 0));
				Utils.log(json);
				this.connector.insertDocument(collectionRepresentation.getCollectionName(), json);
			}
			catch (JsonProcessingException e) {
				Utils.log(e);
			}
		}
	}

	/**
	 * Insert some {@link VacuumWorldCleaningAgent} represented by
	 * {@link AgentRepresentation} into the database. This method should only be
	 * called for {@link VacuumWorldCleaningAgent Agents} that haven't already
	 * been added to the MongoDB - the {@link VacuumWorldCleaningAgent Agent}
	 * must be new.
	 * 
	 * @param collectionRepresentation
	 *            collection to use
	 * @param agents
	 *            a {@link Collection} of {@link AgentRepresentation
	 *            AgentRepresentations} specifying new
	 *            {@link VacuumWorldCleaningAgent Agents} to add
	 */
	public void insertAgents(CollectionRepresentation collectionRepresentation, Collection<AgentRepresentation> agents) {
		String json;
		
		for(AgentRepresentation agent : agents) {
			try {
				json = this.mapper.writeValueAsString(new AgentDatabaseRepresentation(agent.get_id(), agent.getType(), agent.getSensors(), agent.getActuators(), 0, 0, new CycleDataRepresentation[] {}));
				Utils.log(json);
				this.connector.insertDocument(collectionRepresentation.getCollectionName(), json);
			}
			catch (JsonProcessingException e) {
				Utils.log(e);
			}
		}
	}

	// creates a new cycle object that should be pushed to the cycle list
	private BasicDBObject createNewCycleObject(int x, int y, AgentFacingDirection dir) {
		BasicDBObject cycle = new BasicDBObject();
		cycle.append("x", x);
		cycle.append("y", y);
		cycle.append("dir", dir.toString());
		return cycle;
	}

	@Override
	public synchronized ReadResult get(CollectionRepresentation collectionRep) {
		HashSet<RefinedPerception> ps = new HashSet<>();
		ps.add(new DirtDatabaseRepresentation(null, null, 0, 0, 0, 0));
		return new ReadResult(ActionResult.ACTION_DONE, ps, null);
	}

	@Override
	public synchronized Result attempt(Event event, Space context) {
		return event.getAction().attempt(this, context);
	}

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if (o instanceof ObserverActuator || o instanceof EvaluatorActuator) {
			manageActuator(o, arg);
		}
	}

	private void manageActuator(CustomObservable o, Object arg) {
		if (arg instanceof DatabaseEvent) {
			Utils.log("UPDATE " + this.getClass().getSimpleName() + " FROM " + o.getClass().getSimpleName() + " " + arg);
			DatabaseEvent event = (DatabaseEvent) arg;
			Result result = event.attempt(this, event.getCollectionRepresentation());
			List<String> recipients = new ArrayList<>();
			recipients.add((String) ((DatabaseAgent) event.getActor()).getId());
			result.setRecipientsIds(recipients);
			notifyObservers(result);
		}
	}
}