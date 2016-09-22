package uk.ac.rhul.cs.dice.vacuumworld.generation;

public class LocationStructure {
  private int x;
  private int y;
  private AgentStructure agent;
  private String dirt; // the color of the dirt

  public LocationStructure(int x, int y, AgentStructure agent, String dirt) {
    super();
    this.x = x;
    this.y = y;
    this.agent = agent;
    this.dirt = dirt;
  }

  public LocationStructure() {
    super();
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public AgentStructure getAgent() {
    return agent;
  }

  public void setAgent(AgentStructure agent) {
    this.agent = agent;
  }

  public String getDirt() {
    return dirt;
  }

  public void setDirt(String dirt) {
    this.dirt = dirt;
  }

}
