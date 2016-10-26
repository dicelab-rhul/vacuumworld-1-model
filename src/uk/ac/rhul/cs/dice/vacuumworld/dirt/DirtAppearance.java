package uk.ac.rhul.cs.dice.vacuumworld.dirt;

import uk.ac.rhul.cs.dice.gawl.interfaces.appearances.PassiveBodyAppearance;

public class DirtAppearance extends PassiveBodyAppearance {
	private DirtType dirtType;
	
	public DirtAppearance(String name, Double[] dimensions, DirtType type) {
		super(name, dimensions);
		this.dirtType = type;
	}

	@Override
	public String represent() {
		return this.dirtType.compactRepresentation();
	}

	public DirtType getDirtType() {
		return this.dirtType;
	}
	
	public void changeDirtType(DirtType newType) {
		this.dirtType = newType;
	}
	
	@Override
	public DirtAppearance duplicate() {
		return new DirtAppearance(getName(), getDimensions(), this.dirtType);
	}
}