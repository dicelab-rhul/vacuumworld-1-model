package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.Serializable;

public class ViewRequest implements Serializable {
	private static final long serialVersionUID = -6098689427679688772L;
	private ViewRequestsEnum code;
	private Serializable payload;
	
	public ViewRequest(ViewRequestsEnum code, Serializable payload) {
		this.code = code;
		this.payload = payload;
	}

	public ViewRequestsEnum getCode() {
		return this.code;
	}

	public Object getPayload() {
		return this.payload;
	}
}