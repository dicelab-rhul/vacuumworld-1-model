package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;

public class VacuumWorldStepCollectiveEvaluation implements Evaluation {

  private HashMap<String, VacuumWorldStepEvaluation> evaluations;
  
  public VacuumWorldStepCollectiveEvaluation() {
    setEvaluations(new HashMap<>());
  }
  
  public String represent() {
    StringBuilder b = new StringBuilder();
    Iterator<Entry<String, VacuumWorldStepEvaluation>> iter = evaluations.entrySet().iterator();
    while(iter.hasNext()) {
      Entry<String, VacuumWorldStepEvaluation> ent = iter.next();
      b.append(ent.getKey());
      b.append(ent.getValue().represent());
    }
    return b.toString();
  }
  
  public void addEvaluation(String id, VacuumWorldStepEvaluation eval) {
    evaluations.put(id, eval);
  }

  public HashMap<String, VacuumWorldStepEvaluation> getEvaluations() {
    return evaluations;
  }

  public void setEvaluations(HashMap<String, VacuumWorldStepEvaluation> evaluation) {
    this.evaluations = evaluation;
  }
}
