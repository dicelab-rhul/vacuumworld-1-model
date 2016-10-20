package uk.ac.rhul.cs.dice.vacuumworld.legacy.environment;

import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database.AgentDatabaseRepresentation;

/**
 * The representation of a {@link VacuumWorldCleaningAgent} used internally by
 * {@link VacuumWorldLegacyMonitoringContainer} to build a useful representation of
 * what is happening inside its sub container - {@link VacuumWorldSpace}. </br>
 * See also: {@link AgentDatabaseRepresentation},
 * {@link VacuumWorldSpaceRepresentation}.
 * 
 * @author Ben Wilkins
 *
 */
public class AgentRepresentation {

	private String _id;
	private VacuumWorldAgentType type;
	private int sensors;
	private int actuators;
	// did the agent perform a clean this cycle
	private boolean clean;
	// was the clean successful
	private boolean successfulClean;
	private ActorFacingDirection direction;
	private int x;
	private int y;
	private SpeechAction lastSpeechAction;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the id of the agent
	 * @param type
	 *            the {@link VacuumWorldAgentType} of the agent
	 * @param sensors
	 *            the number of sensors the agent has
	 * @param actuators
	 *            the number of actuators the agent has
	 * @param direction
	 *            that the agent is currently facing
	 * @param x
	 *            position
	 * @param y
	 *            position
	 */
	public AgentRepresentation(String id, VacuumWorldAgentType type, int sensors, int actuators, ActorFacingDirection direction, int x, int y) {
		this._id = id;
		this.type = type;
		this.sensors = sensors;
		this.actuators = actuators;
		this.direction = direction;
		this.x = x;
		this.y = y;
		this.setClean(false);
		this.setSuccessfulClean(false);
	}

	// Needed explicit for json mapper.
	public AgentRepresentation() {
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

	public ActorFacingDirection getDirection() {
		return this.direction;
	}

	public void setDirection(ActorFacingDirection direction) {
		this.direction = direction;
	}

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isClean() {
		return this.clean;
	}

	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public boolean isSuccessfulClean() {
		return this.successfulClean;
	}

	public void setSuccessfulClean(boolean successfulClean) {
		this.successfulClean = successfulClean;
	}

	public AgentRepresentation duplicate() {
		AgentRepresentation rep = new AgentRepresentation(this._id, this.type, this.sensors, this.actuators, this.direction, this.x, this.y);
		rep.setClean(this.isClean());
		rep.setSuccessfulClean(this.isSuccessfulClean());
		rep.setLastSpeechAction(this.lastSpeechAction);
		
		return rep;
	}

	public SpeechAction getLastSpeechAction() {
		return this.lastSpeechAction;
	}

	public void setLastSpeechAction(SpeechAction lastSpeechAction) {
		this.lastSpeechAction = lastSpeechAction;
	}
}