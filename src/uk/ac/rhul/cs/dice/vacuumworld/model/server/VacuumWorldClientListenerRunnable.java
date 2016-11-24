package uk.ac.rhul.cs.dice.vacuumworld.model.server;

import java.io.EOFException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.StopSignal;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequest;

public class VacuumWorldClientListenerRunnable implements Runnable {
    private Client client;
    private Queue<ViewRequest> clientRequests;
    private Semaphore canProceed;
    private volatile StopSignal sharedStopSignal;
    
    public VacuumWorldClientListenerRunnable(StopSignal stopSignal, Client client) {
	this.client = client;
	this.clientRequests = new PriorityQueue<>();
	this.sharedStopSignal = stopSignal;
    }
    
    @Override
    public void run() {
	try {
	    runListener();
	}
	catch(InterruptedException e) {
	    Thread.currentThread().interrupt();
	}
    }
    
    private void runListener() throws InterruptedException {
	VWUtils.logWithClass(this.getClass().getSimpleName(), "View listening thread started.");
	boolean canContinue = true;

	while (canContinue) {
	    this.canProceed.acquire();
	    canContinue = loop();
	}
    }

    private boolean loop() {
	if (this.sharedStopSignal.mustStop()) {
	    return false;
	}
	else {
	    return waitForRequest();
	}
    }
    
    private boolean waitForRequest() {
	try {
	    Object request = this.client.getInput().readObject();

	    if (request instanceof ViewRequest) {
		manageViewRequest((ViewRequest) request);
	    }

	    return true;
	}
	catch (EOFException e) {
	    VWUtils.fakeLog(e);
	    
	    return false;
	}
	catch (Exception e) {
	    VWUtils.log(e);
	    
	    return false;
	}
    }

    private void manageViewRequest(ViewRequest request) {
	this.clientRequests.add(request);
	//TODO
    }

    public Client getClient() {
	return this.client;
    }
}