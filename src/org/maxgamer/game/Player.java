package org.maxgamer.game;

import java.awt.Image;
import java.util.HashSet;

import org.maxgamer.game.Game.Weapon;

public class Player extends Entity{
	private int health = 150;
	private int damage = 90;
	
	private long lastAttack = 0;
	private long cooldown = 300;
	
	private Point weaponOffset = new Point(18, 5);
	private Weapon weapon = Weapon.SINGLE;
	
	public Player(Image image, Point point) {
		super(image, point);
		Main.health.setText("Health: " + this.health);
	}
	@Override
	public void tick(){
		super.tick();
		move();
	}
	public void setWeapon(Weapon w){
		this.weapon = w;
	}
	public Weapon getWeapon(){
		return this.weapon;
	}
	
	/**
	 * Cycles through to the next weapon in the players weapon queue
	 */
	public void nextWeapon(){
		int id = this.weapon.ordinal();
		int next = (id + 1) % (Weapon.values().length);
		
		this.setWeapon(Weapon.values()[next]);
	}
	
	/**
	 * Moves the player according to what keys are pressed.
	 * Also handles shooting and any other controls.
	 */
	public void move(){
		//Count the number of directions they're going in
		int directions = 0;
		
		if(Game.Control.UP.isPressed()){
			directions++;
		}
		if(Game.Control.DOWN.isPressed()){
			directions++;
		}
		if(Game.Control.LEFT.isPressed()){
			directions++;
		}
		if(Game.Control.RIGHT.isPressed()){
			directions++;
		}
		
		if(directions > 0){
			//The ratio at which to move horizontally
			double xRatio = 0;
			//The ratio at which to move vertically
			double yRatio = 0;
			
			//Their speed should be split amongst all directions
			double share = 1.0 / directions;
			
			if(Game.Control.UP.isPressed() && this.point.y > this.getSpeed()){
				yRatio -= share;
			}
			if(Game.Control.LEFT.isPressed() && this.point.x > this.getSpeed()){
				xRatio -= share;
			}
			if(Game.Control.DOWN.isPressed() && (this.point.y + this.image.getHeight(null)) < Game.instance.getGameHeight() - this.getSpeed()){
				yRatio += share;
			}
			if(Game.Control.RIGHT.isPressed() && (this.point.x + this.image.getWidth(null)) < Game.instance.getGameWidth() - this.getSpeed()){
				xRatio += share;
			}
			//Modifies the direction, NOT their speed!
			this.setDirection(xRatio, yRatio);
		}
		else{
			//Going nowhere fast.
			this.setDirection(0, 0);
		}
		
		//Shoot handling
		if(Game.Control.SPACE.isPressed() && this.canAttack()){
			this.shoot(10);
		}
		//Admin hack
		if(Game.Control.CTRL.isPressed()){
			this.cooldown -= 100;
			if(this.cooldown <= 0){
				this.cooldown = 100;
			}
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
				Projectile p = (Projectile) e;
				//Destroy the projectile
				p.remove();
				//Take away some hp
				this.takeHealth(p.getDamage());
				
				Explosion explosion = new Explosion(Game.image_explosions, e.point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
			else if(e instanceof Enemy){
				//When two enemies/player collide, take away their hp from each other.
				//The one with the most hp will survive, the other will die.
				Enemy enemy = (Enemy) e;
				
				if(enemy.getHealth() <= 0){
					continue;
				}
				
				int enemyHP = enemy.getHealth();
				
				enemy.takeHealth(this.getHealth());
				this.takeHealth(enemyHP);
				
				Explosion explosion = new Explosion(Game.image_explosions, e.point.clone());
				Game.addEntity(explosion);
				Game.sound_explosion.play();
			}
		}
	}
	
	/**
	 * Takes the given amount of health from this unit
	 * @param amount
	 */
	public void takeHealth(int amount){
		this.health -= amount;
		
		Main.health.setText("Health: " + this.health);
		
		if(this.health <= 0){
			this.remove();
			Main.health.setText("Health: 0");
			
			Game.sound_bigExplosion.play();
			
			Game.instance.reset();
		}
	}
	
	/**
	 * Returns the health of this unit
	 * @return The health of this unit
	 */
	public int getHealth(){
		return this.health;
	}
	
	@Override
	public String toString(){
		return "Player " + this.point.toString();
	}
	
	/**
	 * Fires a projectile at point p with the given speed
	 * @param p The projectile to fire
	 * @param speed the speed of the projectile
	 * @return The projectile fired
	 */
	public void shoot(double speed){
		Point from = this.point.clone().add(this.weaponOffset);
		
		if(this.weapon == Weapon.SINGLE){
			Projectile proj = new Projectile(Game.image_bullet, from, this, this.damage*2);
			Game.addEntity(proj);
			proj.launch(from.clone().add(50, 0), speed);
			
			Game.sound_single.play();
			
			this.lastAttack = System.currentTimeMillis();
		}
		else if(this.weapon == Weapon.RAPID){
			Projectile proj = new Projectile(Game.image_pulsar, from, this, (int) (this.damage / 3));
			Game.addEntity(proj);
			proj.launch(from.clone().add(50, 0), speed);
			
			Game.sound_rapid.play();
			
			this.lastAttack = System.currentTimeMillis() - (long) (this.cooldown / 1.35);
		}
		else if(this.weapon == Weapon.MULTI){
			//Launches the first projectile straight forward
			Projectile center = new Projectile(Game.image_multi, from.clone(), this, this.damage/3);
			Game.addEntity(center);
			center.launch(from.clone().add(50,0), speed);
			
			//Launch the upper projectile
			Projectile upper = new Projectile(Game.image_multi, from.clone(), this, this.damage/3);
			Game.addEntity(upper);
			upper.launch(from.clone().add(50, 3), speed);
			
			//Launch the lower projectile
			Projectile lower = new Projectile(Game.image_multi, from.clone(), this, this.damage/3);
			Game.addEntity(lower);
			lower.launch(from.clone().add(50, -3), speed);
			
			Game.sound_multi.play();
			
			this.lastAttack = System.currentTimeMillis();
		}
		else{
			System.out.println("Invalid weapon: " + weapon.toString());
		}
	}
	
	/**
	 * Returns true if this object can attack (due to cooldowns)
	 * @return true if this object can attack (due to cooldowns)
	 */
	public boolean canAttack(){
		return this.lastAttack + this.cooldown < System.currentTimeMillis();
	}
}