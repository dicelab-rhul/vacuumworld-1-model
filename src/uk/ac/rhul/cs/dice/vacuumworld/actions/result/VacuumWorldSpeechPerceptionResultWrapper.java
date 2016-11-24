package uk.ac.rhul.cs.dice.vacuumworld.actions.result;

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

    @Override
    public void changeActionResult(ActionResult newResult) {
	this.perceptionResult.changeActionResult(newResult);
    }

    @Override
    public Exception getFailureReason() {
	return this.perceptionResult.getFailureReason();
    }

    @Override
    public List<String> getRecipientsIds() {
	return this.perceptionResult.getRecipientsIds();
    }

    @Override
    public void setRecipientsIds(List<String> recipientsIds) {
	this.perceptionResult.setRecipientsIds(recipientsIds);
    }

    @Override
    public String getActorId() {
	return this.perceptionResult.getActorId();
    }

    @Override
    public VacuumWorldPerception getPerception() {
	return this.perceptionResult.getPerception();
    }

    @Override
    public void setPerception(Perception perception) {
	this.perceptionResult.setPerception(perception);
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
}