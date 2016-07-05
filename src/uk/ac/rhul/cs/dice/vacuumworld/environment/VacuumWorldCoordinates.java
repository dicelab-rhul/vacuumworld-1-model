package uk.ac.rhul.cs.dice.vacuumworld.environment;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Coordinates;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;

public class VacuumWorldCoordinates implements Coordinates, Cloneable {
	private int x;
	private int y;
	
	public VacuumWorldCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + ")";
	}
	
	public VacuumWorldCoordinates getNorthernCoordinates() {
		return new VacuumWorldCoordinates(this.x, this.y - 1);
	}
	
	public VacuumWorldCoordinates getSouthernCoordinates() {
		return new VacuumWorldCoordinates(this.x, this.y + 1);
	}
	
	public VacuumWorldCoordinates getWesternCoordinates() {
		return new VacuumWorldCoordinates(this.x - 1, this.y);
	}
	
	public VacuumWorldCoordinates getEasternCoordinates() {
		return new VacuumWorldCoordinates(this.x + 1, this.y);
	}
	
	public VacuumWorldCoordinates getNorthWesternCoordinates() {
		return getNorthernCoordinates().getWesternCoordinates();
	}
	
	public VacuumWorldCoordinates getNorthEasternCoordinates() {
		return getNorthernCoordinates().getEasternCoordinates();
	}
	
	public VacuumWorldCoordinates getSouthWesternCoordinates() {
		return getSouthernCoordinates().getWesternCoordinates();
	}
	
	public VacuumWorldCoordinates getSouthEasternCoordinates() {
		return getSouthernCoordinates().getEasternCoordinates();
	}
	
	public VacuumWorldCoordinates getNewCoordinates(AgentFacingDirection agentDirection) {
		switch(agentDirection) {
		case NORTH:
			return getNorthernCoordinates();
		case SOUTH:
			return getSouthernCoordinates();
		case WEST:
			return getWesternCoordinates();
		case EAST:
			return getEasternCoordinates();
		default:
			throw new IllegalArgumentException("Bad facing position: " + agentDirection);
		}
	}
	
	public boolean areInBounds(int maxX, int maxY) {
		return this.x >= 0 && this.y >= 0 && this.x <= maxX && this.y <= maxY;
	}
	
	@Override
	public int compareTo(LocationKey o) {
		if(o instanceof VacuumWorldCoordinates) {
			VacuumWorldCoordinates other = (VacuumWorldCoordinates) o;
			
			return compareTo(other);
		}
		else {
			throw new IllegalArgumentException("Cannot compare VacuumWorldCoordinates to " + o.getClass().getSimpleName());
		}
	}

	private int compareTo(VacuumWorldCoordinates other) {
		if(this.x > other.getX() && this.y > other.getY()) {
			return 1;
		}
		else if(this.x < other.getX() && this.y < other.getY()) {
			return -1;
		}
		else if(this.equals(other)) {
			return 0;
		}
		else {
			return squaresComparison(this.x, this.y, other.getX(), other.getY());
		}
	}

	private int squaresComparison(int x1, int y1, int x2, int y2) {
		if(((x1 ^ 2) + (y1 ^ 2)) > ((x2 ^ 2) + (y2 ^ 2))) {
			return 1;
		}
		else if(((x1 ^ 2) + (y1 ^ 2)) < ((x2 ^ 2) + (y2 ^ 2))) {
			return -1;
		}
		else {
			return breakTie(x1, x2);
		}
	}

	private int breakTie(int x1, int x2) {
		return x1 > x2 ? 1 : -1;
	}
	
	@Override
	public VacuumWorldCoordinates clone() {
	  return new VacuumWorldCoordinates(this.x, this.y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		VacuumWorldCoordinates other = (VacuumWorldCoordinates) obj;
		if (this.x != other.x)
			return false;
		if (this.y != other.y)
			return false;
		return true;
	}
}