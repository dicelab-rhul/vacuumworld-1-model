package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class VacuumWorldClientListener implements Runnable {
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private boolean canSend;
	private ViewRequestsEnum code;
	private ModelUpdate updateToSend;
	
	public VacuumWorldClientListener(ObjectInputStream input, ObjectOutputStream output) {
		this.input = input;
		this.output = output;
		this.canSend = false;
		this.code = null;
		this.updateToSend = null;
	}
	
	public ViewRequestsEnum getRequestCode() {
		return this.code;
	}
	
	public void setUpdateToSend(ModelUpdate update) {
		this.updateToSend = update;
	}
	
	public void unlock() {
		this.canSend = true;
	}

	@Override
	public void run() {
		while(true) {
			loop();
		}
	}

	private void loop() {
		try {
			Object request = this.input.readObject();
			
			if(request instanceof ViewRequest) {
				manageViewRequest((ViewRequest) request);
			}
		}
		catch (Exception e) {
			return;
		}
	}

	private void manageViewRequest(ViewRequest request) throws IOException {
		ViewRequestsEnum code = request.getCode();
		
		switch(code) {
		case GET_STATE:
		case STOP:
			manageViewRequest(code);
			break;
		default:
			//ignore the request
			break;	
		}
	}

	private void manageViewRequest(ViewRequestsEnum code) throws IOException {
		this.code = code;
		this.canSend = false;
		
		while(!this.canSend) {
			continue;
		}
		
		this.output.writeObject(this.updateToSend);
		this.output.flush();
	}
}