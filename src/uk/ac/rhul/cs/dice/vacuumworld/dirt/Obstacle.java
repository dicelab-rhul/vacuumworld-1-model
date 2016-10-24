package uk.ac.rhul.cs.dice.vacuumworld.dirt;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.PassiveBodyAppearance;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.PassiveBody;
import uk.ac.rhul.cs.dice.gawl.interfaces.observer.CustomObservable;

public abstract class Obstacle extends PassiveBody {

	public Obstacle(PassiveBodyAppearance appearance) {
		super(appearance);
	}

	@Override
	public void update(CustomObservable o, Object arg) {
		return;
	}
}