package uk.ac.rhul.cs.dice.vacuumworld.environment.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.DefaultActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Event;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.EnvironmentalSpace;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.Space;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.AbstractPhysics;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MonitoringUpdateEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldEvent;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.common.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.common.Obstacle;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.environment.Lockable;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocationType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;

public class VacuumWorldPhysics extends AbstractPhysics implements VacuumWorldPhysicsInterface {
	private ConcurrentMap<Long, VacuumWorldCleaningAgent> activeAgents;
	private ConcurrentMap<Long, List<String>> sensorsToNotify;

	public VacuumWorldPhysics() {
		this.activeAgents = new ConcurrentHashMap<>();
		this.sensorsToNotify = new ConcurrentHashMap<>();
	}

	@Override
	public synchronized Result attempt(Event event, Space context) {
		this.activeAgents.put(Thread.currentThread().getId(), (VacuumWorldCleaningAgent) event.getActor());
		this.sensorsToNotify.putIfAbsent(Thread.currentThread().getId(), new ArrayList<>());
		this.sensorsToNotify.get(Thread.currentThread().getId()).add(((VacuumWorldEvent) event).getSensorToCallBackId());

		return event.getAction().attempt(this, context);
	}

	private synchronized VacuumWorldLocation getCurrentActorLocation(VacuumWorldSpace context) {
		VacuumWorldCoordinates coordinates = this.activeAgents.get(Thread.currentThread().getId()).getCurrentLocation();

		return (VacuumWorldLocation) context.getLocation(coordinates);
	}

	private synchronized void releaseWriteLockIfNecessary(Lockable lockable) {
		lockable.releaseExclusiveWriteLock();
	}

