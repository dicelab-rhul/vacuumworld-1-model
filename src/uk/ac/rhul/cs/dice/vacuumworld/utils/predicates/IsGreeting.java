package uk.ac.rhul.cs.dice.vacuumworld.utils.predicates;

import java.util.function.Predicate;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class IsGreeting implements Predicate<Result> {

	@Override
	public boolean test(Result result) {
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