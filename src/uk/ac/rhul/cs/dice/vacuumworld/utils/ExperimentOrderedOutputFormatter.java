package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

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
public class ExperimentOrderedOutputFormatter implements
    ExperimentOutputFormatter {

  BufferedReader reader;
  BufferedWriter writer;
  HashMap<String, ArrayList<String>> groups = new HashMap<>();
  ArrayList<String> lines = new ArrayList<>();

  @Override
  public void format(File input, File output) {
    try {
      reader = new BufferedReader(new FileReader(input));
      String line = null;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
        String[] nameSplit = line.split(" ", 2);
        nameSplit[0] = nameSplit[0].replace("JSONTestCases\\", "");
        nameSplit[0] = nameSplit[0].replace(".json", "");
        nameSplit[0] = nameSplit[0].replace("\\", "-");

        String[] mapSplit = nameSplit[0].split("-");
        groups.putIfAbsent(mapSplit[0], new ArrayList<String>());
        groups.get(mapSplit[0]).add(nameSplit[0] + " " + nameSplit[1]);
      }
      writer = new BufferedWriter(new FileWriter(output));
      groups.forEach(new BiConsumer<String, ArrayList<String>>() {
        @Override
        public void accept(String arg0, ArrayList<String> arg1) {
          for (String s : arg1) {
            try {
              writer.write(s + "\n");
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      });
      writer.flush();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
        writer.close();
      } catch (IOException e) {
      }
    }
  }

  @Override
  public void format(String[] data, File output) {
    System.out.println("FORMAT METHOD NOT SUPPORTED BY: " + this.getClass());
  }
}
