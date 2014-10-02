package org.maxgamer.game;

import java.awt.Image;
import java.util.HashSet;

public class Projectile extends Entity{
	private Entity shooter;
	private int damage;
	
	/**
	 * Creates a new projectile entity
	 * @param image The image to represent this projectile
	 * @param point The point to launch this projectile from
	 * @param shooter The entity who shot this projectile
	 * @param damage The damage this projectile should do to anything it hits.
	 */
	public Projectile(Image image, Point point, Entity shooter, int damage) {
		super(image, point);
		this.shooter = shooter;
		this.damage = damage;
	}
	
	/**
	 * Launches the projectile at the given point
	 * @param to The point to launch it at
	 * @param speed The speed of the projectile
	 */
	public void launch(Point to, double speed){
		if(to.x == this.point.x && to.y == this.point.y) throw new IllegalArgumentException("Target point may not be current point!");
		
		double x = to.x - this.point.x;
		double y = to.y - this.point.y;
		
		double absTotal = Math.abs(x)+Math.abs(y);
		if(absTotal != 0){
			double xRatio = x/absTotal;
			double yRatio = y/absTotal;
			
			this.setDirection(xRatio, yRatio);
			this.setSpeed(speed);
		}
	}
	
	public void onCollision(HashSet<Entity> entities){
		for(Entity e : entities){
			if(e instanceof Projectile){
				Projectile proj = (Projectile) e;
				
				proj.remove();
				this.remove();
				Explosion explosion = new Explosion(Game.image_explosions, point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
			else if(e instanceof Enemy){
				Enemy enemy = (Enemy) e;
				
				if(enemy.getHealth() <= 0){
					continue;
				}
				
				if(this.getShooter() == Game.player){
					Game.instance.addScore(Math.min(this.getDamage(), enemy.getHealth()));
				}
				
				enemy.takeHealth(this.getDamage());
				this.remove();
				
				Explosion explosion = new Explosion(Game.image_explosions, point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
			else if(e instanceof Player){
				Player p = (Player) e;
				
				if(p.getHealth() <= 0){
					continue;
				}
				
				p.takeHealth(this.getDamage());
				this.remove();
				
				Explosion explosion = new Explosion(Game.image_explosions, point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
		}
	}
	
	/**
	 * Increments this objects location according to its velocity.
	 */
	@Override
	public void tick(){
		super.tick();
		
		if(!this.isOnScreen()){
			Game.removeEntity(this);
		}
	}
	/**
	 * Returns the "owner" of this projectile
	 * @return The entity who created this projectile.
	 */
	public Entity getShooter(){
		return shooter;
	}
	/**
	 * The damage power of this projectile
	 * @return The damage power of this projectile.
	 */
	public int getDamage(){
		return damage;
	}
	@Override
	public String toString(){
		return "Projectile: " + this.point.toString();
	}
}