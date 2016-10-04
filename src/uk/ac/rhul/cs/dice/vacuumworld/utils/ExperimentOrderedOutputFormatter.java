package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class is specifically for formatting experiment output in its original format
 * Specified in {@link VacuumWorldAgentThreadExperimentManager}. (This class
 * will become obsolete if the original format is changed.) The original format
 * is: Name cycle1 cycle2 cycle3 ... (the name of the experiment followed by the
 * time taken for each monitoring cycle all separated by a space)
 * 
 * @author Ben Wilkins
 *
 */
public class ExperimentOrderedOutputFormatter implements ExperimentOutputFormatter {
	private Map<String, List<String>> groups = new HashMap<>();
	private List<String> lines = new ArrayList<>();

	@Override
	public void format(File input, File output) {
		try(BufferedReader reader = new BufferedReader(new FileReader(input)); BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
			read(reader);
			
			for(List<String> value : this.groups.values()) {
				write(writer, value);
			}			
		}
		catch (IOException e) {
			Utils.log(e);
		}
	}

	private void read(BufferedReader reader) throws IOException {
		String line;
		
		while ((line = reader.readLine()) != null) {
			this.lines.add(line);
			String[] nameSplit = line.split(" ", 2);
			nameSplit[0] = nameSplit[0].replace("JSONTestCases\\", "");
			nameSplit[0] = nameSplit[0].replace(".json", "");
			nameSplit[0] = nameSplit[0].replace("\\", "-");

			String[] mapSplit = nameSplit[0].split("-");
			this.groups.putIfAbsent(mapSplit[0], new ArrayList<String>());
			this.groups.get(mapSplit[0]).add(nameSplit[0] + " " + nameSplit[1]);
		}
	}

	private void write(BufferedWriter writer, List<String> values) throws IOException {
		for (String s : values) {
			try {
				writer.write(s + "\n");
			}
			catch (IOException e) {
				Utils.log(e);
			}
		}
		
		writer.flush();
	}

	@Override
	public void format(String[] data, File output) {
		Utils.log("FORMAT METHOD NOT SUPPORTED BY: " + this.getClass());
	}
}