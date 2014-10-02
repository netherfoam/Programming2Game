package org.maxgamer.game;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.HashSet;

public class Entity{
	/** The image representing this entity */
	protected Image image;
	/** The top left hand corner of this entity */
	protected Point point;
	
	/** The velocity of this object on the horizontal axes */
	protected double xVelocity = 0;
	/** The velocity of this object on the vertical axes */
	protected double yVelocity = 0;
	/** If my velocities are set to going nowhere, I have to remember what my speed was! */
	private double speed = 0;
	
	/** The ratio of X:Y speed for this entity */
	protected double xRatio = 0.5;
	protected double yRatio = 0.5;
	
	/**
	 * Creates a new entity that will be displayed on screen.
	 * @param image The image to represent the entity with
	 * @param point The location of the top-left hand corner of the image.
	 */
	public Entity(Image image, Point point){
		this.point = point;
		this.image = image;
	}
	
	/**
	 * Teleports this entity to a given point.
	 * Makes a copy of the reference, so it is safe to edit after.
	 * @param p The point to teleport to.
	 */
	public void teleport(Point p){
		this.point = p.clone();
	}
	/**
	 * Teleports this entity to the given coords
	 * @param x The x coordinate
	 * @param y The y coordinate
	 */
	public void teleport(double x, double y){
		this.teleport(new Point(x,y));
	}
	
	/**
	 * Changes the direction of this object.
	 * This also recalculates it's internal velocity.
	 * @param xRatio The amount to move in the x direction
	 * @param yRatio The amount to move in the y direction.
	 * 
	 * Note that these are RATIOS! They should add up to 1!
	 * Any values that don't add up to 1 (Rounding is fine
	 * though) will cause weird speeds!
	 */
	public void setDirection(double xRatio, double yRatio){
		this.xRatio = xRatio;
		this.yRatio = yRatio;
		
		this.xVelocity = xRatio * speed;
		this.yVelocity = yRatio * speed;
	}
	
	/**
	 * Returns the speed of this entity
	 * @return The speed of this entity.
	 */
	public double getSpeed(){
		return this.speed;
	}
	
	/**
	 * Modifies this objects speed without changing direction
	 * @param speed The new speed
	 */
	public void setSpeed(double speed){
		this.xVelocity = this.xRatio * speed;
		this.yVelocity = this.yRatio * speed;
		
		this.speed = speed;
	}
	
	/**
	 * Notifies the object that a tick has passed, and that it should do something now if it has to.
	 */
	public void tick(){
		if(this.xVelocity != 0 || this.yVelocity != 0){
			this.point.add(this.xVelocity, this.yVelocity);
			//Collision is fine here because:
			//An object has to move in order for a collision to occur.
			//If each object checks its collisions after moving, then
			//Stationery objects can be avoided and do not require 
			//Checking!
			HashSet<Entity> collided = this.getCollision();
			
			if(collided.isEmpty()) return; //Nothing to see.
			onCollision(collided);
		}
	}
	
	/**
	 * Called whenever there is a collision of objects.
	 * A collision with a single entity may occur many
	 * times if neither entity is removed!
	 */
	public void onCollision(HashSet<Entity> entities){
	}
	
	/**
	 * Returns a list of all entities colliding with this object.  Returns an empty list if none.
	 * @return a list of all entities colliding with this object.  An empty list if none.
	 */
	public HashSet<Entity> getCollision(){
		Rectangle r1 = new Rectangle((int) this.point.x, (int) this.point.y, this.image.getWidth(null), this.image.getHeight(null));
		
		HashSet<Entity> collided = new HashSet<Entity>(1);
		
		for(Entity e : Game.getEntities()){
			if(e instanceof Explosion){
				continue;
			}
			
			//We can't collide with our own projectile!
			if(e instanceof Projectile){
				Projectile p = (Projectile) e;
				if(p.getShooter() == this){
					continue;
				}
			}
			//Likewise, we can't collide with our own creater!
			if(this instanceof Projectile){
				Projectile p = (Projectile) this;
				if(p.getShooter() == e){
					continue;
				}
			}
			//Our own projectiles shouldn't collide!
			if((this instanceof Projectile) && (e instanceof Projectile)){
				Projectile p1 = (Projectile) this;
				Projectile p2 = (Projectile) e;
				
				if(p1.getShooter() == p2.getShooter()){
					continue;
				}
			}
			Rectangle r2 = new Rectangle((int) e.point.x, (int) e.point.y, e.image.getWidth(null), e.image.getHeight(null));
			
			if(r1.intersects(r2)){
				if(e == this) continue; //We'll always collide with ourselves
				// If I could be bothered, I should add pixel perfect collision
				// But I'm lazy...
				collided.add(e);
			}
		}
		
		return collided;
	}
	
	/**
	 * Called by the system
	 */
	public void repaint(Graphics2D g2){
		g2.drawImage(this.image, (int) point.x, (int) point.y, null);
	}
	
	/**
	 * Returns true if the image for this object is visible.
	 * @return true if the image for this object is visible.
	 * 
	 * An entity is 'on the screen' if it is even partially off.
	 */
	public boolean isOnScreen(){
		Rectangle rectangle = new Rectangle((int)this.point.x, (int) this.point.y, this.image.getWidth(null), this.image.getHeight(null));
		
		if(rectangle.intersects(Game.rectangle)){
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this entity and its image are totally on the screen
	 * @return true if this entity and its image are totally on the screen
	 * 
	 * Slight variation to isOnScreen().
	 */
	public boolean isTotallyOnScreen(){
		Rectangle rectangle = new Rectangle((int)this.point.x, (int) this.point.y, this.image.getWidth(null), this.image.getHeight(null));
		
		//If this rectangle is the same size as the overlapping rectangle, we must be totally in the other one!
		if(rectangle.intersection(Game.rectangle).getSize().equals(rectangle.getSize())){
			return true;
		}
		return false;
	}
	
	/**
	 * Removes this entity from the game world
	 */
	public void remove(){
		Game.removeEntity(this);
	}
	
	public String toString(){
		return "Entity: " + this.point.toString();
	}
}