package uk.ac.rhul.cs.dice.vacuumworld.legacy.generation;

public class LocationStructure {
	private int x;
	private int y;
	private AgentStructure agent;
	private String dirt; // the color of the dirt

	public LocationStructure(int x, int y, AgentStructure agent, String dirt) {
		this.x = x;
		this.y = y;
		this.agent = agent;
		this.dirt = dirt;
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

	public AgentStructure getAgent() {
		return this.agent;
	}

	public void setAgent(AgentStructure agent) {
		this.agent = agent;
	}

	public String getDirt() {
		return this.dirt;
	}

	public void setDirt(String dirt) {
		this.dirt = dirt;
	}
}