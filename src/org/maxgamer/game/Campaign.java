package org.maxgamer.game;

import java.util.LinkedList;

import org.maxgamer.game.Game.Weapon;

public class Campaign{
	/** All available waves */
	private LinkedList<Wave> waves = new LinkedList<Wave>();
	/** The last time a wave was spawned (milliseconds) */
	private long lastWave = 0;
	/** The number of waves so far */
	private int waveId;
	
	/**
	 * Generates a set of waypoints in a backwards Z shape
	 * Direction <------
	 * Direction (Down)
	 * Direction ------>
	 * @return a set of waypoints in a backwards Z shape
	 */
	public static LinkedList<Point> Z2_Waypoints(){
		LinkedList<Point> points = new LinkedList<Point>();
		
		int width = Game.instance.getGameWidth();
		int height = Game.instance.getGameHeight();
		
		points.add(new Point(width * 1, height * 0.1));
		points.add(new Point(width * 0.1, height * 0.1));
		points.add(new Point(width * 0.8, height * 0.8));
		points.add(new Point(width * -0.1, height * 0.8));
		return points;
	}
	
	/**
	 * Returns a set of waypoints in a Z shape
	 * Direction ------>
	 * Direction (Down)
	 * Direction <------
	 * @return a set of waypoints in a Z shape
	 */
	public static LinkedList<Point> Z1_Waypoints(){
		LinkedList<Point> points = new LinkedList<Point>();
		
		int width = Game.instance.getGameWidth();
		int height = Game.instance.getGameHeight();
		
		points.add(new Point(width * 0.1, height * 0.1));
		points.add(new Point(width * 0.8, height * 0.1));
		points.add(new Point(width * 0.1, height * 0.8));
		points.add(new Point(width * 1.1, height * 0.8));
		
		return points;
	}
	
	/**
	 * Generates a set of points travelling across the screen
	 * Direction ----->
	 * @return a set of points travelling across the screen
	 */
	public static LinkedList<Point> lineTop(){
		LinkedList<Point> points = new LinkedList<Point>();
		
		int width = Game.instance.getGameWidth();
		int height = Game.instance.getGameHeight();
		
		points.add(new Point(width * -0.1, height * 0.1));
		points.add(new Point(width * 1.1, height * 0.1));
		return points;
	}
	/**
	 * Generates a set of points travelling across the screen
	 * Direction <-----
	 * @return a set of points travelling across the screen
	 */
	public static LinkedList<Point> lineBottom(){
		LinkedList<Point> points = new LinkedList<Point>();
		
		int width = Game.instance.getGameWidth();
		int height = Game.instance.getGameHeight();
		
		points.add(new Point(width * 1.1, height * 0.8));
		points.add(new Point(width * -1.2, height * 0.8));
		
		return points;
	}
	/**
	 * Generates a set of points travelling across the screen and back
	 * Direction ------>
	 * Direction <------
	 * @return a set of points travelling across the screen and back
	 */
	public static LinkedList<Point> lineTopAndBottom(){
		LinkedList<Point> points = lineTop();
		points.addAll(lineBottom());
		return points;
	}
	
	/**
	 * Generates a LinkedList of all the waypoints given.
	 * Convenience method.
	 * @param points The points to use in order
	 * @return The Listof waypoints.
	 */
	public static LinkedList<Point> getWaypoints(Point ... points){
		LinkedList<Point> waypoints = new LinkedList<Point>();
		for(Point p : points){
			waypoints.add(p);
		}
		return waypoints;
	}
	
