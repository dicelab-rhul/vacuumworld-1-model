package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VacuumWorldClientListener implements Runnable {
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	public VacuumWorldClientListener(ObjectInputStream input, ObjectOutputStream output) {
		this.input = input;
		this.output = output;
	}

	@Override
	public void run() {
		
	}
}