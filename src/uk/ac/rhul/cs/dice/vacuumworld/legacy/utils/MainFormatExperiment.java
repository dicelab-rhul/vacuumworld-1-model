package uk.ac.rhul.cs.dice.vacuumworld.legacy.utils;

import java.io.File;

public class MainFormatExperiment {
	private MainFormatExperiment() {}
	
	public static void main(String[] args) {
		ExperimentOrderedOutputFormatter formatter = new ExperimentOrderedOutputFormatter();
		formatter.format(new File("logs/test/results.txt"), new File("logs/test/orderedResult.txt"));
	}
}