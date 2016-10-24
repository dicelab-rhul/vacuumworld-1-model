package uk.ac.rhul.cs.dice.vacuumworld.monitoring.actions;

import uk.ac.rhul.cs.dice.vacuumworld.monitoring.database.VacuumWorldDatabaseInteractions;

public abstract class DatabaseSingleReadHistoryAction extends DatabaseReadAction {
	private String idToReadAbout;
	
	public DatabaseSingleReadHistoryAction(VacuumWorldDatabaseInteractions actionToPerform, String idToReadAbout) {
		super(actionToPerform);
		
		this.idToReadAbout = idToReadAbout;
	}
	
	public String getIdToReadAbout() {
		return this.idToReadAbout;
	}
}