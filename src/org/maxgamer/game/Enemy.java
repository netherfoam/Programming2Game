package org.maxgamer.game;

import java.awt.Image;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.maxgamer.game.Game.Weapon;

public class Enemy extends Entity{
	private int health = 100;
	private int damage = 40;
	
	private long lastAttack = 0;
	private long cooldown = 2500;
	
	private Point destination;
	private List<Point> waypoints = new LinkedList<Point>();
	
	private Point weaponOffset = new Point(0, 0);
	
	/** The chance this enemy will attack when offered to */
	private double attackChance = 0.05;
	
	private Weapon weapon = Weapon.SINGLE;
	
	/**
	 * Creates a new enemy, that will just sit there.
	 * @param image The image that represents this enemy.
	 * @param point The top left hand corner of this enemy
	 * @param health The health of this enemy
	 * @param damage The damage this enemies projectiles do
	 */
	public Enemy(Image image, Point point, int health, int damage) {
		super(image, point);
		this.health = health;
		this.damage = damage;
		
		this.setSpeed(3);
	}
	
	public void setWeapon(Weapon w){
		this.weapon = w;
	}
	public Weapon getWeapon(){
		return this.weapon;
	}
	
	/**
	 * Creates an enemy with a list of waypoints to go by.  Also begins the route.
	 * @param image The image that represents this enemy.
	 * @param point The top left hand corner of this enemy
	 * @param health The health of this enemy
	 * @param damage The damage this enemies projectiles do
	 * @param waypoints The waypoints this entity should pass by.
	 */
	public Enemy(Image image, Point point, int health, int damage, List<Point> waypoints) {
		this(image, point, health, damage);
		
		this.waypoints = waypoints;
		
		this.loadWaypoint();
		this.setSpeed(3);
	}
	
	public Enemy clone(){
		Enemy clone = new Enemy(image, point.clone(), health, damage);
		clone.setSpeed(this.getSpeed());
		clone.setAttackChance(this.attackChance);
		clone.setAttackCooldown(this.cooldown);
		
		clone.xRatio = this.xRatio;
		clone.yRatio = this.yRatio;
		clone.xVelocity = this.xVelocity;
		clone.yVelocity = this.yVelocity;
		
		clone.weapon = this.weapon;
		
		clone.weaponOffset = this.weaponOffset.clone();
		
		if(this.destination != null){
			clone.destination = this.destination.clone();
		}
		
		for(Point point : this.waypoints){
			clone.addWaypoint(point.clone());
		}
		
		return clone;
	}
	
	/**
	 * Modifies the cooldown for this enemy
	 * @param cooldown The new cooldown (milliseconds) for this enemy to attack
	 */
	public void setAttackCooldown(long cooldown){
		this.cooldown = cooldown;
	}
	
	public void setAttackChance(double chance){
		this.attackChance = chance;
	}
	
	/**
	 * Loads the next waypoint from this enemy's path.
	 * @return True if there was a waypoint, false if there wasn't any left.
	 */
	public boolean loadWaypoint(){
		if(this.waypoints.size() > 0){
			Point p = this.waypoints.remove(0);
			this.setDestination(p, this.getSpeed());
			return true;
		}
		return false;
	}
	
	/**
	 * Adds a given waypoint to the end of this enemy's waypoints
	 * @param wp The waypoint to add
	 */
	public void addWaypoint(Point wp){
		this.waypoints.add(wp);
		if(this.hasReachedDestination()){
			this.loadWaypoint();
		}
	}
	
