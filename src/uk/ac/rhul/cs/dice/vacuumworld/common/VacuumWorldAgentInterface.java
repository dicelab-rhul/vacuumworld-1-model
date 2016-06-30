package uk.ac.rhul.cs.dice.vacuumworld.common;

public interface VacuumWorldAgentInterface {

  public int getPerceptionRange();

  public boolean canSeeBehind();
  
  public int getActionActuatorIndex();
  
  public int getActionResultSensorIndex();
}
