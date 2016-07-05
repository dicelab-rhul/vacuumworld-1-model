package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;
import uk.ac.rhul.cs.dice.monitor.evaluation.EvaluationStrategy;

public class VacuumWorldStepEvaluationStrategy implements EvaluationStrategy<String> {

  @Override
  public Evaluation evaluate(String actor, int startTime, int endTime) {
    System.out.println("EVALUATING");
    return null;
  }

  @Override
  public void update(Perception perception) {
    System.out.println("UPDATING EVALUATION MODEL");
  }
}
