package uk.ac.rhul.cs.dice.vacuumworld;

import uk.ac.rhul.cs.dice.vacuumworld.model.server.VacuumWorldModelServer;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class Main {

    private Main() {}

    public static void main(String[] args) {
	if (args.length < 4) {
	    VWUtils.logWithClass(Main.class.getSimpleName(), "Usage: java -jar model.jar --delay-in-seconds <delay-in-seconds> --config-file <config-json-file-path>");
	    VWUtils.logWithClass(Main.class.getSimpleName(), "Usage (for evaluating): java -jar model.jar --delay-in-seconds <delay-in-seconds> --config-file <config-json-file-path> --initial-state-file <initial-state-file> --max-cycles-number <max-cycles-number>");
	}
	else {
	    double delay = parseDelay(args);
	    String configFilePath = retrieveConfigFilePath(args);
	    String initialStateFilePath = retrieveInitialstateFilePath(args);
	    int maxCyclesNumber = retrieveMaxCyclesNumber(args);
	    tryToStart(configFilePath, delay, initialStateFilePath, maxCyclesNumber);
	    VWUtils.logWithClass(Main.class.getSimpleName(), "Bye!!!");

	    System.exit(0); // this is to terminate threads which could not be stopped in a clean way.
	}
    }

    private static int retrieveMaxCyclesNumber(String[] args) {
	try {
	    if(args.length < 8) {
		return -1;
	    }
	    else if("--max-cycles-number".equals(args[6])) {
		return padCyclesNumber(args[7]);
	    }
		
	    return -1;
	}
	catch(NumberFormatException e) {
	    VWUtils.fakeLog(e);
	    
	    return -1;
	}
	
    }

    private static int padCyclesNumber(String number) {
	int candidate = Integer.parseInt(number);
	
	if(candidate <= 7) {
	    return 8;
	}
	
	while(candidate % 8 != 0) {
	    candidate++;
	}
	
	return candidate;
    }

    private static String retrieveInitialstateFilePath(String[] args) {
	if(args.length < 6) {
	    return null;
	}
	else if("--initial-state-file".equals(args[4])) {
	    return args[5];
	}
	
	return null;
    }

    private static void tryToStart(String configFilePath, double delay, String initialStateFilePath, int maxCyclesNumber) {
	if (!ConfigData.initConfigData(configFilePath)) {
	    VWUtils.logWithClass(Main.class.getSimpleName(), "Error in parsing config file!!!");
	}
	else if(initialStateFilePath == null || maxCyclesNumber == -1){
	    startModelServer(delay);
	}
	else {
	    startModelServerFromFile(initialStateFilePath, maxCyclesNumber, delay);
	}
    }

    private static void startModelServerFromFile(String initialStateFilePath, int maxCyclesNumber, double delay) {
	VacuumWorldModelServer server = new VacuumWorldModelServer(maxCyclesNumber);
	server.startServer(initialStateFilePath, delay);
    }

    private static String retrieveConfigFilePath(String[] args) {
	if ("--config-file".equals(args[2])) {
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
	catch (Exception e) {
	    VWUtils.fakeLog(e);
	    return 0;
	}
    }

    private static void startModelServer(double delayInSeconds) {
	try {
	    VacuumWorldServer server = new VacuumWorldServer();
	    server.startServer(delayInSeconds);
	}
	catch (Exception e) {
	    VWUtils.log(e);
	}
    }
}