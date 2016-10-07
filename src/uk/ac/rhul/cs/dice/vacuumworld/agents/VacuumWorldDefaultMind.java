package uk.ac.rhul.cs.dice.vacuumworld.agents;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgentMind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public abstract class VacuumWorldDefaultMind extends AbstractAgentMind {
	private String bodyId;
	private int perceptionRange;
	private boolean canSeeBehind;
	private Result lastActionResult;

	private Set<Class<? extends AbstractAction>> actions;
	private List<Class<? extends EnvironmentalAction>> availableActions;

	private List<DefaultActionResult> lastCyclePerceptions;
	private EnvironmentalAction nextAction;

	public VacuumWorldDefaultMind() {
		this.setLastCyclePerceptions(new ArrayList<>());
	}

	@Override
	public final void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldDefaultBrain && arg instanceof List<?>) {
			manageBrainRequest((List<?>) arg);
		}
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		while (this.getLastCyclePerceptions().isEmpty()) {
			notifyObservers(null, VacuumWorldDefaultBrain.class);
		}
	}
	
	@Override
	public EnvironmentalAction decide(Object... parameters) {
		setAvailableActions(new ArrayList<>(this.getActions()));
		this.nextAction = null; //this is why all the subclasses need to implement the decide logic.
		
		return null; //this is why all the subclasses need to implement the decide logic.
	}
	
	@Override
	public void execute(EnvironmentalAction action) {
		this.getLastCyclePerceptions().clear();
		Utils.logWithClass(this.getClass().getSimpleName(), "Executing " + this.getNextAction().getClass().getSimpleName() + "...");
		notifyObservers(this.getNextAction(), VacuumWorldDefaultBrain.class);
	}
	
	private void manageBrainRequest(List<?> arg) {
		for (Object result : arg) {
			if (result instanceof DefaultActionResult) {
				this.lastCyclePerceptions.add((DefaultActionResult) result);
				manageNonReceivedSpeechResult(result);
			}
		}
	}

	private void manageNonReceivedSpeechResult(Object result) {
		if (!(result instanceof VacuumWorldSpeechActionResult)) {
			this.lastActionResult = (VacuumWorldActionResult) result;
		}
	}
	
	protected final EnvironmentalAction buildPhysicalAction(Class<? extends EnvironmentalAction> actionPrototype) {
		try {
			return actionPrototype.newInstance();
		}
		catch (Exception e) {
			Utils.log(e);
			return null;
		}
	}

	protected final SpeechAction buildSpeechAction(String senderId, List<String> recipientIds, VacuumWorldSpeechPayload payload) {
		try {
			Constructor<SpeechAction> constructor = SpeechAction.class.getConstructor(String.class, List.class, Payload.class);
			return constructor.newInstance(senderId, recipientIds, payload);
		}
		catch (Exception e) {
			Utils.log(e);
			return null;
		}
	}

	public void setAvailableActions(Set<Class<? extends AbstractAction>> actions) {
		this.setActions(actions);
	}

	public boolean lastActionSucceded() {
		return ActionResult.ACTION_DONE.equals(this.lastActionResult.getActionResult());
	}
	
	public String getBodyId() {
		return this.bodyId;
	}
	
	public void setBodyId(String id) {
		this.bodyId = id;
	}
	
	public void setCanSeeBehind(boolean canSeeBehind) {
		this.canSeeBehind = canSeeBehind;
	}

	public void setPerceptionRange(int preceptionRange) {
		this.perceptionRange = preceptionRange;
	}

	public List<Class<? extends EnvironmentalAction>> getAvailableActions() {
		return this.availableActions;
	}

	public void setAvailableActions(List<Class<? extends EnvironmentalAction>> availableActions) {
		this.availableActions = availableActions;
	}

	public int getPerceptionRange() {
		return this.perceptionRange;
	}

	public boolean isCanSeeBehind() {
		return this.canSeeBehind;
	}

	public EnvironmentalAction getNextAction() {
		return this.nextAction;
	}

	public void setNextAction(EnvironmentalAction nextAction) {
		this.nextAction = nextAction;
	}

	public List<DefaultActionResult> getLastCyclePerceptions() {
		return this.lastCyclePerceptions;
	}

	public void setLastCyclePerceptions(List<DefaultActionResult> lastCyclePerceptions) {
		this.lastCyclePerceptions = lastCyclePerceptions;
	}

	public Set<Class<? extends AbstractAction>> getActions() {
		return this.actions;
	}

	public void setActions(Set<Class<? extends AbstractAction>> actions) {
		this.actions = actions;
	}
}