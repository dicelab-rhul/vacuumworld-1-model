package uk.ac.rhul.cs.dice.vacuumworld.utils;

import java.io.File;

public interface ExperimentOutputFormatter {

  public void format(File input, File output);
  public void format(String[] data, File output);
  
}
