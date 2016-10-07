package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.List;

import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;

public class AgentAwarenessRepresentation {
	private String myid;
	private List<String> myactuatorids;
	private List<String> myEars;
	private List<String> myEyes;
	private VacuumWorldAgentType type;

	public AgentAwarenessRepresentation(String myid, List<String> myactuatorids, List<String> myEars, List<String> myEyes, VacuumWorldAgentType type) {
		this.myid = myid;
		this.myactuatorids = myactuatorids;
		this.myEars = myEars;
		this.myEyes = myEyes;
		this.type = type;
	}

	public String getMyid() {
		return myid;
	}

	public void setMyid(String myid) {
		this.myid = myid;
	}

	public List<String> getMyactuatorids() {
		return this.myactuatorids;
	}

	public void setActuatorid(String myactuatorid) {
		this.myactuatorids.add(myactuatorid);
	}

	public List<String> getMyEars() {
		return this.myEars;
	}

	public void setMyEars(List<String> myEars) {
		this.myEars = myEars;
	}

	public List<String> getMyEyes() {
		return this.myEyes;
	}

	public void setMyEyes(List<String> myEyes) {
		this.myEyes = myEyes;
	}

	public VacuumWorldAgentType getType() {
		return this.type;
	}

	public void setType(VacuumWorldAgentType type) {
		this.type = type;
	}
}