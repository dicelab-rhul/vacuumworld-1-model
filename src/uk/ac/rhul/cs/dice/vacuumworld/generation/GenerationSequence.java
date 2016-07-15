package uk.ac.rhul.cs.dice.vacuumworld.generation;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class GenerationSequence {

  private int[] sizes;
  private int[] agents;
  
  private HashMap<Integer, String> directoryMap = new HashMap<>();
  private HashMap<Integer, String> fileNameMap = new HashMap<>();
  private HashSet<File> completeFilePaths = new HashSet<>();
  private String filePath;

  public GenerationSequence(String filePath, int[] sizes, int[] agents) {
    this.sizes = sizes;
    this.agents = agents;
    this.filePath = filePath;
    generateDirectories(filePath);
  }

  public void generateAllTestCases() {
    Generator gen = new Generator();
    for (int i = 0; i < sizes.length; i++) {
      for (int j = 0; j < agents.length; j++) {
        if (agents[j] < sizes[i] * sizes[i]) {
          // /generate a test case for this!
          generateTestCase(gen, filePath, agents[j], sizes[i]);
        }
      }
    }
  }

  private void generateTestCase(Generator gen, String filePath, int agents,
      int size) {
    File f = new File(filePath + getDirName(size) + getFileName(agents));
    completeFilePaths.add(f);
    gen.generate(f,
        size, size, agents, calculateNumDirts(size));
  }

  private void generateDirectories(String path) {
    System.out.println("Generating Dirs");
    for (int i = 0; i < sizes.length; i++) {
      File file = new File(path + generateDirName(sizes[i]));
      if (!file.exists()) {
        file.mkdir();
      }
    }
    for (int j = 0; j < agents.length; j++) {
      generateFileName(agents[j]);
    }
  }

  private String generateFileName(int agents) {
    String name = "A" + agents + ".json";
    fileNameMap.put(agents, name);
    return name;
  }

  private String getFileName(int agents) {
    return fileNameMap.get(agents);
  }

  private String getDirName(int size) {
    return directoryMap.get(size);
  }

  private String generateDirName(int size) {
    String dir = "D" + size + "x" + size + "/";
    directoryMap.put(size, dir);
    return dir;
  }

  private int calculateNumDirts(int size) {
    return (int) (Math.ceil(0.1 * size));
  }

  public HashSet<File> getCompleteFilePaths() {
    return completeFilePaths;
  }
}
