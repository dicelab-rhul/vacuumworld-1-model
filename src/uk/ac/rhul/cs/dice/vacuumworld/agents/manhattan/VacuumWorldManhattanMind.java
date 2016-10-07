package uk.ac.rhul.cs.dice.vacuumworld.agents.manhattan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Pair;

public class VacuumWorldManhattanMind extends VacuumWorldDefaultMind {
	private ManhattanPlan plan;
	private static final String NAME = "MANHATTAN";
	
	public static final String getName() {
		return NAME;
	}
	
	@Override
	public EnvironmentalAction decide(Object... parameters) {
		super.decide(parameters);
		
		if(this.plan != null) {
			this.setNextAction(followPlan(parameters));
		}
		else {
			this.setNextAction(buildNewPlan());
		}
		
		return getNextAction();
	}

	private EnvironmentalAction buildNewPlan() {
		List<DefaultActionResult> lastCyclePerceptions = getLastCyclePerceptions();
		VacuumWorldActionResult lastPhysicalActionResult = getLastPhysicalActionResultIfExists(lastCyclePerceptions);
		
		if(lastPhysicalActionResult != null) {
			return buildNewPlan(lastPhysicalActionResult.getPerception());
		}
		else {
			return getPerceptionAction();
		}
	}

	private EnvironmentalAction buildNewPlan(VacuumWorldPerception perception) {
		if(perception == null) {
			return getPerceptionAction();
		}
		else {
			return buildNewPlanHelper(perception);
		}
	}

	private EnvironmentalAction buildNewPlanHelper(VacuumWorldPerception perception) {
		if(perception.canAgentClean()) {
			//no need to build a plan, just clean ASAP.
			return buildPhysicalAction(CleanAction.class);
		}
		else if(perception.canAgentSpotCompatibleDirt()) {
			return getCloserToDirt(perception);
		}
		else {
			//here it's impossible to build a new plan.
			return selectRandomAction();
		}
	}

	private EnvironmentalAction followPlan(Object... parameters) {
		if(!isPlanStillValid()) {
			this.plan = null;
			
			return buildNewPlan();
		}
		
		if(lastActionSucceded()) {
			this.plan.setLastAction(this.plan.pullActionToPerform());
			this.plan.setNumberOfConsecutiveFailuresOfTheSameAction(0);
			
			return buildPhysicalAction(this.plan.getLastAction());
		}
		else {
			this.plan.incrementNumberOfConsecutiveFailuresOfTheSameAction();
			
			return checkPlanFeasibility(parameters);
		}	
	}

	private boolean isPlanStillValid() {
		if(!this.plan.getTargetLocation().isDirtPresent()) {
			return false;
		}
		
		if(!this.plan.getTargetDirtType().equals(((DirtAppearance) this.plan.getTargetLocation().getDirt().getExternalAppearance()).getDirtType())) {
			return false;
		}
		
		if(!DirtType.agentAndDirtCompatible(this.plan.getTargetDirtType(), this.plan.getCurrentAgentType())) {
			return false;
		}
		
		return true;
	}

	private EnvironmentalAction checkPlanFeasibility(Object... parameters) {
		if(this.plan.getNumberOfConsecutiveFailuresOfTheSameAction() <= 10) {
			return buildPhysicalAction(this.plan.getLastAction());
		}
		else {
			this.plan = null;
			
			return decide(parameters);
		}
	}

	private VacuumWorldActionResult getLastPhysicalActionResultIfExists(List<DefaultActionResult> lastCyclePerceptions) {
		for(DefaultActionResult result : lastCyclePerceptions) {
			if(result instanceof VacuumWorldActionResult) {
				return (VacuumWorldActionResult) result;
			}
		}
		
		return null;
	}

	private EnvironmentalAction getCloserToDirt(VacuumWorldPerception perception) {
		List<VacuumWorldLocation> locationsWithCompatibleDirt = perception.getLocationsWithCompatibleDirt();
		VacuumWorldLocation closest = determineClosestLocationWithCompatibleDirt(locationsWithCompatibleDirt, perception.getAgentCoordinates(), perception.getAgentCurrentFacingDirection());
		
		return buildPlan(perception, closest, perception.getAgentCoordinates());
	}

	private EnvironmentalAction buildPlan(VacuumWorldPerception perception, VacuumWorldLocation closest, VacuumWorldCoordinates agentCoordinates) {
		int xDifference = closest.getCoordinates().getX() - agentCoordinates.getX();
		int yDifference = closest.getCoordinates().getY() - agentCoordinates.getY();
		AgentFacingDirection facingDirection = perception.getAgentCurrentFacingDirection();
		
		this.plan = new ManhattanPlan();
		this.plan.setCurrentAgentType(perception.getAgentType());
		this.plan.setTargetDirtType(((DirtAppearance) closest.getDirt().getExternalAppearance()).getDirtType());
		this.plan.setTargetLocation(closest);
		
		return buildPlan(xDifference, yDifference, facingDirection);
	}

