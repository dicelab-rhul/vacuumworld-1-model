package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.view.ViewRequest;
import uk.ac.rhul.cs.dice.vacuumworld.view.ViewRequestsEnum;

public class VacuumWorldClientListener implements Runnable {
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Semaphore canProceed;
	//private boolean canProceed;
	private AtomicReference<ViewRequestsEnum> code;
	
	public VacuumWorldClientListener(ObjectInputStream input, ObjectOutputStream output, Semaphore semaphore) {
		this.input = input;
		this.output = output;
		this.canProceed = semaphore;
		//this.canProceed = false;
		this.code = new AtomicReference<>(ViewRequestsEnum.GET_STATE);
	}
	
	public ViewRequestsEnum getRequestCode() {
		System.out.println("NULL CODE????");
		if(this.code == null) {
			System.out.println("NULL CODE");
		}
		
		return this.code.get();
	}
	
	public ObjectOutputStream getOutputStream() {
		return this.output;
	}
	
	/*public void resetRequestCode() {
		this.code.set(null);
	}*/
	
	/*public void unlock() {
		//this.canProceed = true;
		this.canProceed.release();
		System.out.println("After unlock in listener");
	}*/

	@Override
	public void run() {
		try {
			runListener();
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void runListener() throws InterruptedException {
		Utils.println(this.getClass().getSimpleName(), "LISTENING THREAD STARTED!!!");
		
		while(true) {
			/*if(!this.canProceed) {
				continue;
			}*/
			
			Utils.println(this.getClass().getSimpleName(), "Waiting for permission from thread manager");
			this.canProceed.acquire();
			Utils.println(this.getClass().getSimpleName(), "Got permission from thread manager");
			Utils.println(this.getClass().getSimpleName(), "I can proceed: new loop");
			loop();
		}
	}

	private void loop() {
		try {
			Utils.println(this.getClass().getSimpleName(), "Waiting for view request.");
			Object request = this.input.readObject();
			Utils.println(this.getClass().getSimpleName(), "Got view request.");
			
			if(request instanceof ViewRequest) {
				manageViewRequest((ViewRequest) request);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private void manageViewRequest(ViewRequest request) {
		this.code.set(request.getCode());
		Utils.println(this.getClass().getSimpleName(), "after code assignment in listener: " + this.code);
		//this.canProceed = false;
	}
}