package uk.ac.rhul.cs.dice.vacuumworld.common;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;

public enum DirtType {
  GREEN, ORANGE, NEUTRAL;

  public static DirtType fromString(String type) {
    if (type == null) {
      return null;
    }
    switch (type) {
    case "green":
      return DirtType.GREEN;
    case "orange":
      return DirtType.ORANGE;
    default:
      return DirtType.NEUTRAL;
    }
  }

  public static boolean agentAndDirtCompatible(DirtType dtype,
      VacuumWorldAgentType atype) {
    if (atype.equals(VacuumWorldAgentType.GREEN)) {
      if (dtype.equals(DirtType.GREEN)) {
        return true;
      }
    }
    if (atype.equals(VacuumWorldAgentType.ORANGE)) {
      if (dtype.equals(DirtType.ORANGE)) {
        return true;
      }
    }
    if (atype.equals(VacuumWorldAgentType.NEUTRAL)) {
      return true;
    }
    if (dtype.equals(DirtType.NEUTRAL)) {
      return true;
    } else
      return false;
  }

  public String compactRepresentation() {
    switch (this) {
    case GREEN:
      return "g";
    case ORANGE:
      return "o";
    default:
      return "n";
    }
  }
}