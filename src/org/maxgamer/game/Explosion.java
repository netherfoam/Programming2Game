package org.maxgamer.game;

import java.awt.Image;

public class Explosion extends Entity{
	/** The stage for this explosion */
	private int stage = 0;
	/** The array of images that make up this explosion */
	private Image[] images;
	
	/**
	 * Creates a new explosion.  Must add this to the list of entities.
	 * @param images The array of images that should animate as this explodes
	 * @param point The top left hand corner for the explosion
	 */
	public Explosion(Image[] images, Point point) {
		super(images[0], point);
		
		this.images = images;
	}
	
	@Override
	public void tick(){
		super.tick();
		
		this.stage++;
		if(this.stage < images.length){
			//Load the next image
			this.image = images[this.stage];
		}
		else{
			//End of explosion!
			this.remove();
		}
	}
}