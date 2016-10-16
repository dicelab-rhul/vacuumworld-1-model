package uk.ac.rhul.cs.dice.vacuumworld.utils.functions;

import java.util.function.Function;

import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechActionResult;

public class SpeechResultToSenderId implements Function<VacuumWorldSpeechActionResult, String> {

	@Override
	public String apply(VacuumWorldSpeechActionResult result) {
		if(result != null) {
			return result.getSenderId();
		}
		else {
			return null;
		}
	}
}