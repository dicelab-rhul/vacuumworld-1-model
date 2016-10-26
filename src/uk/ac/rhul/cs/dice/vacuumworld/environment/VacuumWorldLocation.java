package uk.ac.rhul.cs.dice.vacuumworld.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.Location;
import uk.ac.rhul.cs.dice.gawl.interfaces.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Obstacle;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldLocation implements Location, Lockable {
	private VacuumWorldCoordinates coordinates;
	private VacuumWorldCleaningAgent agent;
	private User user;
	private Obstacle obstacle;
	private Obstacle oldObstacle;
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
	
	public User getUser() {
		return this.user;
	}

	public boolean isFree() {
		return !isNotFree();
	}
	
	public boolean isNotFree() {
		return isAnAgentPresent() || isAnObstaclePresent() || isAUserPresent();
	}
	
	public boolean isAUserPresent() {
		return this.user != null;
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
	
	public void addUser(User user) {
		this.user = user;
	}
	
	public void removeUser() {
		this.user = null;
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
	
	public Dirt getOldDirt() {
		if(this.oldObstacle == null || !(this.oldObstacle instanceof Dirt)) {
			return null;
		}
		else {
			return (Dirt) this.oldObstacle;
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
			this.oldObstacle = ((Dirt) this.obstacle).duplicate();
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
	
	public VacuumWorldLocationType getNeighborLocationType(ActorFacingDirection direction) {
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
		result = prime * result + ((this.agent == null) ? 0 : this.agent.hashCode());
		result = prime * result + ((this.user == null) ? 0 : this.user.hashCode());
		result = prime * result + ((this.coordinates == null) ? 0 : this.coordinates.hashCode());
		result = prime * result + ((this.easternLocationType == null) ? 0 : this.easternLocationType.hashCode());
		result = prime * result + ((this.northernLocationType == null) ? 0 : this.northernLocationType.hashCode());
		result = prime * result + ((this.obstacle == null) ? 0 : this.obstacle.hashCode());
		result = prime * result + this.readers;
		result = prime * result + ((this.southernLocationType == null) ? 0 : this.southernLocationType.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		result = prime * result + (this.underExclusiveWriteLock ? 1231 : 1237);
		result = prime * result + (this.underSharedReadLock ? 1231 : 1237);
		result = prime * result + ((this.westernLocationType == null) ? 0 : this.westernLocationType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!Utils.equalsHelper(this, obj)){
			return false;
		}
		
		VacuumWorldLocation other = (VacuumWorldLocation) obj;
		
		if(other == null) {
			return false;
		}
		
		return checkFields(other);
	}

	private boolean checkFields(VacuumWorldLocation other) {
		if(!checkFieldsHelper(other)) {
			return false;
		}
		
		if(!VWUtils.checkObjectsEquality(this.type, other.type)) {
			return false;
		}
		
		if(!checkNeighbors(other)) {
			return false;
		}
		
		if(!checkLockstate(other)) {
			return false;
		}
		
		return true;
	}

	private boolean checkFieldsHelper(VacuumWorldLocation other) {
		if(!VWUtils.checkObjectsEquality(this.agent, other.agent)) {
			return false;
		}
		
		if(!VWUtils.checkObjectsEquality(this.user, other.user)) {
			return false;
		}
		
		if(!VWUtils.checkObjectsEquality(this.obstacle, other.obstacle)) {
			return false;
		}
		
		if(!VWUtils.checkObjectsEquality(this.coordinates, other.coordinates)) {
			return false;
		}
		
		return true;
	}

	private boolean checkLockstate(VacuumWorldLocation other) {
		return this.underSharedReadLock == other.underSharedReadLock &&
				this.underExclusiveWriteLock == other.underExclusiveWriteLock &&
				this.readers == other.readers;
	}

	private boolean checkNeighbors(VacuumWorldLocation other) {
		return VWUtils.checkObjectsEquality(this.northernLocationType, other.northernLocationType) &&
				VWUtils.checkObjectsEquality(this.southernLocationType, other.southernLocationType) &&
				VWUtils.checkObjectsEquality(this.westernLocationType, other.westernLocationType) &&
				VWUtils.checkObjectsEquality(this.easternLocationType, other.easternLocationType);
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