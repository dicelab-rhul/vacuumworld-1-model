package uk.ac.rhul.cs.dice.vacuumworld.actions;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.CommunicationAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;

public class SpeechAction extends CommunicationAction<String, VacuumWorldPerception> {

	public SpeechAction(String senderId, List<String> recipientsIds, Payload<String> payload) {
		super(senderId, recipientsIds, payload);
	}
	
	@Override
	public boolean isPossible(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).isPossible(this, context);
	}
	
	@Override
	public boolean isNecessary(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).isNecessary(this, context);
	}
	
	@Override
	public Result<VacuumWorldPerception> perform(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).perform(this, context);
	}
	
	@Override
	public boolean succeeded(Physics<VacuumWorldPerception> physics, Space context) {
		return ((VacuumWorldPhysics) physics).succeeded(this, context);
	}
}