package uk.ac.rhul.cs.dice.vacuumworld.basicmonitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.rhul.cs.dice.monitor.evaluation.Evaluation;

public class VacuumWorldStepCollectiveEvaluation implements Evaluation {

  private HashMap<String, VacuumWorldStepEvaluation> evaluation;
  
  public VacuumWorldStepCollectiveEvaluation() {
    setEvaluation(new HashMap<>());
  }
  
  public String represent() {
    StringBuilder b = new StringBuilder();
    Iterator<Entry<String, VacuumWorldStepEvaluation>> iter = evaluation.entrySet().iterator();
    while(iter.hasNext()) {
      b.append(iter.next().getValue().represent());
    }
    return b.toString();
  }
  
  public void addEvaluation(String id, VacuumWorldStepEvaluation eval) {
    evaluation.put(id, eval);
  }

  public HashMap<String, VacuumWorldStepEvaluation> getEvaluation() {
    return evaluation;
  }

  public void setEvaluation(HashMap<String, VacuumWorldStepEvaluation> evaluation) {
    this.evaluation = evaluation;
  }
}
