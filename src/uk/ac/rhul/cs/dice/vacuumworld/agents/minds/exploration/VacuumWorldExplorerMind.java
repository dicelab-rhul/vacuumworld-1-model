package uk.ac.rhul.cs.dice.vacuumworld.agents.minds.exploration;

import java.util.ArrayList;
import java.util.List;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.minds.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.VWPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;

public class VacuumWorldExplorerMind extends VacuumWorldDefaultMind {
    private ExplorationPlan plan;
    private boolean nFound;
    private int n;
    private boolean done;
    private List<VacuumWorldCoordinates> locationsToPerceiveYet;
    private int cornersFound;
    private boolean isStill;
    
    public VacuumWorldExplorerMind(String bodyId) {
	super(bodyId);
	
	this.plan = new ExplorationPlan();
	this.nFound = false;
	this.n = -1;
	this.done = false;
	this.locationsToPerceiveYet = new ArrayList<>();
	this.cornersFound = 0;
	this.isStill = true;
    }

    @Override
    public EnvironmentalAction decide(Object... parameters) {
	if(this.cornersFound == 4 && this.locationsToPerceiveYet.isEmpty()) {
	    this.done = true;
	}
	
	if(this.done) {
	    return manageSuccess();
	}
	else if(this.plan.isEmpty()) {
	    buildNewPlan();
	    
	    return buildNewAction(this.plan.retrieveActioToPerformPrototype());
	}
	else {
	    return buildNewAction(this.plan.retrieveActioToPerformPrototype());
	}
    }

    private EnvironmentalAction manageSuccess() {
	VWPerception perception = getPerception();
	
	if(perception == null) {
	    return buildPerceiveAction();
	}
	else {
	    updateAvailableActions(perception);
	    
	    return decideActionRandomly();
	}
    }

    private void buildNewPlan() {
	this.plan = new ExplorationPlan();
	VWPerception perception = getPerception();
	
	if(perception == null) {
	    this.plan.enqueueActionPrototype(PerceiveAction.class);
	}
	else {
	    perception.getCoordinatesInPerceptionList().stream().filter(this.locationsToPerceiveYet::contains).forEach(this.locationsToPerceiveYet::remove);
	    
	    if(this.cornersFound == 0) {
		this.cornersFound++;
	    }
	    
	    buildNewPlanFromPerception(perception);
	}
    }

    private void buildNewPlanFromPerception(VWPerception perception) {
	if(!this.nFound && !this.isStill) {
	    updateSizeAndCornersFlagIfNecessary(perception);
	}
	
	if(!this.nFound) {
	    buildPlanToFindN(perception);
	}
	else {
	    initLocationsToExplore();
	    buildPlanToContinueExploration(perception);
	}
    }

    private void initLocationsToExplore() {
	if(this.n == 3) {
	    return;
	}
	
	for(int x = 0; x < this.n; x++) {
	    for(int y = 3; y < this.n; y++) {
		this.locationsToPerceiveYet.add(new VacuumWorldCoordinates(x, y));
	    }
	}
    }

    private void buildPlanToContinueExploration(VWPerception perception) {
	if(this.cornersFound == 4) {
	    buildPlanForLeftovers();
	}
	else if(this.cornersFound == 2){
	    buildPlanToDiscoverThirdCorner(perception);
	}
	else if(this.cornersFound == 3){
	    buildPlanToDiscoverFourthCorner(perception);
	}
    }

    private void buildPlanToDiscoverThirdCorner(VWPerception perception) {
	ActorFacingDirection direction = perception.getActorCurrentFacingDirection();
	
	switch(direction){
	case EAST:
	    this.plan.enqueueActionPrototype(TurnRightAction.class);
	    this.plan.enqueueActionPrototype(MoveAction.class);
	    break;
	case SOUTH:
	    enqueueMoveIfNecessary(perception, direction);
	    break;
	default:
	    this.plan.enqueueActionPrototype(MoveAction.class);
	    break;
	}
	
    }
    
    private void buildPlanToDiscoverFourthCorner(VWPerception perception) {
	ActorFacingDirection direction = perception.getActorCurrentFacingDirection();
	
	switch(direction){
	case SOUTH:
	    this.plan.enqueueActionPrototype(TurnRightAction.class);
	    this.plan.enqueueActionPrototype(MoveAction.class);
	    break;
	case WEST:
	    enqueueMoveIfNecessary(perception, direction);
	    break;
	default:
	    this.plan.enqueueActionPrototype(MoveAction.class);
	    break;
	}
	
    }

    private void buildPlanForLeftovers() {
	this.plan.enqueueActionPrototype(TurnRightAction.class);
	
	for(int i = 0; i < this.n - 6; i++) {
	    this.plan.enqueueActionPrototype(MoveAction.class);
	}
	
	this.plan.enqueueActionPrototype(TurnRightAction.class);
	
	for(int i = 0; i < this.n - 6; i++) {
	    this.plan.enqueueActionPrototype(MoveAction.class);
	}
	
	buildPlanForExplorationAfterFirstLap();
    }

    private void buildPlanForExplorationAfterFirstLap() {
	//TODO
    }

    private void buildPlanToFindN(VWPerception perception) {
	ActorFacingDirection direction = perception.getActorCurrentFacingDirection();
	
	switch(direction) {
	case WEST:
	    this.plan.enqueueActionPrototype(TurnRightAction.class);
	    this.plan.enqueueActionPrototype(TurnRightAction.class);
	    break;
	case NORTH:
	    this.plan.enqueueActionPrototype(TurnRightAction.class);
	    break;
	case EAST:
	    enqueueMoveIfNecessary(perception, direction);
	    break;
	default:
	    this.plan.enqueueActionPrototype(MoveAction.class);
	    break;
	}
    }

    private void enqueueMoveIfNecessary(VWPerception perception, ActorFacingDirection direction) {
	VacuumWorldCoordinates tempTarget = perception.getActorCoordinates().getNewCoordinates(direction);
	VacuumWorldLocation frontLocation = perception.getSpecificPerceivedLocation(tempTarget);
	
	if(!VacuumWorldLocationType.WALL.equals(frontLocation.getNeighborLocationType(perception.getActorCurrentFacingDirection()))) {
	    this.plan.enqueueActionPrototype(MoveAction.class);
	    this.isStill = false;
	}
	else {
	    this.cornersFound++;
	    this.plan.enqueueActionPrototype(TurnRightAction.class);
	}
    }

    private void updateSizeAndCornersFlagIfNecessary(VWPerception perception) {
	VacuumWorldCoordinates tempTarget = perception.getActorCoordinates().getNewCoordinates(perception.getActorCurrentFacingDirection());
	VacuumWorldLocation frontLocation = perception.getSpecificPerceivedLocation(tempTarget);
	
	if(VacuumWorldLocationType.WALL.equals(frontLocation.getNeighborLocationType(perception.getActorCurrentFacingDirection()))) {
	    this.nFound = true;
	    this.n = tempTarget.getX() + 1;
	    this.cornersFound++;
	    
	    if(this.n == 3) {
		this.cornersFound = 4;
	    }
	}
    }

}
