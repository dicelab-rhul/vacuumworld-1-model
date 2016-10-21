package uk.ac.rhul.cs.dice.vacuumworld.utils.predicates;

import java.util.function.Predicate;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class IsGreeting implements Predicate<Result<VacuumWorldPerception>> {

	@Override
	public boolean test(Result<VacuumWorldPerception> result) {
		if(!(result instanceof VacuumWorldSpeechActionResult)) {
			return false;
		}
		
		try {
			return ((VacuumWorldSpeechActionResult) result).getPayload().isGreetingAction();
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}
}