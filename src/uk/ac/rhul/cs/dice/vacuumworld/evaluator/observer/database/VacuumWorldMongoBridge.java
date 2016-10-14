package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.DatabaseAction;
import uk.ac.rhul.cs.dice.monitor.actions.DatabaseEvent;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.actions.WriteResult;
import uk.ac.rhul.cs.dice.monitor.agents.DatabaseAgent;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorActuator;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverActuator;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldReadAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldReadResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldWriteAction;
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
 * see: {@link CycleDatabaseRepresentation}, . These Objects are represented in
 * the database as: {@link AgentDatabaseRepresentation},
 * {@link DirtDatabaseRepresentation}, {@link SpeechDatabaseRepresentation}.
 * 
 * @author Ben Wilkins
 *
 */
public class VacuumWorldMongoBridge extends AbstractMongoBridge {
	// Connector to the MongoDB
	private VacuumWorldMongoConnector connector;
	// Used to map java objects to JSON Strings for read and write operations
	private ObjectMapper mapper = new ObjectMapper();

	// Fields names for performing database updates (names are used as keys)
	private static final String[] SPEECHDATABASEREPRESENTATIONFIELDS = getAllNonStaticFieldNames(SpeechDatabaseRepresentation.class);
	private static final String[] CYCLEDATABASEREPRESENTATIONFIELDS = getAllNonStaticFieldNames(CycleDatabaseRepresentation.class);
	private static final String[] AGENTDATABASEREPRESENTATIONFIELDS = getAllNonStaticFieldNames(AgentDatabaseRepresentation.class);
	private static final String[] DIRTDATABASEREPRESENTATIONFIELDS = getAllNonStaticFieldNames(DirtDatabaseRepresentation.class);

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
	public VacuumWorldMongoBridge(VacuumWorldMongoConnector connector, VacuumWorldSpaceRepresentation representation, CollectionRepresentation dirtCollection, CollectionRepresentation agentCollection) {
		this.connector = connector;
		this.initialiseCollection(dirtCollection, agentCollection, representation);
	}

	/**
	 * Insert method specific to VacuumWorld.
	 */
	@Override
	public synchronized WriteResult write(DatabaseAction a) {
		VacuumWorldWriteAction action = (VacuumWorldWriteAction) a;
		VacuumWorldSpaceRepresentation p = (VacuumWorldSpaceRepresentation) action.getPerception();
		
		if (action.getAgentCollection() != null) {
			doAgents(action.getAgentCollection(), p);
		}
		
		if (action.getDirtCollection() != null) {
			doDirt(action.getDirtCollection(), p);
		}
		
		return new WriteResult(ActionResult.ACTION_DONE, null);
	}

	private void doDirt(CollectionRepresentation collectionRep, VacuumWorldSpaceRepresentation p) {
		if (!p.getRemovedDirts().isEmpty()) {
			checkForDirts(collectionRep, p);
		}
	}

	private void checkForDirts(CollectionRepresentation collection, VacuumWorldSpaceRepresentation p) {
		if (!p.getRemovedDirts().isEmpty()) {
			for (DirtRepresentation rep : p.getRemovedDirts()) {
				this.connector.incrementSingleValueInDocument(collection.getCollectionName(), DIRTDATABASEREPRESENTATIONFIELDS[0], rep.getId(), DIRTDATABASEREPRESENTATIONFIELDS[5], Utils.getCycleNumber() - 1);
			}
			
			p.getRemovedDirts().clear();
			notifyDatabaseChanged(collection);
		}
	}

	private void doAgents(CollectionRepresentation collection, VacuumWorldSpaceRepresentation p) {
		for (AgentRepresentation value : p.getAgents().values()) {
			this.connector.pushToList(collection.getCollectionName(), value.getId(), AGENTDATABASEREPRESENTATIONFIELDS[6], createNewCycleObject(value.getX(), value.getY(), value.getDirection(), value.getLastSpeechAction()), new BasicDBObject(AGENTDATABASEREPRESENTATIONFIELDS[0], value.getId()));
			checkForClean(collection, value);
		}
		
		notifyDatabaseChanged(collection);
	}

	private BasicDBObject createNewSpeechObject(SpeechAction lastSpeechAction) {
		if (lastSpeechAction == null) {
			return null;
		}
		
		String[] recipients;
		
		if (lastSpeechAction.getRecipientsIds() == null) {
			recipients = new String[] { SpeechDatabaseRepresentation.ALLRECIPIENTS };
		}
		else {
			recipients = lastSpeechAction.getRecipientsIds().toArray(new String[] {});
		}
		
		BasicDBObject speech = new BasicDBObject();
		speech.append(SPEECHDATABASEREPRESENTATIONFIELDS[0], lastSpeechAction.getPayload().getPayload());
		speech.append(SPEECHDATABASEREPRESENTATIONFIELDS[1], recipients);
		
		return speech;
	}

	// creates a new cycle object that should be pushed to the cycle list
	private BasicDBObject createNewCycleObject(int x, int y, AgentFacingDirection dir, SpeechAction lastSpeechAction) {
		BasicDBObject cycle = new BasicDBObject();
		cycle.append(CYCLEDATABASEREPRESENTATIONFIELDS[0], x);
		cycle.append(CYCLEDATABASEREPRESENTATIONFIELDS[1], y);
		cycle.append(CYCLEDATABASEREPRESENTATIONFIELDS[2], dir.toString());
		cycle.append(CYCLEDATABASEREPRESENTATIONFIELDS[3], createNewSpeechObject(lastSpeechAction));
		
		return cycle;
	}

