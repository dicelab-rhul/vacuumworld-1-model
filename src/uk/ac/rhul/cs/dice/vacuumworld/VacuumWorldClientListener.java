package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.StopSignal;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequest;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequestsEnum;

public class VacuumWorldClientListener implements Runnable {
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Semaphore canProceed;
	private AtomicReference<ViewRequestsEnum> code;
	
	private volatile StopSignal sharedStopSignal;
	
	public VacuumWorldClientListener(ObjectInputStream input, ObjectOutputStream output, Semaphore semaphore, StopSignal sharedStopSignal) {
		this.input = input;
		this.output = output;
		this.canProceed = semaphore;
		this.code = new AtomicReference<>(ViewRequestsEnum.GET_STATE);
		this.sharedStopSignal = sharedStopSignal;
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
		if(this.sharedStopSignal.mustStop()) {
			return false;
		}
		
		try {
			Object request = this.input.readObject();
			
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