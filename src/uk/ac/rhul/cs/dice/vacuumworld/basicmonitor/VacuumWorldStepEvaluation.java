package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;

public class VacuumWorldStepEvaluation implements Evaluation {

  private String id;
  private int startCycle, endCycle, totalSteps = 0, dirtsCleaned = 0,
      moves = 0, turns = 0, speech = 0, idle = 0, cost = 0;

  public String represent() {
    return "Evaluation of: " + id + "from time: " + startCycle + " to "
        + endCycle + ",\n total actions: " + totalSteps
        + ",\ntotal dirt cleaned: " + dirtsCleaned + ",\ntotal moves: " + moves
        + ",\ntotal turns" + turns + ",\ntotal communication actions: "
        + speech + ",\ntotal time idle: " + idle;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public void setTotalSteps(int totalSteps) {
    this.totalSteps = totalSteps;
  }

  public int getDirtsCleaned() {
    return dirtsCleaned;
  }

  public void setDirtsCleaned(int dirtsCleaned) {
    this.dirtsCleaned = dirtsCleaned;
  }

  public int getMoves() {
    return moves;
  }

  public void setMoves(int moves) {
    this.moves = moves;
  }

  public int getTurns() {
    return turns;
  }

  public void setTurns(int turns) {
    this.turns = turns;
  }

  public int getIdle() {
    return idle;
  }

  public void setIdle(int idle) {
    this.idle = idle;
  }

  public int getStartCycle() {
    return startCycle;
  }

  public void setStartCycle(int startCycle) {
    this.startCycle = startCycle;
  }

  public int getEndCycle() {
    return endCycle;
  }

  public void setEndCycle(int endCycle) {
    this.endCycle = endCycle;
  }

  public int getCost() {
    return cost;
  }

  public void setCost(int cost) {
    this.cost = cost;
  }

  public int getSpeech() {
    return speech;
  }

  public void setSpeech(int speech) {
    this.speech = speech;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
