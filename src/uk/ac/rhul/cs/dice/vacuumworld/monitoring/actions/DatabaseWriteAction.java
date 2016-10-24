package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;

public abstract class DatabaseWriteAction extends DatabaseAction {

	public DatabaseWriteAction(VacuumWorldDatabaseInteractions actionToPerform) {
		super(actionToPerform);
	}
}