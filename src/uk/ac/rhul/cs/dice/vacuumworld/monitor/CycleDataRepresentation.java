package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;

public class CycleDataRepresentation {
  private int x;
  private int y;
  private AgentFacingDirection dir;

  public CycleDataRepresentation(int x, int y, AgentFacingDirection dir) {
    super();
    this.x = x;
    this.y = y;
    this.dir = dir;
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

  public AgentFacingDirection getDir() {
    return dir;
  }

  public void setDir(AgentFacingDirection dir) {
    this.dir = dir;
  }

}
