package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;

public abstract class DatabaseReadAction extends DatabaseAction {

	public DatabaseReadAction(VacuumWorldDatabaseInteractions actionToPerform) {
		super(actionToPerform);
	}
}