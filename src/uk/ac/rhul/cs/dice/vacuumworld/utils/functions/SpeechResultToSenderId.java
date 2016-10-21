package uk.ac.rhul.cs.dice.vacuumworld.utils.functions;

import java.util.function.Function;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Result;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;
import uk.ac.rhul.cs.dice.vacuumworld.common.VacuumWorldPerception;

public class SpeechResultToSenderId implements Function<Result<VacuumWorldPerception>, String> {

	@Override
	public String apply(Result<VacuumWorldPerception> result) {
		if(result == null) {
			return null;
		}
		
		if(!(result instanceof VacuumWorldSpeechActionResult)) {
			return null;
		}
		
		return ((VacuumWorldSpeechActionResult) result).getSenderId();
	}
}