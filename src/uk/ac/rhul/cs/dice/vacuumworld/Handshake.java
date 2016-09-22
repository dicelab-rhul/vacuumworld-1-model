package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Handshake {
	private static final String ERROR = "Bad handshake.";
	private static final int TIME_TO_WAIT = 10000;
	
	private Handshake(){}
	
	public static Socket attemptHanshakeWithController(Socket controllerSocket, Object code) throws HandshakeException {
		if(code instanceof HandshakeCodes) {
			return attemptHanshakeWithController(controllerSocket, (HandshakeCodes) code); 
		}
		else {
			throw new HandshakeException(ERROR);
		}
	}
	
	public static Socket attemptHanshakeWithController(Socket controllerSocket, HandshakeCodes code) throws HandshakeException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Socket> future = executor.submit(() -> doHandshake(controllerSocket, new ObjectOutputStream(controllerSocket.getOutputStream()), new ObjectInputStream(controllerSocket.getInputStream()), code));
		
		try {
			return future.get(TIME_TO_WAIT, TimeUnit.MILLISECONDS);
		}
		catch (Exception e) {
			throw new HandshakeException(e);
		}
	}

	private static Socket doHandshake(Socket controllerSocket, ObjectOutputStream toController, ObjectInputStream fromController, HandshakeCodes code) throws HandshakeException, ClassNotFoundException, IOException {
		switch(code) {
		case CHCM:
			return continueHandshake(controllerSocket, toController, fromController, code, HandshakeCodes.MHMC);
		case CHVM:
			return continueHandshake(controllerSocket, toController, fromController, code, HandshakeCodes.MHMV);
		default:
			throw new HandshakeException(ERROR);
		}
	}

	private static Socket continueHandshake(Socket controllerSocket, ObjectOutputStream toController, ObjectInputStream fromController, HandshakeCodes oldCode, HandshakeCodes code) throws IOException, HandshakeException, ClassNotFoundException {
		toController.writeObject(code);
		toController.flush();
		
		Object codeFromController = fromController.readObject();
		
		if(codeFromController instanceof HandshakeCodes) {
			return finalizeHandshake(controllerSocket, toController, oldCode, (HandshakeCodes) codeFromController);
		}
		else {
			throw new HandshakeException(ERROR);
		}
	}

	private static Socket finalizeHandshake(Socket controllerSocket, ObjectOutputStream toController, HandshakeCodes oldCode, HandshakeCodes codeFromController) throws HandshakeException, IOException {
		if(oldCode.equals(codeFromController)) {
			throw new HandshakeException(ERROR);
		}
		
		switch(codeFromController) {
		case CHCM:
			return finalizeHandshake(controllerSocket, toController, HandshakeCodes.MHMC);
		case CHVM:
			return finalizeHandshake(controllerSocket, toController, HandshakeCodes.MHMV);
		default:
			throw new HandshakeException(ERROR);
		}
	}

	private static Socket finalizeHandshake(Socket controllerSocket, ObjectOutputStream toController, HandshakeCodes code) throws IOException {
		toController.writeObject(code);
		toController.flush();
		
		return controllerSocket;
	}
}