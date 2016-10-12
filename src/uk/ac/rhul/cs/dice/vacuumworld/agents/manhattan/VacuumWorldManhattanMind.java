package uk.ac.rhul.cs.dice.vacuumworld.agents.manhattan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.common.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Pair;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldManhattanMind extends VacuumWorldDefaultMind {
	private ManhattanPlan plan;
	private static final String NAME = "MANHATTAN";
	
	public static final String getName() {
		return NAME;
	}
	
	@Override
	public EnvironmentalAction decide(Object... parameters) {
		if(this.plan != null) {
			return followPlan(parameters);
		}
		else {
			return buildNewPlan();
		}
	}

	private EnvironmentalAction buildNewPlan() {
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": new plan generation: seeking for a target...");
		VacuumWorldPerception perception = getPerception();
		
		if(perception == null) {
			Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": perception is null: the only reasonable plan is to get a new one...");
			
			return buildPerceiveAction();
		}
		else {
			updateAvailableActions(perception);
			
			return buildNewPlanHelper(perception);
		}
	}

	private EnvironmentalAction buildNewPlanHelper(VacuumWorldPerception perception) {
		if(perception.canAgentClean()) {
			//no need to build a plan, just clean ASAP.
			Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + " is on a location with compatible dirt. The only reasonable plan is to clean now.");
			
			return buildPhysicalAction(CleanAction.class);
		}
		else if(perception.canAgentSpotCompatibleDirt()) {
			return getCloserToDirt(perception);
		}
		else {
			//here it's impossible to build a new plan.
			Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + " cannot spot any compatible dirt. The next action will be a random one.");
			
			return decideActionRandomly();
		}
	}

	private EnvironmentalAction followPlan(Object... parameters) {
		if(!isPlanStillValid()) {
			Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": plan is no more valid. Trying to build a new one...");
			this.plan = null;
			
			return buildNewPlan();
		}
		
		if(lastActionSucceded()) {			
			this.plan.setLastAction(this.plan.pullActionToPerform(getBodyId()));
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
			Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": too many failures! Trying to build a new plan...");
			
			return decide(parameters);
		}
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
		
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": building Manhattan distance driven plan...");
		
		this.plan = new ManhattanPlan();
		this.plan.setCurrentAgentType(perception.getAgentType());
		this.plan.setTargetDirtType(((DirtAppearance) closest.getDirt().getExternalAppearance()).getDirtType());
		this.plan.setTargetLocation(closest);
		
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": target: " + this.plan.getTargetDirtType().toString() + " dirt on " + closest.getCoordinates().toString() + ".");
		
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
		
		this.plan.pushActionToPerform(CleanAction.class, getBodyId());
		Utils.logWithClass(this.getClass().getSimpleName(), Utils.AGENT + getBodyId() + ": finished bulding plan.");
		
		this.plan.setLastAction(this.plan.pullActionToPerform(getBodyId()));
		
		return buildPhysicalAction(this.plan.getLastAction());
	}

	private void addActionsToPlan(char character, int xDifference, int yDifference, AgentFacingDirection facingDirection) {
		switch(character) {
		case 'L':
			this.plan.pushActionToPerform(TurnLeftAction.class, getBodyId());
			break;
		case 'R':
			this.plan.pushActionToPerform(TurnRightAction.class, getBodyId());
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
			this.plan.pushActionToPerform(action, getBodyId());
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
}