	/**
	 * A turning action is always possible.
	 */
	@Override
	public synchronized boolean isPossible(TurnLeftAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TurnLeftAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(TurnLeftAction action, Space context) {
		VacuumWorldLocation agentLocation = null;

		try {
			agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
			agentLocation.getExclusiveWriteLock();

			action.setAgentOldFacingDirection(agentLocation.getAgent().getFacingDirection());
			// System.out.println(Thread.currentThread().getId() + "Local
			// variable before turning left: " +
			// agentLocation.getAgent().getFacingDirection());
			// System.out.println(Thread.currentThread().getId() + "List
			// variable before turning left: " +
			// this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection());
			agentLocation.getAgent().turnLeft();

			// System.out.println(Thread.currentThread().getId() + "Local
			// variable after turning left: " +
			// agentLocation.getAgent().getFacingDirection());
			// System.out.println(Thread.currentThread().getId() + "List
			// variable after turning left: " +
			// this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection());

			VacuumWorldPerception newPerception = perceive((VacuumWorldSpace) context,
					this.activeAgents.get(Thread.currentThread().getId()).getPerceptionRange(),
					this.activeAgents.get(Thread.currentThread().getId()).canSeeBehind());
			agentLocation.releaseExclusiveWriteLock();

			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, newPerception,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		} catch (Exception e) {
			e.printStackTrace();
			this.activeAgents.remove(Thread.currentThread().getId());
			releaseWriteLockIfNecessary(agentLocation);
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
	}

	@Override
	public synchronized boolean succeeded(TurnLeftAction action, Space context) {
		boolean check = this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection() == action
				.getAgentOldFacingDirection().getLeftDirection();

		if (!check) {
			Logger.getGlobal().log(Level.SEVERE,
					Thread.currentThread().getId() + "FAILED: succeeded:TurnLeftAction : List variable:"
							+ this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection()
							+ ", Local variable.getLeft():" + action.getAgentOldFacingDirection().getLeftDirection());

			Logger.getGlobal().log(Level.SEVERE,
					Thread.currentThread().getId() + "Furthermore, the old direction (from Local variable) was "
							+ action.getAgentOldFacingDirection());
		}
		return check;
	}

	/**
	 * A turning action is always possible.
	 */
	@Override
	public synchronized boolean isPossible(TurnRightAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(TurnRightAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(TurnRightAction action, Space context) {
		VacuumWorldLocation agentLocation = null;

		try {
			agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
			agentLocation.getExclusiveWriteLock();

			action.setAgentOldFacingDirection(agentLocation.getAgent().getFacingDirection());
			// System.out.println(Thread.currentThread().getId() + "Local
			// variable before turning right: " +
			// agentLocation.getAgent().getFacingDirection());
			// System.out.println(Thread.currentThread().getId() + "List
			// variable before turning right: " +
			// this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection());
			agentLocation.getAgent().turnRight();
			// System.out.println(Thread.currentThread().getId() + "Local
			// variable after turning right: " +
			// agentLocation.getAgent().getFacingDirection());
			// System.out.println(Thread.currentThread().getId() + "List
			// variable after turning right: " +
			// this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection());

			VacuumWorldPerception newPerception = perceive((VacuumWorldSpace) context,
					this.activeAgents.get(Thread.currentThread().getId()).getPerceptionRange(),
					this.activeAgents.get(Thread.currentThread().getId()).canSeeBehind());
			agentLocation.releaseExclusiveWriteLock();

			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, newPerception,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		} catch (Exception e) {
			e.printStackTrace();
			this.activeAgents.remove(Thread.currentThread().getId());
			releaseWriteLockIfNecessary(agentLocation);
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
	}

	@Override
	public synchronized boolean succeeded(TurnRightAction action, Space context) {
		boolean check = this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection() == action
				.getAgentOldFacingDirection().getRightDirection();

		if (!check) {
			Logger.getGlobal().log(Level.SEVERE,
					Thread.currentThread().getId() + "FAILED: succeeded:TurnRightAction : List variable:"
							+ this.activeAgents.get(Thread.currentThread().getId()).getFacingDirection()
							+ ", Local variable.getRight():" + action.getAgentOldFacingDirection().getRightDirection());

			Logger.getGlobal().log(Level.SEVERE,
					Thread.currentThread().getId() + "Furthermore, the old direction (from Local variable) was "
							+ action.getAgentOldFacingDirection());
		}
		return check;
	}

	@Override
	public synchronized boolean isPossible(MoveAction action, Space context) {
		AgentFacingDirection agentFacingDirection = this.activeAgents.get(Thread.currentThread().getId())
				.getFacingDirection();
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		VacuumWorldCoordinates originalCooridinates = agentLocation.getCoordinates();
		VacuumWorldLocation targetLocation = ((VacuumWorldSpace) context).getFrontLocation(originalCooridinates,
				agentFacingDirection);

		return !checkForWall(agentLocation, agentFacingDirection) && !checkForObstacle(targetLocation);
	}

	private synchronized boolean checkForWall(VacuumWorldLocation agentLocation,
			AgentFacingDirection agentFacingDirection) {
		return agentLocation.getNeighborLocation(agentFacingDirection) == VacuumWorldLocationType.WALL;
	}

	private synchronized boolean checkForObstacle(VacuumWorldLocation targetLocation) {
		if (targetLocation == null) {
			return true;
		}

		return checkForGenericObstacle(targetLocation) || checkForAnotherAgent(targetLocation);
	}

	private synchronized boolean checkForAnotherAgent(VacuumWorldLocation targetLocation) {
		return targetLocation.getAgent() != null;
	}

	private synchronized boolean checkForGenericObstacle(VacuumWorldLocation targetLocation) {
		Obstacle potentialObstacle = targetLocation.getObstacle();

		if (potentialObstacle == null || potentialObstacle instanceof Dirt) {
			return false;
		}

		return true;
	}

	@Override
	public synchronized boolean isNecessary(MoveAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(MoveAction action, Space context) {
		VacuumWorldLocation agentLocation = null;
		VacuumWorldLocation targetLocation = null;

		try {
			AgentFacingDirection agentFacingDirection = this.activeAgents.get(Thread.currentThread().getId())
					.getFacingDirection();
			agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
			VacuumWorldCoordinates originalCooridinates = agentLocation.getCoordinates();
			targetLocation = ((VacuumWorldSpace) context).getFrontLocation(originalCooridinates, agentFacingDirection);

			agentLocation.getExclusiveWriteLock();
			targetLocation.getExclusiveWriteLock();

			action.setOldLocationCoordinates(originalCooridinates);

			((VacuumWorldLocation) ((EnvironmentalSpace) context).getLocation(agentLocation.getCoordinates()))
					.removeAgent();
			targetLocation.addAgent(this.activeAgents.get(Thread.currentThread().getId()));
			targetLocation.getAgent().setCurrentLocation(originalCooridinates.getNewCoordinates(agentFacingDirection));
			this.activeAgents.get(Thread.currentThread().getId())
					.setCurrentLocation(originalCooridinates.getNewCoordinates(agentFacingDirection));

			VacuumWorldPerception newPerception = perceive((VacuumWorldSpace) context,
					this.activeAgents.get(Thread.currentThread().getId()).getPerceptionRange(),
					this.activeAgents.get(Thread.currentThread().getId()).canSeeBehind());
			agentLocation.releaseExclusiveWriteLock();
			targetLocation.releaseExclusiveWriteLock();

			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, newPerception,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		} catch (Exception e) {
			e.printStackTrace();
			this.activeAgents.remove(Thread.currentThread().getId());
			releaseWriteLockIfNecessary(agentLocation);
			releaseWriteLockIfNecessary(targetLocation);
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
	}

	@Override
	public synchronized boolean succeeded(MoveAction action, Space context) {
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
		VacuumWorldLocation agentOldLocation = (VacuumWorldLocation) ((EnvironmentalSpace) context)
				.getLocation(action.getOldLocationCoordinates());

		return !(agentOldLocation.isAnAgentPresent())
				&& this.activeAgents.get(Thread.currentThread().getId()).equals(agentLocation.getAgent());
	}

	@Override
	public synchronized boolean isPossible(CleanAction action, Space context) {
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);

		if (agentLocation == null) {
			return false;
		}

		return agentLocation.isDirtPresent();
	}

	@Override
	public synchronized boolean isNecessary(CleanAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(CleanAction action, Space context) {
		VacuumWorldLocation agentLocation = null;

		try {
			agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);
			agentLocation.getExclusiveWriteLock();

			agentLocation.removeDirt();

			VacuumWorldPerception newPerception = perceive((VacuumWorldSpace) context,
					this.activeAgents.get(Thread.currentThread().getId()).getPerceptionRange(),
					this.activeAgents.get(Thread.currentThread().getId()).canSeeBehind());
			agentLocation.releaseExclusiveWriteLock();

			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, newPerception,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		} catch (Exception e) {
			e.printStackTrace();
			this.activeAgents.remove(Thread.currentThread().getId());
			releaseWriteLockIfNecessary(agentLocation);
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
	}

	@Override
	public synchronized boolean succeeded(CleanAction action, Space context) {
		VacuumWorldLocation agentLocation = getCurrentActorLocation((VacuumWorldSpace) context);

		return !(agentLocation.isDirtPresent());
	}

	/**
	 * A perceiving action is always possible.
	 */
	@Override
	public synchronized boolean isPossible(PerceiveAction action, Space context) {
		return true;
	}

	@Override
	public synchronized boolean isNecessary(PerceiveAction action, Space context) {
		return false;
	}

	@Override
	public synchronized Result perform(PerceiveAction action, Space context) {
		try {
			int perceptionRange = action.getPerceptionRange();
			boolean canSeeBehind = action.canAgentSeeBehind();

			VacuumWorldPerception newPerception = perceive((VacuumWorldSpace) context, perceptionRange, canSeeBehind);

			return new VacuumWorldActionResult(ActionResult.ACTION_DONE, newPerception,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		} catch (Exception e) {
			e.printStackTrace();
			this.activeAgents.remove(Thread.currentThread().getId());
			return new VacuumWorldActionResult(ActionResult.ACTION_FAILED, e, null,
					this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
	}

	private synchronized VacuumWorldPerception perceive(VacuumWorldSpace context, int perceptionRange,
			boolean canSeeBehind) {
		VacuumWorldCleaningAgent current = this.activeAgents.get(Thread.currentThread().getId());
		AgentFacingDirection direction = current.getFacingDirection();

		int northernOverhead = getNorthernOverhead(perceptionRange, canSeeBehind, direction);
		int southernOverhead = getSouthernOverhead(perceptionRange, canSeeBehind, direction);
		int westernOverhead = getWesternOverhead(perceptionRange, canSeeBehind, direction);
		int easternOverhead = getEasternOverhead(perceptionRange, canSeeBehind, direction);

		int[] overheads = new int[] { northernOverhead, southernOverhead, westernOverhead, easternOverhead };

		return perceive(context, overheads);
	}

	private synchronized int getEasternOverhead(int perceptionRange, boolean canSeeBehind,
			AgentFacingDirection direction) {
		int baseOverhead = perceptionRange - 1;

		if (!canSeeBehind && direction == AgentFacingDirection.WEST) {
			baseOverhead = 0;
		}

		return baseOverhead;
	}

	private synchronized int getWesternOverhead(int perceptionRange, boolean canSeeBehind,
			AgentFacingDirection direction) {
		int baseOverhead = perceptionRange - 1;

		if (!canSeeBehind && direction == AgentFacingDirection.EAST) {
			baseOverhead = 0;
		}

		return baseOverhead;
	}

	private synchronized int getSouthernOverhead(int perceptionRange, boolean canSeeBehind,
			AgentFacingDirection direction) {
		int baseOverhead = perceptionRange - 1;

		if (!canSeeBehind && direction == AgentFacingDirection.NORTH) {
			baseOverhead = 0;
		}

		return baseOverhead;
	}

	private synchronized int getNorthernOverhead(int perceptionRange, boolean canSeeBehind,
			AgentFacingDirection direction) {
		int baseOverhead = perceptionRange - 1;

		if (!canSeeBehind && direction == AgentFacingDirection.SOUTH) {
			baseOverhead = 0;
		}

		return baseOverhead;
	}

	private synchronized VacuumWorldPerception perceive(VacuumWorldSpace context, int[] overheads) {
		VacuumWorldCoordinates currentCoordinates = getCurrentActorLocation(context).getCoordinates();
		int currentX = currentCoordinates.getX();
		int currentY = currentCoordinates.getY();

		Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = perceive(context, overheads, currentX, currentY);
		return new VacuumWorldPerception(perception, currentCoordinates);
	}

	private synchronized Map<VacuumWorldCoordinates, VacuumWorldLocation> perceive(VacuumWorldSpace context,
			int[] overheads, int currentX, int currentY) {
		Map<VacuumWorldCoordinates, VacuumWorldLocation> perception = new HashMap<>();

		for (int i = currentX - overheads[2]; i <= currentX + overheads[3]; i++) {
			for (int j = currentY - overheads[0]; j <= currentY + overheads[1]; j++) {
				VacuumWorldCoordinates temp = new VacuumWorldCoordinates(i, j);
				VacuumWorldLocation location = (VacuumWorldLocation) context.getLocation(temp);

				if (location != null) {
					perception.put(temp, location);
				}
			}
		}

		return perception;
	}

	/**
	 * A perceiving action has no post-conditions to check.
	 */
	@Override
	public synchronized boolean succeeded(PerceiveAction action, Space context) {
		return true;
	}
	
	@Override
	public boolean isPossible(SpeechAction action, Space context) {
		return true;
	}

	@Override
	public boolean isNecessary(SpeechAction action, Space context) {
		return false;
	}

	@Override
	public Result perform(SpeechAction action, Space context) {
		return new VacuumWorldSpeechActionResult(ActionResult.ACTION_DONE, action);
	}

	@Override
	public boolean succeeded(SpeechAction action, Space context) {
		return true;
	}

	@Override
	public synchronized void update(CustomObservable o, Object arg) {
		if (o instanceof VacuumWorldSpace && arg instanceof Object[]) {
			manageEnvironmentRequest((Object[]) arg);
		}
	}

	private synchronized void manageEnvironmentRequest(Object[] arg) {
		if (arg.length != 2) {
			return;
		}

		if (arg[0] instanceof VacuumWorldEvent && arg[1] instanceof VacuumWorldSpace) {
			attemptEvent((VacuumWorldEvent) arg[0], (VacuumWorldSpace) arg[1]);
		}
	}

	private synchronized void attemptEvent(VacuumWorldEvent event, VacuumWorldSpace context) {
		Result result = event.attempt(this, context);
		ActionResult code = ((DefaultActionResult) result).getActionResult();
		MonitoringUpdateEvent ue = new MonitoringUpdateEvent(event.getAction(), event.getTimestamp(), event.getActor(),
				code);
		notifyObservers(ue, VacuumWorldMonitoringContainer.class);

		if (result.getRecipientsIds() == null) {
			result.setRecipientsIds(this.sensorsToNotify.get(Thread.currentThread().getId()));
		}
		else if(result.getRecipientsIds().isEmpty()) {
			result.setRecipientsIds(this.sensorsToNotify.get(Thread.currentThread().getId()));
		}

		this.activeAgents.remove(Thread.currentThread().getId());
		this.sensorsToNotify.remove(Thread.currentThread().getId());

		notifyObservers(result, VacuumWorldSpace.class);
	}
}