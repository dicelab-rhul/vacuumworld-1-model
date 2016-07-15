package uk.ac.rhul.cs.dice.vacuumworld.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class ExperimentConnector {

  private final String PATH = "path";
  private final String SIZES = "sizes";
  private final String AGENTS = "agents";
  private final String CONFIGFILE = "TestConfig.txt";

  private String path;
  private int[] sizes;
  private int[] agents;
  
  private GenerationSequence gen;

  public ExperimentConnector() throws ConfigFileException {
  }
  
  public void generateTestFiles() throws ConfigFileException {
    File config = new File(CONFIGFILE);
    if (!config.exists()) {
      throw new ConfigFileException("config file does not exist");
    }
    ArrayList<String> lines = new ArrayList<>();
    try {
      BufferedReader r = new BufferedReader(new FileReader(config));
      String line = null;
      while ((line = r.readLine()) != null) {
        lines.add(line);
      }
      r.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    Iterator<String> iter = lines.iterator();
    while (iter.hasNext()) {
      handleLine(iter.next());
    }

    File p = new File(path);
    if (!p.exists()) {
      if (!p.mkdir()) {
        throw new ConfigFileException("Cannot create dir: " + path);
      }
    }
    
    gen = new GenerationSequence(path+"/", sizes,
        agents);
    gen.generateAllTestCases();
  }

  private void handleLine(String line) throws ConfigFileException {
    String[] split = line.split(":");
    if (split.length != 2) {
      throw new ConfigFileException("invalid line structure: " + line);
    }
    String com = split[0].trim();
    if (PATH.equals(com)) {
      handlePath(split[1]);
    } else if (SIZES.equals(com)) {
      sizes = handleArray(split[1]);
    } else if (AGENTS.equals(com)) {
      agents = handleArray(split[1]);
    } else {
      throw new ConfigFileException("invalid property: " + com);
    }
  }

  private void handlePath(String line) {
    path = line.replaceAll("\\s+", "");
  }

  private int[] handleArray(String line) throws ConfigFileException {
    line = line.replaceAll("\\s+", "");
    String[] split = line.split(",");
    if (split.length < 1) {
      throw new ConfigFileException("invalid number of arguments");
    }
    int[] array = new int[split.length];
    for (int i = 0; i < split.length; i++) {
      array[i] = Integer.valueOf(split[i]);
    }
    return array;
  }

  public class ConfigFileException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConfigFileException(String message) {
      super(CONFIGFILE + " doesnt exist or is not in the correct format: "
          + message);
    }
  }
  
  public HashSet<File> getFilePaths() {
    return gen.getCompleteFilePaths();
  }
}
