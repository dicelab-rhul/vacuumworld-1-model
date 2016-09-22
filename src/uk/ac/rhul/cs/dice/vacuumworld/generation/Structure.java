package uk.ac.rhul.cs.dice.vacuumworld.generation;

import java.util.ArrayList;

public class Structure {
  private int width;
  private int height;
  private ArrayList<LocationStructure> notable_locations;

  public Structure(int width, int height,
      ArrayList<LocationStructure> notable_locations) {
    super();
    this.width = width;
    this.height = height;
    this.notable_locations = notable_locations;
  }

  public Structure() {
    super();
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public ArrayList<LocationStructure> getNotable_locations() {
    return notable_locations;
  }

  public void setNotable_locations(
      ArrayList<LocationStructure> notable_locations) {
    this.notable_locations = notable_locations;
  }

}
