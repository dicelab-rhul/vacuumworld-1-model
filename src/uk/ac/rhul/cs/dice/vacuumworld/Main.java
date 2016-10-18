package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;

import uk.ac.rhul.cs.dice.vacuumworld.legacy.utils.LegacyUtils;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.HandshakeException;

public class Main {
  
	private Main() {}

	public static void main(String[] args) {
		if(args.length < 4) {
			VWUtils.logWithClass(Main.class.getSimpleName(), "Usage: java -jar model.jar --delay-in-seconds <delay-in-seconds> --config-file <config-json-file-path>");
		}
		else {
			double delay = parseDelay(args);
			String configFilePath = retrieveConfigFilePath(args);
			tryToStart(configFilePath, delay);
			VWUtils.logWithClass(Main.class.getSimpleName(), "Bye!!!");
			
			System.exit(0);
		}
	}

	private static void tryToStart(String configFilePath, double delay) {
		if(!ConfigData.initConfigData(configFilePath)) {
			VWUtils.logWithClass(Main.class.getSimpleName(), "Error in parsing config file!!!");
		}
		else {
			startModelServer(delay);
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
			VWUtils.fakeLog(e);
			return 0;
		}
	}

	private static void startModelServer(double delayInSeconds) {
		try {
			VacuumWorldServer server = new VacuumWorldServer();
			server.startServer(new String[]{LegacyUtils.FOO, LegacyUtils.FOO}, delayInSeconds);
		}
		catch (IOException | HandshakeException e) {
			VWUtils.log(e);
		}
	}
}