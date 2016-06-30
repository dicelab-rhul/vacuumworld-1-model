package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
	private Main(){}
	
	public static void main(String[] args) {
		startModelServer();
	}

	private static void startModelServer() {
		try {
			VacuumWorldServer server = new VacuumWorldServer(13337);
			server.startServer(true);
		} catch (IOException e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
		}
	}
}