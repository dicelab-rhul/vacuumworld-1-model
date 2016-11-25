package uk.ac.rhul.cs.dice.vacuumworld.agents.minds.manhattan;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Pair;

public class PlanCodes {
    private static PlanCodes instance;
    private Map<Pair<Integer>, Map<ActorFacingDirection, String>> planCodesMap;

    private PlanCodes() {
	this.planCodesMap = initMap();
    }

    public static PlanCodes getInstance() {
	if (PlanCodes.instance == null) {
	    PlanCodes.instance = new PlanCodes();
	}

	return PlanCodes.instance;
    }

    public Map<Pair<Integer>, Map<ActorFacingDirection, String>> getPlanCodes() {
	return this.planCodesMap;
    }

    private Map<Pair<Integer>, Map<ActorFacingDirection, String>> initMap() {
	Map<ActorFacingDirection, String> pp = fillPositivePositiveMap();
	Map<ActorFacingDirection, String> pe = fillPositiveZeroMap();
	Map<ActorFacingDirection, String> pn = fillPositiveNegativeMap();
	Map<ActorFacingDirection, String> ep = fillZeroPositiveMap();
	Map<ActorFacingDirection, String> ee = fillZeroZeroMap();
	Map<ActorFacingDirection, String> en = fillZeroNegativeMap();
	Map<ActorFacingDirection, String> np = fillNegativePositiveMap();
	Map<ActorFacingDirection, String> ne = fillNegativeZeroMap();
	Map<ActorFacingDirection, String> nn = fillNegativeNegativeMap();

	return fillMap(Arrays.asList(pp, pe, pn, ep, ee, en, np, ne, nn));
    }

    private static Map<Pair<Integer>, Map<ActorFacingDirection, String>> fillMap(List<Map<ActorFacingDirection, String>> maps) {
	Map<Pair<Integer>, Map<ActorFacingDirection, String>> toReturn = new HashMap<>();

	toReturn.put(new Pair<Integer>(1, 1), maps.get(0));
	toReturn.put(new Pair<Integer>(1, 0), maps.get(1));
	toReturn.put(new Pair<Integer>(1, -1), maps.get(2));
	toReturn.put(new Pair<Integer>(0, 1), maps.get(3));
	toReturn.put(new Pair<Integer>(0, 0), maps.get(4));
	toReturn.put(new Pair<Integer>(0, -1), maps.get(5));
	toReturn.put(new Pair<Integer>(-1, 1), maps.get(6));
	toReturn.put(new Pair<Integer>(-1, 0), maps.get(7));
	toReturn.put(new Pair<Integer>(-1, -1), maps.get(8));

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillPositivePositiveMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "RMRM");
	toReturn.put(ActorFacingDirection.SOUTH, "MLM");
	toReturn.put(ActorFacingDirection.WEST, "LMLM");
	toReturn.put(ActorFacingDirection.EAST, "MRM");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillPositiveZeroMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "RM");
	toReturn.put(ActorFacingDirection.SOUTH, "LM");
	toReturn.put(ActorFacingDirection.WEST, "RRM");
	toReturn.put(ActorFacingDirection.EAST, "M");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillPositiveNegativeMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "MRM");
	toReturn.put(ActorFacingDirection.SOUTH, "LMLM");
	toReturn.put(ActorFacingDirection.WEST, "RMRM");
	toReturn.put(ActorFacingDirection.EAST, "MLM");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillZeroPositiveMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "RRM");
	toReturn.put(ActorFacingDirection.SOUTH, "M");
	toReturn.put(ActorFacingDirection.WEST, "LM");
	toReturn.put(ActorFacingDirection.EAST, "RM");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillZeroZeroMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "");
	toReturn.put(ActorFacingDirection.SOUTH, "");
	toReturn.put(ActorFacingDirection.WEST, "");
	toReturn.put(ActorFacingDirection.EAST, "");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillZeroNegativeMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "M");
	toReturn.put(ActorFacingDirection.SOUTH, "RRM");
	toReturn.put(ActorFacingDirection.WEST, "RM");
	toReturn.put(ActorFacingDirection.EAST, "LM");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillNegativePositiveMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "LMLM");
	toReturn.put(ActorFacingDirection.SOUTH, "MRM");
	toReturn.put(ActorFacingDirection.WEST, "MLM");
	toReturn.put(ActorFacingDirection.EAST, "RMRM");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillNegativeZeroMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "LM");
	toReturn.put(ActorFacingDirection.SOUTH, "RM");
	toReturn.put(ActorFacingDirection.WEST, "M");
	toReturn.put(ActorFacingDirection.EAST, "RRM");

	return toReturn;
    }

    private static Map<ActorFacingDirection, String> fillNegativeNegativeMap() {
	Map<ActorFacingDirection, String> toReturn = new EnumMap<>(ActorFacingDirection.class);

	toReturn.put(ActorFacingDirection.NORTH, "MLM");
	toReturn.put(ActorFacingDirection.SOUTH, "RMRM");
	toReturn.put(ActorFacingDirection.WEST, "MRM");
	toReturn.put(ActorFacingDirection.EAST, "LMLM");

	return toReturn;
    }
}