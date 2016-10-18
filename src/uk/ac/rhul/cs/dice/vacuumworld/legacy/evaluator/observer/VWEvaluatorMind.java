package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer;

import java.io.File;
import java.util.Map.Entry;
import java.util.logging.Handler;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.ChangedResult;
import uk.ac.rhul.cs.dice.monitor.actions.ReadAction;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorMind;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.EvaluateAction;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.VacuumWorldReadAction;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.actions.VacuumWorldReadResult;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor.VacuumWorldStepCollectiveEvaluation;
import uk.ac.rhul.cs.dice.vacuumworld.legacy.basicmonitor.VacuumWorldStepEvaluation;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VWEvaluatorMind extends EvaluatorMind {
	private int changes = 0;
	private int evaluate = 0;
	private EnvironmentalAction nextAction;
	private ReadResult perception;
	private CollectionRepresentation dirtCollection;

	/*
	 * IMPORTANT NOTE. The last read is always one cycle out of sync because the
	 * first entry in the database is on cycle 1 not cycle 0. This is taken into
	 * account when reading the database and performing evaluations.
	 */
	private int lastCycleRead = 0;
	private int currentCycle = 0;

	public VWEvaluatorMind(EvaluationStrategy<?> strategy, CollectionRepresentation dirtCollection, CollectionRepresentation agentCollection) {
		super(strategy, agentCollection);
		this.setDirtCollection(dirtCollection);
	}

	private void outputEvaluation(VacuumWorldStepCollectiveEvaluation eval) {
		File e = new File("logs/eval/evaluation.log");
		Logger log = Utils.fileLogger(e.getPath(), true);
		log.info("Evaluation up to cycle: " + this.lastCycleRead);
		
		for(Entry<String, VacuumWorldStepEvaluation> evaluation : eval.getEvaluations().entrySet()) {
			log.info(evaluation.getKey() + "\n" + evaluation.getValue().represent());
		}
		
		closeHandlers(log);
	}

	private void closeHandlers(Logger log) {
		Handler[] hs = log.getHandlers();
		
		for (Handler h : hs) {
			h.close();
		}
	}

	@Override
	public void execute(EnvironmentalAction action) {
		if (shouldRead()) {
			read();
		}
		else if (shouldEvaluate()) {
			outputEvaluation((VacuumWorldStepCollectiveEvaluation) evaluate());
		}
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		if (this.perception != null) {
			VacuumWorldReadResult p = (VacuumWorldReadResult) this.perception;
			DatabasePerception dbPerception = new DatabasePerception(p.getAgents(), p.getDirts());
			dbPerception.setLastCycleRead(this.lastCycleRead);
			this.getStrategy().update(dbPerception);
			this.perception = null;
		}
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		this.currentCycle++;
		
		if (this.changes > 3) {
			// for the step evaluation we don't need any dirt information
			this.nextAction = new VacuumWorldReadAction(null, this.getAgentCollection(), this.lastCycleRead);
			this.changes = 0;
			this.evaluate++;
		} 
		else if (this.evaluate > 1) {
			this.nextAction = new EvaluateAction();
			this.evaluate = 0;
		} 
		else {
			this.nextAction = null;
		}
		return this.nextAction;
	}

	@Override
	protected void manageReadResult(ReadResult result) {
		this.perception = result;
	}

	@Override
	protected void manageChangedResult(ChangedResult result) {
		this.changes++;
	}

	@Override
	protected boolean shouldRead() {
		return this.nextAction instanceof ReadAction;
	}

	@Override
	protected boolean shouldEvaluate() {
		return this.nextAction instanceof EvaluateAction;
	}

	@Override
	protected void read() {
		this.lastCycleRead = this.currentCycle;
		notifyObservers(this.nextAction, VWEvaluatorBrain.class);
	}

	@Override
	public Evaluation evaluate() {
		return this.getStrategy().evaluate(null, 0, 0);
	}

	@Override
	public void updateCon(CustomObservable o, Object arg) {
		// Useless
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