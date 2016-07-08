package uk.ac.rhul.cs.dice.vacuumworld.threading;

import util.Utils;

/**
 * Representation of what a real agent should look like
 * 
 * @author Ben Wilkins
 *
 */
public class GenericAgentMind {
	private int id;
	private String action;
	private String perception;

	public GenericAgentMind(int id) {
		this.id = id;
	}

	public void update() {
		Utils.log(this.id + ": update");
		perceive();
	}

	public void execute() {
		Utils.log(this.id + ": executing: " + this.action);
		// update the observers
		testWait(); // simulates the whole process of sending/receiving from the
					// environment
		update(); // the update method will be called by the brain
		Utils.log("..." + this.id + ": executed: " + this.action);
	}

	public void decide() {
		Utils.log(this.id + ": deciding...");
		testWait();
		this.action = "RANDOM ACTION"; // decide based on perception or other data
		Utils.log("..." + this.id + ": decided");
	}

	public void perceive() {
		Utils.log(this.id + ": perceiving: " + this.perception);
		testWait();
		Utils.log("..." + this.id + ": perceived: " + this.perception);
	}

	public int getId() {
		return id;
	}

	private void testWait() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}