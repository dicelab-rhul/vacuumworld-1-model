package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;

import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class Main {
  
  public static final String TEST = "test";
  public static final String GENERATEFILES = "-g";
  public static final String DEBUG = "debug";
  public static final String FOO = "foo";
  
	private Main() {}

	public static void main(String[] args) {
		if(args.length < 4) {
			Utils.logWithClass(Main.class.getSimpleName(), "Usage: java -jar model.jar --delay-in-seconds <delay-in-seconds> --config-file <config-json-file-path>");
		}
		else {
			ConfigData.initConfigData(args[3]);
			double delay = parseDelay(args[0]);
			startModelServer(delay);
		}
	}

	private static double parseDelay(String string) {
		try {
			return Double.valueOf(string);
		}
		catch(Exception e) {
			Utils.fakeLog(e);
			return 0;
		}
	}

	private static void startModelServer(double delayInSeconds) {
		try {
			VacuumWorldServer server = new VacuumWorldServer(13337);
			server.startServer(new String[]{FOO, FOO}, delayInSeconds);
		}
		catch (IOException | HandshakeException e) {
			Utils.log(e);
		}
	}
}