	@Override
	public void onCollision(HashSet<Entity> entities){
		super.onCollision(entities);
		
		if(this.getHealth() <= 0){
			return;
		}
		
		for(Entity e : entities){
			if(e instanceof Projectile){
				Projectile proj = (Projectile) e;
				
				if(proj.getShooter() == Game.player){
					Game.instance.addScore(Math.min(proj.getDamage(), this.getHealth()));
				}
				
				proj.remove();
				this.takeHealth(proj.getDamage());
				
				Explosion explosion = new Explosion(Game.image_explosions, e.point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
			else if(e instanceof Enemy){
				Enemy enemy = (Enemy) e;
				
				if(enemy.getHealth() <= 0){
					continue;
				}
				
				int dmg = Math.max(enemy.getHealth(), this.getHealth());
				
				enemy.takeHealth(dmg);
				this.takeHealth(dmg);
				
				Explosion explosion = new Explosion(Game.image_explosions, e.point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
			else if(e instanceof Player){
				Player p = (Player) e;
				
				if(p.getHealth() <= 0){
					continue;
				}
				
				int dmg = Math.max(p.getHealth(), this.getHealth());
				
				p.takeHealth(dmg);
				this.takeHealth(dmg);
				
				Explosion explosion = new Explosion(Game.image_explosions, e.point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
		}
	}
	
	/**
	 * Returns true if this object can attack (due to cooldowns)
	 * @return true if this object can attack (due to cooldowns)
	 */
	public boolean canAttack(){
		return this.lastAttack + this.cooldown < System.currentTimeMillis();
	}
	
	/**
	 * Returns the health of this enemy
	 * @return the health of this enemy
	 */
	public int getHealth(){
		return this.health;
	}
	/**
	 * Reduces this enemies health and deletes it if its now <= 0
	 * @param amount The amount to take away
	 */
	public void takeHealth(int amount){
		this.health -= amount;
		if(health <= 0){
			Game.removeEntity(this);
		}
	}
	
	@Override
	public void tick(){
		super.tick();
		//TODO: Why are enemies with 0 velocity teleporting to (0,0)?
		if(this.hasReachedDestination()){
			if(!this.loadWaypoint()){
				if(!this.isOnScreen()){
					Game.removeEntity(this);
					return;
				}
				this.setDirection(0,0);
			}
		}
		
		if(this.isOnScreen()){
			//Shoot
			if(Game.r.nextDouble() < this.attackChance && this.canAttack()){
				this.shoot(Game.player.point, 10);
			}
		}
	}
	
	/**
	 * Returns true if they're as close as they're going to get to their destination
	 * @return true if they're as close as they're going to get to their destination
	 * If their destination is null, this will return true.
	 */
	public boolean hasReachedDestination(){
		if(this.destination == null) return true;
		
		Point now = this.point;
		Point next = new Point((int) (this.point.x + this.xVelocity),(int) (this.point.y + this.yVelocity));
		
		Point dest = destination;
		
		if(now.distanceSq(dest) < next.distanceSq(dest)){
			//The next step makes us further away!
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Launches the projectile at the given point
	 * @param to The point to launch it at
	 * @param speed The speed of the projectile
	 */
	public void setDestination(Point to, double speed){
		double x = to.x - this.point.x;
		double y = to.y - this.point.y;
		
		double absTotal = Math.abs(x)+Math.abs(y);
		if(absTotal != 0){
			double xRatio = x/absTotal;
			double yRatio = y/absTotal;
			
			this.setDirection(xRatio, yRatio);
			this.setSpeed(speed);
			this.destination = to;
		}
		else{
			this.setDirection(0, 0);
		}
	}
	
	/**
	 * Adjusts the weapon offset of this enemy. AKA, moves where the 'gun' is on the enemy.
	 * @param x The x difference from the top-left hand corner
	 * @param y The y difference from the top-left hand corner
	 */
	public void setWeaponOffset(double x, double y){
		this.weaponOffset.x = x;
		this.weaponOffset.y = y;
	}
	
	/**
	 * Fires a projectile at point p with the given speed
	 * @param p The projectile to fire
	 * @param speed the speed of the projectile
	 * @return The projectile fired
	 */
	public void shoot(Point p, double speed){
		Point from = this.point.clone().add(this.weaponOffset);
		
		if(this.weapon == Weapon.SINGLE){
			Projectile proj = new Projectile(Game.image_bullet, from, this, this.damage);
			Game.addEntity(proj);
			proj.launch(p, speed);
			
			this.lastAttack = System.currentTimeMillis();
		}
		else if(this.weapon == Weapon.RAPID){
			Projectile proj = new Projectile(Game.image_pulsar, from, this, (int) (this.damage / 1.35));
			Game.addEntity(proj);
			proj.launch(p, speed);
			
			this.lastAttack = System.currentTimeMillis() - (long) (this.cooldown / 2);
		}
		else if(this.weapon == Weapon.MULTI){
			double x = from.x - p.x;
			double y = from.y - p.y;
			
			double absTotal = Math.abs(x) + Math.abs(y);
			
			double xRatio = x/absTotal;
			double yRatio = y/absTotal;
			
			Projectile center = new Projectile(Game.image_multi, from, this, this.damage/2);
			Game.addEntity(center);
			center.launch(p.clone(), speed);
			
			Projectile upper = new Projectile(Game.image_multi, from.clone().add(xRatio * -20, yRatio * 20), this, this.damage/2);
			Game.addEntity(upper);
			upper.launch(p.clone().add(xRatio * -50, yRatio * 50), speed);
			
			Projectile lower = new Projectile(Game.image_multi, from.clone().add(20 * yRatio, -20 * xRatio), this, this.damage/2);
			Game.addEntity(lower);
			lower.launch(p.clone().add(xRatio * 50, yRatio * -50), speed);
			
			this.lastAttack = System.currentTimeMillis();
		}
		else{
			System.out.println("Invalid weapon: " + weapon.toString());
		}
	}
	/**
	 * Convenience method for shooting a projectile.
	 * Defaults to a speed of 5 for the projectile.
	 * @param p The point to target
	 * @return The projectile fired
	 */
	public void shoot(Point p){
		shoot(p, 5);
	}
	
	public String toString(){
		return "Enemy: " + this.point.toString();
	}
}