	private void checkForClean(CollectionRepresentation collectionRep, AgentRepresentation value) {
		if (value.isClean()) {
			// increment total cleans.
			this.connector.incrementSingleValueInDocument(collectionRep.getCollectionName(), AGENTDATABASEREPRESENTATIONFIELDS[0], value.getId(), AGENTDATABASEREPRESENTATIONFIELDS[4], 1);
			checkForSuccessfulClean(collectionRep, value);
		}
	}

	private void checkForSuccessfulClean(CollectionRepresentation collectionRep, AgentRepresentation value) {
		if (value.isSuccessfulClean()) {
			this.connector.incrementSingleValueInDocument(collectionRep.getCollectionName(), AGENTDATABASEREPRESENTATIONFIELDS[0], value.getId(), AGENTDATABASEREPRESENTATIONFIELDS[5], 1);
		}
	}

	/*
	 * This read method only reads from the agents collection! if you want to
	 * make an evaluation strategy that requires dirt reading then this will
	 * need to be added to VacuumWorldMongoConnector and this method.
	 */
	@Override
	public synchronized ReadResult read(DatabaseAction a) {
		VacuumWorldReadAction action = (VacuumWorldReadAction) a;

		Set<AgentDatabaseRepresentation> agents = new HashSet<>();
		// dirts is null in this case because it is not required by the step
		// evaluation strategy!
		Set<DirtDatabaseRepresentation> dirts = null;
		List<String> jsons = this.connector.efficentAgentRead(action.getAgentCollection(), action.getLastCycleRead());
		
		for (String s : jsons) {
			AgentDatabaseRepresentation r = mapAgent(s);
			agents.add(r);
		}
		
		return new VacuumWorldReadResult(ActionResult.ACTION_DONE, agents, dirts, null);
	}

	public DirtDatabaseRepresentation mapDirt(String json) {
		try {
			return this.mapper.readValue(json, DirtDatabaseRepresentation.class);
		}
		catch (IOException e) {
			Utils.log(e);
			return null;
		}
	}

	public AgentDatabaseRepresentation mapAgent(String json) {
		try {
			return this.mapper.readValue(json, AgentDatabaseRepresentation.class);
		}
		catch (IOException e) {
			Utils.log(e);
			return null;
		}
	}

	@Override
	public synchronized Result attempt(Event event, Space context) {
		return event.getAction().attempt(this, context);
	}

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if (o instanceof ObserverActuator || o instanceof EvaluatorActuator) {
			manageActuator(arg);
		}
	}

	private void manageActuator(Object arg) {
		if (arg instanceof DatabaseEvent) {
			DatabaseEvent event = (DatabaseEvent) arg;
			Result result = event.attempt(this, null);
			List<String> recipients = new ArrayList<>();
			recipients.add((String) ((DatabaseAgent<?,?>) event.getActor()).getId());
			result.setRecipientsIds(recipients);
			notifyObservers(result);
		}
	}

	public static String[] getAllNonStaticFieldNames(Class<?> c) {
		Field[] fields = c.getDeclaredFields();
		ArrayList<String> names = new ArrayList<>(fields.length);
		
		for (Field f : fields) {
			if (!Modifier.isStatic(f.getModifiers())) {
				names.add(f.getName());
			}
		}
		return names.toArray(new String[] {});
	}

	/*
	 * Creates the initial collection and all the dirts and agents that are in
	 * the initial state of Vacuum World.
	 */
	private void initialiseCollection(CollectionRepresentation dirtCollection, CollectionRepresentation agentCollection, VacuumWorldSpaceRepresentation representation) {
		insertAgents(agentCollection, representation.getAgents().values());
		insertDirts(dirtCollection, representation.getDirts());
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
		for (Entry<VacuumWorldCoordinates, DirtRepresentation> entry : map.entrySet()) {
			VacuumWorldCoordinates coord = entry.getKey();
			DirtRepresentation value = entry.getValue();

			insertDirts(collectionRepresentation, value, coord);
		}
	}

	private void insertDirts(CollectionRepresentation collectionRepresentation, DirtRepresentation value, VacuumWorldCoordinates coord) {
		try {
			String json = this.mapper.writeValueAsString(new DirtDatabaseRepresentation(value.getId(), value.getType(), coord.getX(), coord.getY(), 0, 0));
			Utils.logWithClass(this.getClass().getSimpleName(), "\n" + json);
			this.connector.insertDocument(collectionRepresentation.getCollectionName(), json);
		}
		catch (JsonProcessingException e) {
			Utils.log(e);
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
		for (AgentRepresentation agent : agents) {
			insertAgents(collectionRepresentation, agent);
		}
	}

	private void insertAgents(CollectionRepresentation collectionRepresentation, AgentRepresentation agent) {
		try {
			String json = this.mapper.writeValueAsString(new AgentDatabaseRepresentation(agent.getId(), agent.getType(), agent.getSensors(), agent.getActuators(), 0, 0, new CycleDatabaseRepresentation[] {}));
			Utils.logWithClass(this.getClass().getSimpleName(), "\n" + json);
			this.connector.insertDocument(collectionRepresentation.getCollectionName(), json);
		}
		catch (JsonProcessingException e) {
			Utils.log(e);
		}
	}
}