package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.Serializable;

public class ModelUpdate implements Serializable {
	private static final long serialVersionUID = 7897495143082366358L;
	private Serializable payload;
	
	public ModelUpdate(Serializable payload) {
		this.payload = payload;
	}

	public Object getPayload() {
		return this.payload;
	}
}