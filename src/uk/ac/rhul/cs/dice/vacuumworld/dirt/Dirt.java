package uk.ac.rhul.cs.dice.vacuumworld.dirt;

public class Dirt extends Obstacle {

	public Dirt(DirtAppearance appearance) {
		super(appearance);
	}
	
	@Override
	public DirtAppearance getExternalAppearance() {
		return (DirtAppearance) super.getExternalAppearance();
	}
}