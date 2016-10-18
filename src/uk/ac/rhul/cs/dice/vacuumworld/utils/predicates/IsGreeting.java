package uk.ac.rhul.cs.dice.vacuumworld.utils.predicates;

import java.util.function.Predicate;

import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class IsGreeting implements Predicate<VacuumWorldSpeechActionResult> {

	@Override
	public boolean test(VacuumWorldSpeechActionResult result) {
		try {
			return result.getPayload().isGreetingAction();
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			
			return false;
		}
	}
}