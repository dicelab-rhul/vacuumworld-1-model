package uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.json.JsonObject;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAbstractActorMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateActionsAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.DatabaseUpdateStatesAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions.VacuumWorldMonitoringPerception;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.utils.parser.StateActionsRepresentationBuilder;
import uk.ac.rhul.cs.dice.vacuumworld.utils.parser.StateRepresentationBuilder;

public class VacuumWorldMonitoringAgentMind extends VacuumWorldAbstractActorMind {
    private int cycleCounter;
    private boolean updateStatesCollection;
    private boolean updateActionsCollection;

    private List<JsonObject> states;
    private List<JsonObject> actionReports;

    public VacuumWorldMonitoringAgentMind(String bodyId, JsonObject initialStateRepresentation) {
	super(bodyId);

	setPerceptionRange(Integer.MAX_VALUE);
	setCanSeeBehind(true);
	init(initialStateRepresentation);
    }

    private void init(JsonObject initialStateRepresentation) {
	this.cycleCounter = 0;
	this.updateStatesCollection = false;
	this.updateActionsCollection = false;
	this.states = new ArrayList<>();
	this.states.add(initialStateRepresentation);
	this.actionReports = new ArrayList<>();
    }

    @Override
    public void perceive(Object perceptionWrapper) {
	notifyObservers(null, VacuumWorldMonitoringAgentBrain.class);
	storeStatesHistoryInMemory();
	storeActionsHistoryInMemory();
	loadAvailableActionsForThisCycle(new ArrayList<>(getAvailableActionsForThisMind()));
    }

    private void storeStatesHistoryInMemory() {
	if (this.updateStatesCollection) {
	    this.states.clear();
	}

	if (this.cycleCounter > 0) {
	    this.states.add(StateRepresentationBuilder.buildCompactStateRepresentation(getPerception() == null ? new HashMap<>() : getPerception().getPerceivedMap(), this.cycleCounter));
	}
    }

    @Override
    public EnvironmentalAction decide(Object... parameters) {
	updateMonitoringVariables();

	return buildSystemMonitoringAction();
    }

    private void storeActionsHistoryInMemory() {
	if (this.updateActionsCollection) {
	    this.actionReports.clear();
	}

	if (this.cycleCounter > 0) {
	    this.actionReports.add(StateActionsRepresentationBuilder.buildStateActionsRepresentation(getLastActionResult().getCycleReports(), this.cycleCounter));
	}
    }

    private EnvironmentalAction buildSystemMonitoringAction() {
	if (this.updateStatesCollection) {
	    return buildNewAction(DatabaseUpdateStatesAction.class);
	}
	else if (this.updateActionsCollection) {
	    return buildNewAction(DatabaseUpdateActionsAction.class);
	}
	else {
	    return buildNewAction(TotalPerceptionAction.class);
	}
    }

    @Override
    public VacuumWorldMonitoringActionResult getLastActionResult() {
	return (VacuumWorldMonitoringActionResult) super.getLastActionResult();
    }

    private void updateMonitoringVariables() {
	this.cycleCounter++;
	this.updateStatesCollection = this.cycleCounter % 5 == 0;
	this.updateActionsCollection = this.cycleCounter % 5 == 1 && this.cycleCounter != 1;
    }

    @Override
    public void execute(EnvironmentalAction action) {
	setLastActionResult(null);
	clearReceivedCommunications();

	VWUtils.logWithClass(this.getClass().getSimpleName(), VWUtils.ACTOR + getBodyId() + ": executing " + this.getNextAction().getClass().getSimpleName() + "...");
	notifyObservers(this.getNextAction(), VacuumWorldMonitoringAgentBrain.class);
    }

    private EnvironmentalAction buildNewAction(Class<? extends EnvironmentalAction> actionPrototype) {
	if (TotalPerceptionAction.class.isAssignableFrom(actionPrototype)) {
	    return buildPerceiveAction();
	}
	else if (DatabaseUpdateStatesAction.class.isAssignableFrom(actionPrototype)) {
	    return buildDatabaseAction(DatabaseUpdateStatesAction.class, List.class, this.states);
	}
	else if (DatabaseUpdateActionsAction.class.isAssignableFrom(actionPrototype)) {
	    return buildDatabaseAction(DatabaseUpdateActionsAction.class, List.class, this.actionReports);
	}
	else {
	    throw new UnsupportedOperationException();
	}
    }

    private EnvironmentalAction buildDatabaseAction(Class<? extends DatabaseAction> actionPrototype, Class<?> payloadType, Object payload) {
	try {
	    return actionPrototype.getConstructor(payloadType).newInstance(payload);
	} catch (Exception e) {
	    throw new UnsupportedOperationException(e);
	}
    }

    private EnvironmentalAction buildPerceiveAction() {
	try {
	    return TotalPerceptionAction.class.newInstance();
	}
	catch (Exception e) {
	    VWUtils.fakeLog(e);

	    return new TotalPerceptionAction();
	}
    }

    @Override
    public EnvironmentalAction decideActionRandomly() {
	return buildPerceiveAction();
    }

    @Override
    public void update(CustomObservable o, Object arg) {
	if (o instanceof VacuumWorldMonitoringAgentBrain && arg instanceof List<?>) {
	    ((List<?>) arg).stream().filter(result -> result instanceof VacuumWorldMonitoringActionResult).forEach(result -> setLastActionResult((VacuumWorldMonitoringActionResult) result));
	}
    }

    @Override
    public VacuumWorldMonitoringPerception getPerception() {
	return (VacuumWorldMonitoringPerception) super.getPerception();
    }
}