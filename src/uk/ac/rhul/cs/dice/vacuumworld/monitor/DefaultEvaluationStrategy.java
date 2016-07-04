package uk.ac.rhul.cs.dice.vacuumworld.monitor;

import java.util.List;

import uk.ac.rhul.cs.dice.monitor.common.RefinedPerception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;

public class DefaultEvaluationStrategy implements EvaluationStrategy{

  @Override
  public Evaluation evaluate(List<RefinedPerception> perceptions) {
    //TODO change
    return new VWStepsEvaluation();
  }

}
