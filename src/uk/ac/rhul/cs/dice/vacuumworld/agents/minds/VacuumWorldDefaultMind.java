package uk.ac.rhul.cs.dice.vacuumworld.agents.minds;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.PhysicalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.speech.Payload;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Brain;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAbstractActorMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultBrain;
import uk.ac.rhul.cs.dice.vacuumworld.common.VWPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

/**
 * 
 * This class is the default abstract mind for all the cleaning agents.
 * 
 * @author cloudstrife9999, a.k.a. Emanuele Uliana
 *
 */
public abstract class VacuumWorldDefaultMind extends VacuumWorldAbstractActorMind {

    public VacuumWorldDefaultMind(String bodyId) {
	super(new Random(System.currentTimeMillis()), bodyId);
    }

    /**
     * This is part of the Observer pattern. This method is automatically called when an {@link Object} observed by this mind calls notifyObservers(...). 
     */
    @Override
    public final void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldDefaultBrain && arg instanceof List<?>) {
	    manageBrainRequest((List<?>) arg);
	}
    }

    /**
     * Asks the brain for the most recent {@link VWPerception} and received communications {@link List}.
     */
    @Override
    public void perceive(Object perceptionWrapper) {
	notifyObservers(null, VacuumWorldDefaultBrain.class);
	loadAvailableActionsForThisCycle(new ArrayList<>(getAvailableActionsForThisMind()));
    }

    /**
     * Executes the next action by notifying the {@link Brain}.
     */
    @Override
    public void execute(EnvironmentalAction action) {
	setLastActionResult(null);
	clearReceivedCommunications();

	VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
	notifyObservers(this.getNextAction(), VacuumWorldDefaultBrain.class);
    }

    private void manageBrainRequest(List<?> arg) {
	for (Object result : arg) {
	    if (result instanceof VacuumWorldActionResult) {
		setLastActionResult((VacuumWorldActionResult) result);
	    }
	    else if (result instanceof VacuumWorldSpeechActionResult) {
		addReceivedCommunicationToList((VacuumWorldSpeechActionResult) result);
	    }
	}
    }

    /**
     * Randomly decides the next action from the available actions for this cycle.
     */
    @Override
    public EnvironmentalAction decideActionRandomly() {
	Class<? extends EnvironmentalAction> actionPrototype = super.decideActionPrototypeRandomly();

	return buildNewAction(actionPrototype);
    }

    /**
     * 
     * Returns a new {@link EnvironmentalAction} from its prototype {@link Class}.
     * 
     * @param actionPrototype the prototype {@link Class} of the {@link EnvironmentalAction} to build.
     * @return a new {@link EnvironmentalAction}.
     * 
     */
    public EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
	if (actionPrototype.equals(SpeechAction.class)) {
	    return buildSpeechAction(getBodyId(), new ArrayList<>(), new VacuumWorldSpeechPayload("Hello everyone!!!", false));
	}
	else if (actionPrototype.equals(PerceiveAction.class)) {
	    return buildPerceiveAction();
	}
	else {
	    return buildPhysicalAction(actionPrototype);
	}
    }

    /**
     * 
     * Returns a new {@link PerceiveAction}
     * 
     * @return a new {@link PerceiveAction}.
     * 
     */
    public EnvironmentalAction buildPerceiveAction() {
	try {
	    return PerceiveAction.class.getConstructor(Integer.class, Boolean.class).newInstance(getPerceptionRange(), canSeeBehind());
	}
	catch (Exception e) {
	    VWUtils.fakeLog(e);

	    return new PerceiveAction();
	}
    }

    /**
     * 
     * Returns a new {@link PhysicalAction} from its prototype {@link Class}.
     * 
     * @param actionPrototype the prototype {@link Class} of the {@link EnvironmentalAction} to build.
     * @return a new {@link PhysicalAction}.
     * 
     */
    public EnvironmentalAction buildPhysicalAction(Class<? extends EnvironmentalAction> actionPrototype) {
	try {
	    return actionPrototype.newInstance();
	}
	catch (Exception e) {
	    VWUtils.log(e);

	    return null;
	}
    }

    /**
     * 
     * Returns a new {@link SpeechAction} from the sender id, the {@link List} of recipients ids and the {@link VacuumWorldSpeechPayload}.
     * 
     * @param senderId: the sender id.
     * @param recipientIds the {@link List} of recipients ids.
     * @param payload the {@link VacuumWorldSpeechPayload}.
     * @return a new {@link SpeechAction}.
     * 
     */
    public SpeechAction buildSpeechAction(String senderId, List<String> recipientIds, VacuumWorldSpeechPayload payload) {
	try {
	    Constructor<SpeechAction> constructor = SpeechAction.class.getConstructor(String.class, List.class, Payload.class);
	    return constructor.newInstance(senderId, recipientIds == null ? new ArrayList<>() : new ArrayList<>(recipientIds), payload);
	}
	catch (Exception e) {
	    VWUtils.log(e);

	    return null;
	}
    }

    /**
     * 
     * Updates the available actions for this cycle.
     * 
     * @param perception the most recent perception.
     * 
     */
    public void updateAvailableActions(VWPerception perception) {
	updateMoveActionIfNecessary(perception);
	updateCleaningActionIfNecessary(perception);
    }

    private void updateCleaningActionIfNecessary(VWPerception perception) {
	VacuumWorldCoordinates agentCoordinates = perception.getActorCoordinates();
	VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);

	if (!agentLocation.isDirtPresent()) {
	    removeActionIfNecessary(CleanAction.class);
	}
    }

    private void removeActionIfNecessary(Class<? extends EnvironmentalAction> name) {
	List<Class<? extends EnvironmentalAction>> toRemove = new ArrayList<>();

	for (Class<? extends EnvironmentalAction> a : this.getAvailableActionsForThisCycle()) {
	    if (a.isAssignableFrom(name)) {
		VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": removing " + name.getSimpleName() + " from my available actions for this cycle because it is clearly impossible...");
		toRemove.add(name);
	    }
	}

	this.getAvailableActionsForThisCycle().removeAll(toRemove);
    }

    private void updateMoveActionIfNecessary(VWPerception perception) {
	VacuumWorldCoordinates agentCoordinates = perception.getActorCoordinates();
	VacuumWorldLocation agentLocation = perception.getPerceivedMap().get(agentCoordinates);
	VacuumWorldCleaningAgent agent = agentLocation.getAgent();

	if (agent != null) {
	    ActorFacingDirection facingDirection = agent.getFacingDirection();

	    if (agentLocation.getNeighborLocationType(facingDirection) == VacuumWorldLocationType.WALL) {
		removeActionIfNecessary(MoveAction.class);
	    }
	}
    }

    /**
     * Returns the {@link VWPerception} from last cycle execution. It can be null.
     */
    @Override
    public VWPerception getPerception() {
	return (VWPerception) super.getPerception();
    }
}