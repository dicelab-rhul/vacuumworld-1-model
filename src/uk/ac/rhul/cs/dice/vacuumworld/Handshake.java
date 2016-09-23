package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Handshake {
	private static final String ERROR = "Bad handshake.";
	private static final int TIME_TO_WAIT = 100000;
	
	private Handshake(){}
	
	public static Boolean attemptHanshakeWithController(ObjectOutputStream toController, ObjectInputStream fromController, HandshakeCodes code) throws HandshakeException, IOException {
		if(code != null) {
			return attemptHanshakeWithControllerHelper(toController, fromController, code); 
		}
		else {
			throw new HandshakeException(ERROR);
		}
	}
	
	public static Boolean attemptHanshakeWithControllerHelper(ObjectOutputStream toController, ObjectInputStream fromController, HandshakeCodes code) throws HandshakeException, IOException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		Future<Boolean> future = executor.submit(() -> doHandshake(toController, fromController, code));
		
		try {
			return future.get(TIME_TO_WAIT, TimeUnit.MILLISECONDS);
		}
		catch (Exception e) {
			throw new HandshakeException(e);
		}
	}

	private static Boolean doHandshake(ObjectOutputStream toController, ObjectInputStream fromController, HandshakeCodes code) throws HandshakeException, ClassNotFoundException, IOException {
		switch(code) {
		case CHCM:
			return continueHandshake(toController, fromController, code, HandshakeCodes.MHMC);
		case CHVM:
			return continueHandshake(toController, fromController, code, HandshakeCodes.MHMV);
		default:
			throw new HandshakeException(ERROR);
		}
	}

	private static Boolean continueHandshake(ObjectOutputStream toController, ObjectInputStream fromController, HandshakeCodes oldCode, HandshakeCodes code) throws IOException, HandshakeException, ClassNotFoundException {
		toController.writeObject(code.toString());
		toController.flush();
		System.out.println("sent " + code.toString() + " to controller"); //MHMC
		
		HandshakeCodes codeFromController = HandshakeCodes.fromString((String) fromController.readObject());
		System.out.println("received " + (codeFromController == null ? null : codeFromController.toString()) + " from controller");
		
		if(codeFromController != null) {
			return finalizeHandshake(toController, oldCode, codeFromController);
		}
		else {
			throw new HandshakeException(ERROR);
		}
	}

	private static Boolean finalizeHandshake(ObjectOutputStream toController, HandshakeCodes oldCode, HandshakeCodes codeFromController) throws HandshakeException, IOException {
		if(oldCode.equals(codeFromController)) {
			throw new HandshakeException(ERROR);
		}
		
		switch(codeFromController) {
		case CHCM:
			return finalizeHandshake(toController, HandshakeCodes.MHMC);
		case CHVM:
			return finalizeHandshake(toController, HandshakeCodes.MHMV);
		default:
			throw new HandshakeException(ERROR);
		}
	}

	private static Boolean finalizeHandshake(ObjectOutputStream toController, HandshakeCodes code) throws IOException {
		toController.writeObject(code.toString());
		toController.flush();
		System.out.println("sent " + code.toString() + " to controller");
		
		return true;
	}
}