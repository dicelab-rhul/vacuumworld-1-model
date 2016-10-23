package uk.ac.rhul.cs.dice.vacuumworld.actions;

import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class VacuumWorldSpeechPerceptionResultWrapper implements Result {
	private VacuumWorldSpeechActionResult speechResult;
	private VacuumWorldActionResult perceptionResult;

	public VacuumWorldSpeechPerceptionResultWrapper(VacuumWorldSpeechActionResult speechResult, VacuumWorldActionResult actionResult) {
		this.speechResult = speechResult;
		this.perceptionResult = actionResult;
	}

	@Override
	public ActionResult getActionResult() {
		return this.perceptionResult.getActionResult();
	}

	// Unused
	@Override
	public void changeActionResult(ActionResult newResult) {
		this.perceptionResult.changeActionResult(newResult);
	}

	@Override
	public Exception getFailureReason() {
		return this.perceptionResult.getFailureReason();
	}

	// Unused
	@Override
	public List<String> getRecipientsIds() {
		return this.perceptionResult.getRecipientsIds();
	}

	// Unused
	@Override
	public void setRecipientsIds(List<String> recipientsIds) {
		this.perceptionResult.setRecipientsIds(recipientsIds);
	}

	public VacuumWorldSpeechActionResult getSpeechResult() {
		return speechResult;
	}

	public void setSpeechResult(VacuumWorldSpeechActionResult speechResult) {
		this.speechResult = speechResult;
	}

	public VacuumWorldActionResult getPerceptionResult() {
		return perceptionResult;
	}

	public void setPerceptionResult(VacuumWorldActionResult perceptionResult) {
		this.perceptionResult = perceptionResult;
	}

	@Override
	public String getActorId() {
		return this.perceptionResult.getActorId();
	}

	@Override
	public VacuumWorldPerception getPerception() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPerception(Perception perception) {
		throw new UnsupportedOperationException();
	}
}