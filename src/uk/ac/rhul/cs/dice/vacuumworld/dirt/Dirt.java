package uk.ac.rhul.cs.dice.vacuumworld.dirt;

public class Dirt extends Obstacle {
	private boolean inInitialState;
	private int dropCycle;
	
	public Dirt(DirtAppearance appearance, boolean inInitialState, int dropCycle) {
		super(appearance);
		
		this.inInitialState = inInitialState;
		this.dropCycle = dropCycle;
	}
	
	@Override
	public DirtAppearance getExternalAppearance() {
		return (DirtAppearance) super.getExternalAppearance();
	}
	
	public boolean isInInitialState() {
		return this.inInitialState;
	}
	
	public int getDropCycle() {
		return this.dropCycle;
	}
	
	public Dirt duplicate() {
		DirtAppearance appearance = getExternalAppearance().duplicate();
		
		return new Dirt(appearance, this.inInitialState, this.dropCycle);
	}
}