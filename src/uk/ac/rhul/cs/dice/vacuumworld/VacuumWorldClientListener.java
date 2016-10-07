package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequest;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequestsEnum;

public class VacuumWorldClientListener implements Runnable {
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Semaphore canProceed;
	private AtomicReference<ViewRequestsEnum> code;
	
	public VacuumWorldClientListener(ObjectInputStream input, ObjectOutputStream output, Semaphore semaphore) {
		this.input = input;
		this.output = output;
		this.canProceed = semaphore;
		this.code = new AtomicReference<>(ViewRequestsEnum.GET_STATE);
	}
	
	public ViewRequestsEnum getRequestCode() {
		return this.code.get();
	}
	
	public ObjectOutputStream getOutputStream() {
		return this.output;
	}

	@Override
	public void run() {
		try {
			runListener();
		}
		catch(InterruptedException e) {
			Utils.log(e);
			Thread.currentThread().interrupt();
		}
	}

	private void runListener() throws InterruptedException {
		Utils.logWithClass(this.getClass().getSimpleName(), "View listening thread started.");
		boolean canContinue = true;
		
		while(canContinue) {			
			this.canProceed.acquire();
			canContinue = loop();
		}
	}

	private boolean loop() {
		try {
			Utils.logWithClass(this.getClass().getSimpleName(), "Waiting for view request.");
			Object request = this.input.readObject();
			Utils.logWithClass(this.getClass().getSimpleName(), "Got view request.");
			
			if(request instanceof ViewRequest) {
				manageViewRequest((ViewRequest) request);
			}
			
			return true;
		}
		catch (Exception e) {
			Utils.log(e);
			return false;
		}
	}

	private void manageViewRequest(ViewRequest request) {
		this.code.set(request.getCode());
	}
}