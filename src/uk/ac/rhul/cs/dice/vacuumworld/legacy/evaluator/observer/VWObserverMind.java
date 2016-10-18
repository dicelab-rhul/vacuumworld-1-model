package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer;

import java.util.Set;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.ActionResult;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Mind;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.DatabaseAction;
import uk.ac.rhul.cs.dice.monitor.actions.WriteResult;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverAgent;
import uk.ac.rhul.cs.dice.monitor.agents.ObserverMind;
import uk.ac.rhul.cs.dice.monitor.common.DefaultPerceptionRefiner;
import uk.ac.rhul.cs.dice.monitor.common.PerceptionRefiner;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.MonitoringResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.VacuumWorldWriteAction;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

/**
 * A Vacuum World implementation of {@link ObserverMind}. Contains Vacuum World
 * Specific functionality including {@link Thread} related functionality. This
 * {@link Mind} is set up to handle {@link MonitoringResult MonitoringResults}
 * from {@link VWObserverBrain}.
 * 
 * @author Ben Wilkins
 *
 */
public class VWObserverMind extends ObserverMind {
	private EnvironmentalAction nextAction;
	private MonitoringResult currentPerception;
	private CollectionRepresentation dirtCollection;

	/**
	 * Constructor.
	 * 
	 * @param refiner
	 *            for refining {@link Perceptions}. Note that this should be
	 *            {@link DefaultPerceptionRefiner} as (unless for a good reason)
	 *            the perception should not be refined further. Only parse a
	 *            value other than {@link DefaultPerceptionRefiner} if you
	 *            understand the implications; having create a new
	 *            {@link MongoBridge} etc.
	 * @param collection
	 *            that will be used by this database agent when attempting
	 *            {@link DatabaseAction DatabaseActions}
	 */
	public VWObserverMind(PerceptionRefiner refiner, CollectionRepresentation dirtCollection, CollectionRepresentation agentCollection) {
		super(refiner, agentCollection);
		this.setDirtCollection(dirtCollection);
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		try {
			this.nextAction = getAvailableActionsForThisCycle().get(0).newInstance();
		}
		catch (InstantiationException | IllegalAccessException e) {
			Utils.log(e);
		}
		
		return this.nextAction;
	}

	@Override
	public void updateCon(CustomObservable o, Object arg) {
		if (o instanceof VWObserverBrain && arg instanceof MonitoringResult) {
			this.currentPerception = (MonitoringResult) arg;
		}
	}

	/**
	 * Starts the perceive process, which for an {@link ObserverAgent} involves
	 * sending the perception to a database.
	 */
	@Override
	public void perceive(Object perceptionWrapper) {
		notifyObservers(null, VWObserverBrain.class);
		
		if (this.currentPerception != null) {
			this.storePerception(new VacuumWorldWriteAction(this.getDirtCollection(), this.getAgentCollection(), super.refine(this.currentPerception.getPerception())));
		}
	}

	@Override
	public void execute(EnvironmentalAction action) {
		notifyObservers(this.nextAction, VWObserverBrain.class);
	}

	@Override
	protected void manageWriteResult(WriteResult result) {
		if (result.getActionResult().equals(ActionResult.ACTION_DONE)) {
			return;
		}
		else {
			Utils.log(result.getFailureReason());
		}
	}

	public void setAvailableActions(Set<Class<? extends AbstractAction>> actions) {
		for (Class<? extends AbstractAction> action : actions) {
			super.addAvailableActionForThisCycle(action);
		}
	}

	public CollectionRepresentation getDirtCollection() {
		return this.dirtCollection;
	}

	public void setDirtCollection(CollectionRepresentation dirtCollection) {
		this.dirtCollection = dirtCollection;
	}

	public CollectionRepresentation getAgentCollection() {
		return super.getCollection();
	}

	public void setAgentCollection(CollectionRepresentation agentCollection) {
		super.setCollection(agentCollection);
	}

	/**
	 * 
	 * @deprecated should use {@link #getDirtCollection()} or
	 *             {@link #getAgentCollection()} instead
	 */
	@Override
	@Deprecated
	public CollectionRepresentation getCollection() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @deprecated should use {@link #setDirtCollection()} or
	 *             {@link #setAgentCollection()} instead
	 */
	@Override
	@Deprecated
	public void setCollection(CollectionRepresentation collection) {
		throw new UnsupportedOperationException();
	}
}