package uk.ac.rhul.cs.dice.vacuumworld.generation;

public class AgentStructure {
  private String id;
  private String name;
  private String color;
  private int sensors;
  private int actuators;
  private int width;
  private int height;
  private String facing_direction;

  public AgentStructure(String id, String name, String color, int sensors,
      int actutators, int width, int height, String facing_direction) {
    super();
    this.id = id;
    this.name = name;
    this.color = color;
    this.sensors = sensors;
    this.actuators = actutators;
    this.width = width;
    this.height = height;
    this.facing_direction = facing_direction;
  }

  public AgentStructure() {
    super();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public int getSensors() {
    return sensors;
  }

  public void setSensors(int sensors) {
    this.sensors = sensors;
  }

  public int getActuators() {
    return actuators;
  }

  public void setActuators(int actuators) {
    this.actuators = actuators;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public String getFacing_direction() {
    return facing_direction;
  }

  public void setFacing_direction(String facing_direction) {
    this.facing_direction = facing_direction;
  }

}