	/**
	 * Creates a new "campaign" - Building a set of predefined
	 * "waves" for the game.
	 */
	public Campaign(){
		int width = Game.instance.getGameWidth();
		int height = Game.instance.getGameHeight();
		
		Enemy prototype;
		Wave wave;
		//Wave 1 -- Vodaphone 1
		prototype = new Enemy(Game.image_enemy, new Point(width, height * 0.1), 100, 20, Z2_Waypoints());
		wave = new Wave(2, 750, prototype);
		this.waves.add(wave);
		
		//Wave 2 -- Vodaphone 2 (Normal)
		prototype = new Enemy(Game.image_enemy, new Point(width * 1.1,height * 0.8), 100, 40, lineBottom());
		prototype.setSpeed(6);
		prototype.setWeapon(Weapon.SINGLE);
		wave = new Wave(5, 1200, prototype);
		this.waves.add(wave);
		
		//Wave 3 -- Telstra Tower (Pulsar)
		prototype = new Enemy(Game.image_tower, new Point(width,height * 0.7), 800, 30, getWaypoints(new Point(400, 300)));
		prototype.setAttackChance(1);
		prototype.setAttackCooldown(200);
		prototype.setWeaponOffset(40, 25);
		prototype.setWeapon(Weapon.RAPID);
		wave = new Wave(1, 300, prototype);
		this.waves.add(wave);
		
		//Wave 4 -- Optus 
		prototype = new Enemy(Game.image_target, new Point(width * -0.2, height * 0.1), 300, 20, lineTopAndBottom());
		prototype.setSpeed(6);
		wave = new Wave(7, 700, prototype);
		this.waves.add(wave);
		
		//Wave 5 -- Vodaphone 1 (Rapid)
		prototype = new Enemy(Game.image_enemy, new Point(width, height * 0.1), 50, 20, Z2_Waypoints());
		prototype.setSpeed(4);
		prototype.setWeapon(Weapon.RAPID);
		wave = new Wave(10, 600, prototype);
		this.waves.add(wave);
		
		//Wave 6 -- Vodaphone 2 (Rapid)
		prototype = new Enemy(Game.image_enemy, new Point(width * -0.1, height * 0.1), 50, 20, Z1_Waypoints());
		prototype.setSpeed(4);
		prototype.setWeapon(Weapon.RAPID);
		wave = new Wave(10, 600, prototype);
		this.waves.add(wave);
		
		//Wave 7 -- Vodaphone 3 (Normal)
		prototype = new Enemy(Game.image_enemy, new Point(width, height * 0.1), 50, 20, Z2_Waypoints());
		prototype.setSpeed(4);
		wave = new Wave(10, 600, prototype);
		this.waves.add(wave);
		
		//Wave 8 -- Telstra Tower (Multi)
		prototype = new Enemy(Game.image_tower, new Point(width,height * 0.7), 1500, 30, getWaypoints(new Point(400, 300)));
		prototype.setSpeed(1);
		prototype.setWeapon(Weapon.MULTI);
		prototype.setAttackChance(1);
		prototype.setAttackCooldown(100);
		prototype.setWeaponOffset(40, 25);
		wave = new Wave(1, 300, prototype);
		this.waves.add(wave);
		
		//Wave 9
		prototype = new Enemy(Game.image_player, new Point(width, height * 0.1), 200, 20, Z2_Waypoints());
		prototype.setSpeed(5);
		prototype.setWeapon(Weapon.MULTI);
		prototype.setAttackChance(0.8);
		prototype.setAttackCooldown(1500);
		wave = new Wave(3, 1000, prototype);
		this.waves.add(wave);
		
		//Wave 10
		prototype = new Enemy(Game.image_enemy, new Point(width * 0.7, height * -0.1), 180, 20, getWaypoints(new Point(width * 0.7, height * 1.1)));
		prototype.setSpeed(4);
		prototype.setAttackCooldown(1000);
		wave = new Wave(10, 600, prototype);
		this.waves.add(wave);
		
		//Wave 11
		prototype = new Enemy(Game.image_enemy, new Point(width * 0.5, height * -0.1), 180, 20, getWaypoints(new Point(width * 0.5, height * 1.1)));
		prototype.setSpeed(4);
		prototype.setAttackCooldown(1000);
		wave = new Wave(10, 600, prototype);
		this.waves.add(wave);
		
		//Wave 12
		prototype = new Enemy(Game.image_target, new Point(width * 0.6, height * -0.2), 100, 20, getWaypoints(new Point(width * 0.6, height * 0.5), new Point(width * 1.2, height * 0.5)));
		prototype.setSpeed(6);
		prototype.setWeapon(Weapon.MULTI);
		wave = new Wave(7, 700, prototype);
		this.waves.add(wave);
		
		//Wave 13 (What, you're still alive?)
		prototype = new Enemy(Game.image_tower, new Point(width,height * 0.7), 2000, 30, getWaypoints(new Point(400, 300)));
		prototype.setSpeed(0.5);
		prototype.setWeapon(Weapon.RAPID);
		prototype.setAttackChance(1);
		prototype.setAttackCooldown(80);
		prototype.setWeaponOffset(40, 25);
		wave = new Wave(1, 300, prototype);
		this.waves.add(wave);
	}
	
	/**
	 * Returns true if the next wave is ready to run
	 * @return true if the next wave is ready to run
	 */
	public boolean isReady(){
		if(this.lastWave + 3000 < System.currentTimeMillis() && !Game.instance.isSpawning() && !Game.instance.isPaused()){
			for(Entity e : Game.getEntities()){
				if(e instanceof Enemy){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Runs the next available wave.
	 */
	public void nextWave(){
		if(!this.waves.isEmpty()){
			this.waveId++;
			this.lastWave = System.currentTimeMillis();
			this.waves.pop().run();
		}
		else{
			Game.instance.reset();
		}
	}
	
	/**
	 * Returns the current wave ID
	 * @return the current wave ID
	 */
	public int getWaveId(){
		return this.waveId;
	}
	
	/**
	 * Represents a wave of enemies
	 */
	class Wave{
		int enemies;
		Enemy prototype;
		int msInterval;
		
		/**
		 * Creates a new wave of enemies
		 * @param enemies The number of enemies to spawn
		 * @param msInterval The interval (milliseconds) to spawn enemies at
		 * @param prototype The prototype enemy to clone and to base all enemies off of
		 */
		public Wave(int enemies, int msInterval, Enemy prototype){
			this.enemies = enemies;
			this.msInterval = msInterval;
			this.prototype = prototype.clone();
		}
		
		/**
		 * Starts this wave.
		 */
		public void run(){
			Game.instance.spawnEnemies(enemies, msInterval, prototype);
		}
	}
}