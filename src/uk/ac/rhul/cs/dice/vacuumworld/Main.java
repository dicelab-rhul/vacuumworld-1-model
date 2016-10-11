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
			double delay = parseDelay(args);
			String configFilePath = retrieveConfigFilePath(args);
			ConfigData.initConfigData(configFilePath);
			startModelServer(delay);
			
			Utils.logWithClass(Main.class.getSimpleName(), "Bye!!!");
			
			System.exit(0);
		}
	}

	private static String retrieveConfigFilePath(String[] args) {
		if("--config-file".equals(args[2])) {
			return args[3];
		}
		else {
			return null;
		}
	}

	private static double parseDelay(String[] args) {
		try {
			return Double.valueOf(args[1]);
		}
		catch(Exception e) {
			Utils.fakeLog(e);
			return 0;
		}
	}

	private static void startModelServer(double delayInSeconds) {
		try {
			VacuumWorldServer server = new VacuumWorldServer();
			server.startServer(new String[]{FOO, FOO}, delayInSeconds);
		}
		catch (IOException | HandshakeException e) {
			Utils.log(e);
		}
	}
}