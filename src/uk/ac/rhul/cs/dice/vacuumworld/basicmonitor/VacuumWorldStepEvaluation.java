package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;

public class VacuumWorldStepEvaluation implements Evaluation {

	private int totalPhyicalActions = 0;
	private int failedCleans = 0;
	private int dirtsCleaned = 0;
	private int moves = 0;
	private int turns = 0;
	private int totalSpeechActions = 0;
	private int idle = 0;

	public String represent() {
		return "Total physical actions: " + totalPhyicalActions + ",\nTotal dirt cleaned: " + dirtsCleaned
				+ ",\nTotal moves: " + moves + ",\nTotal turns" + turns + ",\nTotal communication actions: "
				+ totalSpeechActions + ",\nTotal time idle: " + idle
				+ ",\nTotal cost defined as (Total actions + Total communication actions): " + this.getCost();
	}

	public void incDirtsCleaned() {
		this.dirtsCleaned++;
		this.totalPhyicalActions++;
	}

	public void incMoves() {
		this.moves++;
		this.totalPhyicalActions++;
	}

	public void incTurns() {
		this.turns++;
		this.totalPhyicalActions++;
	}

	public void incIdle() {
		this.idle++;
	}

	public void incTotalSpeechActions() {
		this.totalSpeechActions++;
	}

	public void incFailedCleans() {
		this.failedCleans++;
		this.idle++;
	}

	public int getTotalPhyicalActions() {
		return totalPhyicalActions;
	}

	public int getDirtsCleaned() {
		return dirtsCleaned;
	}

	public int getMoves() {
		return moves;
	}

	public int getTurns() {
		return turns;
	}

	public int getIdle() {
		return idle;
	}

	public int getCost() {
		return totalPhyicalActions + totalSpeechActions;
	}

	public int getTotalSpeechActions() {
		return totalSpeechActions;
	}

	public int getFailedCleans() {
		return failedCleans;
	}
}