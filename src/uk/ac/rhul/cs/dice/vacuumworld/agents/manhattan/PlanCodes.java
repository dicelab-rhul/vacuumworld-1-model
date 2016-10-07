package uk.ac.rhul.cs.dice.vacuumworld.agents.manhattan;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.vacuumworld.agents.AgentFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Pair;

public class PlanCodes {
	private static PlanCodes instance;
	private Map<Pair<Integer>, Map<AgentFacingDirection, String>> planCodesMap;
	
	private PlanCodes() {
		this.planCodesMap = initMap();
	}
	
	public static PlanCodes getInstance() {
		if(PlanCodes.instance == null) {
			PlanCodes.instance = new PlanCodes();
		}
		
		return PlanCodes.instance;
	}
	
	public Map<Pair<Integer>, Map<AgentFacingDirection, String>> getPlanCodes() {
		return this.planCodesMap;
	}
	
	private Map<Pair<Integer>, Map<AgentFacingDirection, String>> initMap() {
		Map<AgentFacingDirection, String> pp = fillPositivePositiveMap();
		Map<AgentFacingDirection, String> pe = fillPositiveZeroMap();
		Map<AgentFacingDirection, String> pn = fillPositiveNegativeMap();
		Map<AgentFacingDirection, String> ep = fillZeroPositiveMap();
		Map<AgentFacingDirection, String> ee = fillZeroZeroMap();
		Map<AgentFacingDirection, String> en = fillZeroNegativeMap();
		Map<AgentFacingDirection, String> np = fillNegativePositiveMap();
		Map<AgentFacingDirection, String> ne = fillNegativeZeroMap();
		Map<AgentFacingDirection, String> nn = fillNegativeNegativeMap();
		
		return fillMap(Arrays.asList(pp, pe, pn, ep, ee, en, np, ne, nn));
	}

	private static Map<Pair<Integer>, Map<AgentFacingDirection, String>> fillMap(List<Map<AgentFacingDirection, String>> maps) {
		Map<Pair<Integer>, Map<AgentFacingDirection, String>> toReturn = new HashMap<>();
		
		toReturn.put(new Pair<Integer>(1, 1),  maps.get(0));
		toReturn.put(new Pair<Integer>(1, 0),  maps.get(1));
		toReturn.put(new Pair<Integer>(1, -1),  maps.get(2));
		toReturn.put(new Pair<Integer>(0, 1),  maps.get(3));
		toReturn.put(new Pair<Integer>(0, 0),  maps.get(4));
		toReturn.put(new Pair<Integer>(0, -1),  maps.get(5));
		toReturn.put(new Pair<Integer>(-1, 1),  maps.get(6));
		toReturn.put(new Pair<Integer>(-1, 0),  maps.get(7));
		toReturn.put(new Pair<Integer>(-1, -1),  maps.get(8));
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillPositivePositiveMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "RMRM");
		toReturn.put(AgentFacingDirection.SOUTH, "MLM");
		toReturn.put(AgentFacingDirection.WEST, "LMLM");
		toReturn.put(AgentFacingDirection.EAST, "MRM");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillPositiveZeroMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "RM");
		toReturn.put(AgentFacingDirection.SOUTH, "LM");
		toReturn.put(AgentFacingDirection.WEST, "RRM");
		toReturn.put(AgentFacingDirection.EAST, "M");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillPositiveNegativeMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "MRM");
		toReturn.put(AgentFacingDirection.SOUTH, "LMLM");
		toReturn.put(AgentFacingDirection.WEST, "RMRM");
		toReturn.put(AgentFacingDirection.EAST, "MLM");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillZeroPositiveMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "RRM");
		toReturn.put(AgentFacingDirection.SOUTH, "M");
		toReturn.put(AgentFacingDirection.WEST, "LM");
		toReturn.put(AgentFacingDirection.EAST, "RM");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillZeroZeroMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "");
		toReturn.put(AgentFacingDirection.SOUTH, "");
		toReturn.put(AgentFacingDirection.WEST, "");
		toReturn.put(AgentFacingDirection.EAST, "");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillZeroNegativeMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "M");
		toReturn.put(AgentFacingDirection.SOUTH, "RRM");
		toReturn.put(AgentFacingDirection.WEST, "RM");
		toReturn.put(AgentFacingDirection.EAST, "LM");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillNegativePositiveMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "LMLM");
		toReturn.put(AgentFacingDirection.SOUTH, "MRM");
		toReturn.put(AgentFacingDirection.WEST, "MLM");
		toReturn.put(AgentFacingDirection.EAST, "RMRM");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillNegativeZeroMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "LM");
		toReturn.put(AgentFacingDirection.SOUTH, "RM");
		toReturn.put(AgentFacingDirection.WEST, "M");
		toReturn.put(AgentFacingDirection.EAST, "RRM");
		
		return toReturn;
	}

	private static Map<AgentFacingDirection, String> fillNegativeNegativeMap() {
		Map<AgentFacingDirection, String> toReturn = new EnumMap<>(AgentFacingDirection.class);
		
		toReturn.put(AgentFacingDirection.NORTH, "MLM");
		toReturn.put(AgentFacingDirection.SOUTH, "RMRM");
		toReturn.put(AgentFacingDirection.WEST, "MRM");
		toReturn.put(AgentFacingDirection.EAST, "LMLM");
		
		return toReturn;
	}
}