package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class Main {
	private Main() {}

	public static void main(String[] args) {
		startModelServer();
	}

	private static void startModelServer() {
		try {
			VacuumWorldServer server = new VacuumWorldServer(13337);
			server.startServer(true);
		}
		catch (IOException | ClassNotFoundException | HandshakeException e) {
			Utils.log(e);
		}
	}
}