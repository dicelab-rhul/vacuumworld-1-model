package uk.ac.rhul.cs.dice.vacuumworld.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.common.Obstacle;

public class VacuumWorldLocation implements Location, Lockable {
	private VacuumWorldCoordinates coordinates;
	private VacuumWorldCleaningAgent agent;
	private Obstacle obstacle;
	private VacuumWorldLocationType type;
	private VacuumWorldLocationType northernLocationType;
	private VacuumWorldLocationType southernLocationType;
	private VacuumWorldLocationType westernLocationType;
	private VacuumWorldLocationType easternLocationType;
	private boolean underSharedReadLock;
	private int readers;
	private boolean underExclusiveWriteLock;
	
	public VacuumWorldLocation(VacuumWorldCoordinates coordinates, VacuumWorldLocationType type, int maxX, int maxY) {
		int x = coordinates.getX();
		int y = coordinates.getY();
		
		this.coordinates = coordinates;
		this.type = type;
		this.northernLocationType = y == 0 ? VacuumWorldLocationType.WALL : VacuumWorldLocationType.NORMAL;
		this.southernLocationType = y == maxY ? VacuumWorldLocationType.WALL : VacuumWorldLocationType.NORMAL;
		this.westernLocationType = x == 0 ? VacuumWorldLocationType.WALL : VacuumWorldLocationType.NORMAL;
		this.easternLocationType = x == maxX ? VacuumWorldLocationType.WALL : VacuumWorldLocationType.NORMAL;
		
		this.underSharedReadLock = false;
		this.readers = 0;
		this.underExclusiveWriteLock = false;
	}

	public VacuumWorldCoordinates getCoordinates() {
		return this.coordinates;
	}


	public void setCoordinates(VacuumWorldCoordinates coordinates) {
		this.coordinates = coordinates;
	}


	public VacuumWorldCleaningAgent getAgent() {
		return this.agent;
	}

	public boolean isFree() {
		return !(isAnAgentPresent() || isAnObstaclePresent());
	}
	
	public boolean isAnAgentPresent() {
		return this.agent != null;
	}

	public void addAgent(VacuumWorldCleaningAgent agent) {
		this.agent = agent;
	}
	
	public void removeAgent() {
		this.agent = null;
	}
	
	public boolean isAnObstaclePresent() {
		if(this.obstacle == null) {
			return false;
		}
		
		return true;
	}
	
	public Obstacle getObstacle() {
		return this.obstacle;
	}
	
	public boolean isDirtPresent() {
		if(this.obstacle == null) {
			return false;
		}
		
		return this.obstacle instanceof Dirt;
	}
	
	public Dirt getDirt() {
		if(this.obstacle == null || !(this.obstacle instanceof Dirt)) {
			return null;
		}
		else {
			return (Dirt) this.obstacle;
		}
	}
	
	public void setObstacle(Obstacle obstacle) {
		this.obstacle = obstacle;
	}
	
	public void setDirt(Dirt dirt) {
		this.obstacle = dirt;
	}
	
	public void removeDirt() {
		if(isDirtPresent()) {
			this.obstacle = null;
		}
	}

	public VacuumWorldLocationType getType() {
		return this.type;
	}


	public void setType(VacuumWorldLocationType type) {
		this.type = type;
	}


	public VacuumWorldLocationType getNorthernLocationType() {
		return this.northernLocationType;
	}


	public void setNorthernLocationType(VacuumWorldLocationType northernLocationType) {
		this.northernLocationType = northernLocationType;
	}


	public VacuumWorldLocationType getSouthernLocationType() {
		return this.southernLocationType;
	}


	public void setSouthernLocationType(VacuumWorldLocationType southernLocationType) {
		this.southernLocationType = southernLocationType;
	}


	public VacuumWorldLocationType getWesternLocationType() {
		return this.westernLocationType;
	}


	public void setWesternLocationType(VacuumWorldLocationType westernLocationType) {
		this.westernLocationType = westernLocationType;
	}


	public VacuumWorldLocationType getEasternLocationType() {
		return this.easternLocationType;
	}


	public void setEasternLocationType(VacuumWorldLocationType easternLocationType) {
		this.easternLocationType = easternLocationType;
	}
	
	public VacuumWorldLocationType getNeighborLocation(AgentFacingDirection direction) {
		switch(direction) {
		case NORTH:
			return getNorthernLocationType();
		case SOUTH:
			return getSouthernLocationType();
		case WEST:
			return getWesternLocationType();
		case EAST:
			return getEasternLocationType();
		default:
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result + ((easternLocationType == null) ? 0 : easternLocationType.hashCode());
		result = prime * result + ((northernLocationType == null) ? 0 : northernLocationType.hashCode());
		result = prime * result + ((obstacle == null) ? 0 : obstacle.hashCode());
		result = prime * result + readers;
		result = prime * result + ((southernLocationType == null) ? 0 : southernLocationType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (underExclusiveWriteLock ? 1231 : 1237);
		result = prime * result + (underSharedReadLock ? 1231 : 1237);
		result = prime * result + ((westernLocationType == null) ? 0 : westernLocationType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VacuumWorldLocation other = (VacuumWorldLocation) obj;
		if (agent == null) {
			if (other.agent != null)
				return false;
		} else if (!agent.equals(other.agent))
			return false;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (easternLocationType != other.easternLocationType)
			return false;
		if (northernLocationType != other.northernLocationType)
			return false;
		if (obstacle == null) {
			if (other.obstacle != null)
				return false;
		} else if (!obstacle.equals(other.obstacle))
			return false;
		if (readers != other.readers)
			return false;
		if (southernLocationType != other.southernLocationType)
			return false;
		if (type != other.type)
			return false;
		if (underExclusiveWriteLock != other.underExclusiveWriteLock)
			return false;
		if (underSharedReadLock != other.underSharedReadLock)
			return false;
		if (westernLocationType != other.westernLocationType)
			return false;
		return true;
	}

	@Override
	public synchronized void getSharedReadLock() throws AlreadyLockedException {
		if(this.underExclusiveWriteLock) {
			throw new AlreadyLockedException("Object is already under write lock.");
		}
		
		this.underSharedReadLock = true;
		this.readers++;
	}

	@Override
	public synchronized void getExclusiveWriteLock() throws AlreadyLockedException {
		if(this.underSharedReadLock) {
			throw new AlreadyLockedException("Object is already under read lock.");
		}
		else if(this.underExclusiveWriteLock) {
			throw new AlreadyLockedException("Object is already under write lock.");
		}
		else {
			this.underExclusiveWriteLock = true;
		}
	}

	@Override
	public synchronized void releaseSharedReadLock() {
		if(this.underSharedReadLock) {
			this.readers = this.readers == 0 ? 0 : this.readers - 1;
			this.underSharedReadLock = this.readers == 0 ? false : true;
		}
	}

	@Override
	public synchronized void releaseExclusiveWriteLock() {
		if(this.underExclusiveWriteLock) {
			this.underExclusiveWriteLock = false;
		}
	}
}