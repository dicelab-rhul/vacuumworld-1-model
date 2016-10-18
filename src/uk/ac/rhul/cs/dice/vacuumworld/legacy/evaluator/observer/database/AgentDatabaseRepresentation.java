package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorAgent;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.environment.AgentRepresentation;

/**
 * The representation of a {@link VacuumWorldCleaningAgent} in a MongoDB.
 * Converting an instance of this class to and from JSON Strings using
 * {@link ObjectMapper} is how the MongoDB communicates
 * {@link VacuumWorldCleaningAgent} data with {@link VacuumWorldMongoBridge}.
 * </br>
 * See also: {@link AgentRepresentation}. Implements (@link RefinedPerception}
 * as this class will be a part of the {@link Perception} of an
 * {@link EvaluatorAgent}.
 * 
 * @author Ben Wilkins
 *
 */
public class AgentDatabaseRepresentation implements RefinedPerception {

	// IMPORTANT TO KEEP variable names as are for JSON generation!
	private String _id;
	private VacuumWorldAgentType type;
	private int sensors;
	private int actuators;
	private int totalCleans;
	private int successfulCleans;
	private CycleDatabaseRepresentation[] cycleList;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the _id of the agent
	 * @param type
	 *            the {@link VacuumWorldAgentType} of the agent
	 * @param sensors
	 *            the number of sensors the agent has
	 * @param actuators
	 *            the number of actuators the agent has
	 * @param totalCleans
	 *            the number of {@link CleanAction CleanActions} performed by
	 *            the agent
	 * @param successfulCleans
	 *            the number of {@link CleanAction CleanActions} performed that
	 *            had the {@link ActionResult#ACTION_DONE} tag
	 * @param cycleList
	 *            a of {@link CycleDatabaseRepresentation
	 *            CycleDataRepresentations} used as a record of what the agent
	 *            has done during its lifetime. Note that this list should
	 *            usually be empty when using this class for 'to JSON'
	 *            conversion unless the {@link VacuumWorldCleaningAgent} has
	 *            previously existed e.g. it is being loaded into another
	 *            session.
	 */
	public AgentDatabaseRepresentation(String id, VacuumWorldAgentType type, int sensors, int actuators, int totalCleans, int successfulCleans, CycleDatabaseRepresentation[] cycleList) {
		this._id = id;
		this.type = type;
		this.sensors = sensors;
		this.actuators = actuators;
		this.totalCleans = totalCleans;
		this.successfulCleans = successfulCleans;
		this.cycleList = cycleList;
	}

	// Needed explicit for json mapper.
	public AgentDatabaseRepresentation() {
		super();
	}
	
	public String getId() {
		return this._id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public VacuumWorldAgentType getType() {
		return this.type;
	}

	public void setType(VacuumWorldAgentType type) {
		this.type = type;
	}

	public int getSensors() {
		return this.sensors;
	}

	public void setSensors(int sensors) {
		this.sensors = sensors;
	}

	public int getActuators() {
		return this.actuators;
	}

	public void setActuators(int actuators) {
		this.actuators = actuators;
	}

	public int getTotalCleans() {
		return this.totalCleans;
	}

	public void setTotalCleans(int totalCleans) {
		this.totalCleans = totalCleans;
	}

	public int getSuccessfulCleans() {
		return this.successfulCleans;
	}

	public void setSuccessfulCleans(int successfulCleans) {
		this.successfulCleans = successfulCleans;
	}

	public CycleDatabaseRepresentation[] getCycleList() {
		return this.cycleList;
	}

	public void setCycleList(CycleDatabaseRepresentation[] cycleList) {
		this.cycleList = cycleList;
	}
}