package uk.ac.rhul.cs.dice.vacuumworld.legacy.generation;

public class AgentStructure {
	private String id;
	private String name;
	private String color;
	private int sensors;
	private int actuators;
	private int width;
	private int height;
	private String facingDirection;

	public AgentStructure(String id, String name, String color, String facingDirection, int... data) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.facingDirection = facingDirection;
		this.sensors = data[0];
		this.actuators = data[1];
		this.width = data[2];
		this.height = data[3];
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
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

	public String getFacingDirection() {
		return this.facingDirection;
	}

	public void setFacingDirection(String facingDirection) {
		this.facingDirection = facingDirection;
	}
}