	private EnvironmentalAction buildPlan(int xDifference, int yDifference, AgentFacingDirection facingDirection) {
		String planCode = this.plan.getPlanCodes().getPlanCodes().get(new Pair<>(Integer.signum(xDifference), Integer.signum(yDifference))).get(facingDirection);
		
		return fillPlan(planCode, xDifference, yDifference, facingDirection);
	}
	
	private EnvironmentalAction fillPlan(String planCode, int xDifference, int yDifference, AgentFacingDirection facingDirection) {
		for(char character : planCode.toCharArray()) {
			addActionsToPlan(character, xDifference, yDifference, facingDirection);
		}
		
		this.plan.pushActionToPerform(CleanAction.class);
		this.plan.setLastAction(this.plan.pullActionToPerform());
		
		return buildPhysicalAction(this.plan.getLastAction());
	}

	private void addActionsToPlan(char character, int xDifference, int yDifference, AgentFacingDirection facingDirection) {
		switch(character) {
		case 'L':
			this.plan.pushActionToPerform(TurnLeftAction.class);
			break;
		case 'R':
			this.plan.pushActionToPerform(TurnRightAction.class);
			break;
		case 'M':
			pushAllNecessaryMoveActions(getAllNecessaryMoveActions(xDifference, yDifference, facingDirection));
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void pushAllNecessaryMoveActions(Collection<? extends Class<? extends EnvironmentalAction>> allNecessaryMoveActions) {
		for(Class<? extends EnvironmentalAction> action : allNecessaryMoveActions) {
			this.plan.pushActionToPerform(action);
		}
	}

	private Collection<? extends Class<? extends EnvironmentalAction>> getAllNecessaryMoveActions(int xDifference, int yDifference, AgentFacingDirection facingDirection) {
		switch(facingDirection) {
			case NORTH:
			case SOUTH:
				return addMoveActions(yDifference);
			case WEST:
			case EAST:
				return addMoveActions(xDifference);
			default:
				throw new IllegalArgumentException();
		}
	}

	private Collection<? extends Class<? extends EnvironmentalAction>> addMoveActions(int difference) {
		List<Class<? extends EnvironmentalAction>> actions = new ArrayList<>();
		
		for(int i=0; i < Math.abs(difference); i++) {
			actions.add(MoveAction.class);
		}
		
		return actions;
	}

	private VacuumWorldLocation determineClosestLocationWithCompatibleDirt(List<VacuumWorldLocation> locationsWithCompatibleDirt, VacuumWorldCoordinates agentCoordinates, AgentFacingDirection agentFacingDirection) {
		VacuumWorldLocation closest = null;
		Integer currentOptimalDistance = Integer.MAX_VALUE;
		
		for(VacuumWorldLocation location : locationsWithCompatibleDirt) {
			int distance = getDistance(agentCoordinates, location.getCoordinates()) + getNumberOfTurnActionsNeeded(agentCoordinates, location.getCoordinates(), agentFacingDirection);
			
			if(distance < currentOptimalDistance) {
				closest = location;
				currentOptimalDistance = distance;
			}
		}
		
		return closest;
	}

	private int getNumberOfTurnActionsNeeded(VacuumWorldCoordinates agentCoordinates, VacuumWorldCoordinates targetCoordinates, AgentFacingDirection agentFacingDirection) {
		int xDifference = targetCoordinates.getX() - agentCoordinates.getX();
		int yDifference = targetCoordinates.getY() - agentCoordinates.getY();
		
		PlanCodes codes = PlanCodes.getInstance();
		Map<Pair<Integer>, Map<AgentFacingDirection, String>> planCodes = codes.getPlanCodes();
		Pair<Integer> key = new Pair<>(Integer.signum(xDifference), Integer.signum(yDifference));
		Map<AgentFacingDirection, String> m = planCodes.get(key);
		String planCode = m.get(agentFacingDirection);
		
		return (int) planCode.chars().filter(character -> character == 'L' || character == 'R').count();
	}

	private int getDistance(VacuumWorldCoordinates c1, VacuumWorldCoordinates c2) {
		return Math.abs(c1.getX() - c2.getX()) + Math.abs(c1.getY() - c2.getY());
	}

	private EnvironmentalAction selectRandomAction() {
		Random rng = new Random();
		
		int size = this.getAvailableActions().size();
		int randomNumber = rng.nextInt(size);
		Class<? extends EnvironmentalAction> actionPrototype = this.getAvailableActions().get(randomNumber);

		if(SpeechAction.class.isAssignableFrom(actionPrototype)) {
			return buildSpeechAction(getBodyId(), null, new VacuumWorldSpeechPayload("Hello!!!"));
		}
		else {
			return buildPhysicalAction(actionPrototype);
		}
	}

	private EnvironmentalAction getPerceptionAction() {
		for(Class<? extends EnvironmentalAction> candidate : getAvailableActions()) {
			if(PerceiveAction.class.isAssignableFrom(candidate)) {
				return buildPhysicalAction(candidate);
			}
		}
		
		throw new IllegalArgumentException();
	}
}