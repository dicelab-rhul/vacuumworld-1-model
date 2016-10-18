package uk.ac.rhul.cs.dice.vacuumworld.legacy.generation;

import java.util.List;

public class Structure {
	private int width;
	private int height;
	private List<LocationStructure> notableLocations;

	public Structure(int width, int height, List<LocationStructure> notableLocations) {
		this.width = width;
		this.height = height;
		this.notableLocations = notableLocations;
	}

	public Structure() {
		super();
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<LocationStructure> getNotableLocations() {
		return this.notableLocations;
	}

	public void setNotableLocations(List<LocationStructure> notableLocations) {
		this.notableLocations = notableLocations;
	}
}