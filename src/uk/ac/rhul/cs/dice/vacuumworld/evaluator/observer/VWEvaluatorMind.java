package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;
import uk.ac.rhul.cs.dice.monitor.actions.ChangedResult;
import uk.ac.rhul.cs.dice.monitor.actions.ReadAction;
import uk.ac.rhul.cs.dice.monitor.actions.ReadResult;
import uk.ac.rhul.cs.dice.monitor.agents.EvaluatorMind;
import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VWEvaluatorMind extends EvaluatorMind {
	private EvaluationStrategy<?> strategy;
	private int changes = 0;
	private ReadAction readAction = new ReadAction();
	private EnvironmentalAction nextAction;
	private ReadResult perception;

	/**
	 * Constructor.
	 * 
	 * @param strategy
	 *            that will be used for evaluation.
	 */
	public VWEvaluatorMind(EvaluationStrategy<?> strategy) {
		this.strategy = strategy;
	}

	@Override
	public void execute(EnvironmentalAction action) {
		if (shouldRead()) {
			read();
		}
		else {
			Utils.log("EVALUATOR DID NOTHING");
		}
	}

	@Override
	public void perceive(Object perceptionWrapper) {
		if (this.perception != null) {
			RefinedPerception[] ps = (RefinedPerception[]) this.perception.getPerceptions().toArray(new RefinedPerception[] {});
			this.strategy.update(ps[0]);
		}
	}

	@Override
	public EnvironmentalAction decide(Object... parameters) {
		if (this.changes > 1) {
			Utils.log("EVALUATOR DECIDED TO READ");
			this.nextAction = this.readAction;
			this.changes = 0;
		}
		else {
			Utils.log("EVALUATOR DECIDED TO DO NOTHING");
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
		Utils.log("RECIEVED CHANGED RESULT");
		this.changes++;
	}

	@Override
	protected boolean shouldRead() {
		return this.nextAction instanceof ReadAction;
	}

	@Override
	protected boolean shouldEvaluate() {
		return shouldRead();
	}

	@Override
	protected void read() {
		Utils.log("READING");
		notifyObservers(nextAction, VWEvaluatorBrain.class);
	}

	@Override
	public Evaluation evaluate() {
		return strategy.evaluate(null, 0, 0);
	}

	@Override
	public void updateCon(CustomObservable o, Object arg) {
		//Useless
	}
}