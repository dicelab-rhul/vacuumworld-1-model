package uk.ac.rhul.cs.dice.vacuumworld;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public enum HandshakeCodes {
	VHVC,
	CHCV,
	CHCM,
	CHVM,
	MHMC,
	MHMV,
	CHMV;
	
	private static Map<String, HandshakeCodes> lookup = initLookupMap();
	private static Map<HandshakeCodes, String> reverseLookup = initReverseLookupMap();
	
	public static HandshakeCodes fromString(String s) {
		HandshakeCodes code = HandshakeCodes.lookup.get(s);
		
		if(code == null) {
			throw new IllegalArgumentException("Invalid handshake code representation: " + s + ".");
		}
		else {
			return code;
		}
	}
	
	private static Map<HandshakeCodes, String> initReverseLookupMap() {
		Map<HandshakeCodes, String> toReturn = new EnumMap<>(HandshakeCodes.class);
		
		toReturn.put(HandshakeCodes.VHVC, "VHVC");
		toReturn.put(HandshakeCodes.CHCV, "CHCV");
		toReturn.put(HandshakeCodes.CHCM, "CHCM");
		toReturn.put(HandshakeCodes.CHVM, "CHVM");
		toReturn.put(HandshakeCodes.MHMC, "MHMC");
		toReturn.put(HandshakeCodes.MHMV, "MHMV");
		toReturn.put(HandshakeCodes.CHMV, "CHMV");
		
		return toReturn;
	}

	private static Map<String, HandshakeCodes> initLookupMap() {
		Map<String, HandshakeCodes> toReturn = new HashMap<>();
		
		toReturn.put("VHVC", HandshakeCodes.VHVC);
		toReturn.put("CHCV", HandshakeCodes.CHCV);
		toReturn.put("CHCM", HandshakeCodes.CHCM);
		toReturn.put("CHVM", HandshakeCodes.CHVM);
		toReturn.put("MHMC", HandshakeCodes.MHMC);
		toReturn.put("MHMV", HandshakeCodes.MHMV);
		toReturn.put("CHMV", HandshakeCodes.CHMV);
		
		return toReturn;
	}

	@Override
	public String toString() {
		return HandshakeCodes.reverseLookup.get(this);
	}
}