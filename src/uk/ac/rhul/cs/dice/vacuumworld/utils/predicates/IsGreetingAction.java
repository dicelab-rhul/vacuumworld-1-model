package uk.ac.rhul.cs.dice.vacuumworld.utils.predicates;

import java.util.function.Predicate;

import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.VacuumWorldSpeechPayload;

public class IsGreetingAction implements Predicate<SpeechAction> {

    @Override
    public boolean test(SpeechAction action) {
	return ((VacuumWorldSpeechPayload) action.getPayload()).isGreetingAction();
    }
}