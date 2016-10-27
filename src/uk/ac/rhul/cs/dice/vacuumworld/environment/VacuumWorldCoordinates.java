package uk.ac.rhul.cs.dice.vacuumworld.environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Coordinates;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.locations.LocationKey;
import uk.ac.rhul.cs.dice.gawl.interfaces.utils.AbstractSingleTypedPair;
import uk.ac.rhul.cs.dice.gawl.interfaces.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Duplicable;

public class VacuumWorldCoordinates extends AbstractSingleTypedPair<Integer> implements Coordinates, Duplicable<VacuumWorldCoordinates> {
	public VacuumWorldCoordinates(int x, int y) {
		super(x, y);
	}
	
	public int getX() {
		return getFirst();
	}
	
	public int getY() {
		return getSecond();
	}
	
	@Override
	public String toString() {
		return "(" + getFirst() + ", " + getSecond() + ")";
	}
	
	public VacuumWorldCoordinates getNorthernCoordinates() {
		return new VacuumWorldCoordinates(getFirst(), getSecond() - 1);
	}
	
	public VacuumWorldCoordinates getSouthernCoordinates() {
		return new VacuumWorldCoordinates(getFirst(), getSecond() + 1);
	}
	
	public VacuumWorldCoordinates getWesternCoordinates() {
		return new VacuumWorldCoordinates(getFirst() - 1, getSecond());
	}
	
	public VacuumWorldCoordinates getEasternCoordinates() {
		return new VacuumWorldCoordinates(getFirst() + 1, getSecond());
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
	
	public List<VacuumWorldCoordinates> getAllNeighboursCoordinates() {
		List<VacuumWorldCoordinates> toReturn = new ArrayList<>();
		
		toReturn.addAll(Arrays.asList(getNorthernCoordinates(), getSouthernCoordinates(), getWesternCoordinates(), getEasternCoordinates()));
		
		return toReturn;
	}
	
	public VacuumWorldCoordinates getNewCoordinates(ActorFacingDirection agentDirection) {
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
		return getFirst() >= 0 && getSecond() >= 0 && getFirst() <= maxX && getSecond() <= maxY;
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
		if(getFirst() > other.getX() && getSecond() > other.getY()) {
			return 1;
		}
		else if(getFirst() < other.getX() && getSecond() < other.getY()) {
			return -1;
		}
		else if(this.equals(other)) {
			return 0;
		}
		else {
			return squaresComparison(getFirst(), getSecond(), other.getX(), other.getY());
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
	public VacuumWorldCoordinates duplicate() {
		return new VacuumWorldCoordinates(getFirst(), getSecond());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getFirst().intValue();
		result = prime * result + getSecond().intValue();
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!Utils.equalsHelper(this, obj)) {
			return false;
		}
		else {
			return checkValues(obj);
		}
	}

	private boolean checkValues(Object obj) {
		VacuumWorldCoordinates other = (VacuumWorldCoordinates) obj;
		
		if(other == null) {
			return false;
		}
		
		return getFirst().intValue() == other.getFirst().intValue() && getSecond().intValue() == other.getSecond().intValue();
	}
}