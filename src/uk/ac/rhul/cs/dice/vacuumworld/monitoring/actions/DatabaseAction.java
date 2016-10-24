package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;

public abstract class DatabaseAction extends AbstractAction {
	private VacuumWorldDatabaseInteractions actionToPerform;
	
	public DatabaseAction(VacuumWorldDatabaseInteractions actionToPerform) {
		this.actionToPerform = actionToPerform;
	}
	
	public VacuumWorldDatabaseInteractions getActionToPerform() {
		return this.actionToPerform;
	}
}