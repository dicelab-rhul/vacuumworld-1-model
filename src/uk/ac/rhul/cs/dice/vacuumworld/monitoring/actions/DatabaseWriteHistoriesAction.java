package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import java.util.List;

import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;

public abstract class DatabaseWriteHistoriesAction extends DatabaseWriteAction {
	private List<VacuumWorldMonitoringActionResult> historiesWrappers;
	
	public DatabaseWriteHistoriesAction(VacuumWorldDatabaseInteractions actionToPerform, List<VacuumWorldMonitoringActionResult> historiesWrappers) {
		super(actionToPerform);
		
		this.historiesWrappers = historiesWrappers;
	}

	public List<VacuumWorldMonitoringActionResult> getHistoriesWrappers() {
		return this.historiesWrappers;
	}
}