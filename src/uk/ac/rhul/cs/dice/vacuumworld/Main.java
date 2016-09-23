package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class Main {
  
  public static final String TEST = "test";
  public static final String GENERATEFILES = "-g";
  //to debug with a file args should be {DEBUG, EXAMPLEFILE}
  public static final String DEBUG = "debug";
  public static final String FOO = "foo";
  private static final String EXAMPLEFILE = "state_example2.json";
  
	private Main() {}

	public static void main(String[] args) {
		startModelServer();
	}

	private static void startModelServer() {
		try {
			VacuumWorldServer server = new VacuumWorldServer(13337);
			//server.startServer(new String[]{DEBUG, EXAMPLEFILE});
			server.startServer(new String[]{FOO, FOO});
		}
		catch (IOException | ClassNotFoundException | HandshakeException e) {
			Utils.log(e);
		}
	}
}