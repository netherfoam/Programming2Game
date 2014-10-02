package org.maxgamer.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

import com.hazelcast.util.ConcurrentHashSet;

@SuppressWarnings("serial")
public class Game extends JPanel implements KeyListener{
	/** A set of all entities in the game */
	private static ConcurrentHashSet<Entity> entities = new ConcurrentHashSet<Entity>();
	
	/** Keys that are pressed down at any time */
	private static HashSet<Integer> pressed = new HashSet<Integer>(4);
	
	/** For generating random numbers */
	public static Random r = new Random();
	
	/** Number of ticks per second. Increasing this up speeds up gameplay */
	public final int TICKS_PER_SECOND = 30;
	/** The number of frames per second - This defines smoothness */
	public final int FRAMES_PER_SECOND = 30;
	
	/** The actual player entity */
	public static Player player;
	
	/** Player's image */
	public static Image image_player;
	
	/** Enemy images */
	public static Image image_enemy;
	public static Image image_target;
	public static Image image_tower;
	
	/** Bullet images */
	public static Image image_bullet;
	public static Image image_pulsar;
	public static Image image_multi;
	
	/** Explosion image */
	public static Image[] image_explosions;
	
	/** The timer for ticking, spawning and painting */
	public static Timer timer = new Timer();
	
	/** Increased every tick - isn't accurate in realtime though! */
	public long time = 0;
	
	/** Shortcut reference to this */
	public static Game instance;
	
	/** The campaign containing wave controls */
	public static Campaign campaign;
	
	/** The rectangle representing this game panel */
	public static Rectangle rectangle;
	
	/** Sound files */
	public static Sound sound_single;
	public static Sound sound_rapid;
	public static Sound sound_multi;
	public static Sound sound_bigExplosion;
	public static Sound sound_explosion;
	
	/** Background music */
	public static Clip sound_background;
	
	/** Width of the game panel */
	private static int GAME_WIDTH;
	private static int GAME_HEIGHT;
	
	/** True if everything is on hold */
	private boolean pause = false;
	
	/** Player's score */
	private int score;
	
	/** The position in the scrolling background */
	private int skyline_x = 0;
	/** The background image */
	private static BufferedImage image_skyline;
	
	/** The spawner object that spawns..objects...*/
	private Spawner spawner;
	
	/** The tip box */
	public String tip = "";
	
	/** Player controls */
	enum Control{
		SPACE(32),
		UP(87),
		DOWN(83),
		LEFT(65),
		RIGHT(68),
		CTRL(17),
		ALT(18),
		SHIFT(16),
		GRAVE(192),
		P(80);
		
		private int id;
		private Control(int id){
			this.id = id;
		}
		/**
		 * @return The keycode value of this key
		 */
		public int getValue(){
			return this.id;
		}
		public boolean isPressed(){
			return Game.isPressed(id);
		}
	}
	
	/** Weapon types */
	enum Weapon{
		SINGLE,
		RAPID,
		MULTI;
	}
	
	/**
	 * Starts a new game.  
	 */
	public Game(int width, int height){
		instance = this;
		
		GAME_WIDTH = width;
		GAME_HEIGHT = height;
		super.setSize(width, height);
		rectangle = new Rectangle(0, 0, GAME_WIDTH, GAME_HEIGHT);
		
		this.setBackground(Color.LIGHT_GRAY);
		
		System.out.println("Game started");
		
		load();
		
		reset();
		pause();
		
		this.setFocusable(true);
		this.addKeyListener(this);
	}
	
	/** The width of the game panel */
	public int getGameWidth(){
		return GAME_WIDTH;
	}
	/** The height of the game panel */
	public int getGameHeight(){
		return GAME_HEIGHT;
	}
	
	/** Loads all resources from disc */
	public void load(){
		try{
			image_player = ImageIO.read(new File("player.png"));
			image_enemy = ImageIO.read(new File("enemy.png"));
			image_target = ImageIO.read(new File("target.png"));
			image_tower = ImageIO.read(new File("tower.png"));
			
			image_bullet = ImageIO.read(new File("bullet.png"));
			image_pulsar = ImageIO.read(new File("pulsar.png"));
			image_multi = ImageIO.read(new File("multi.png"));
			
			image_skyline = ImageIO.read(new File("skyline.png"));
			
			BufferedImage explosion = ImageIO.read(new File("explosion.png"));
			
			int rows = 5;
			int columns = 4;
			
			int width = 40;
			int height = 30;
			
			image_explosions = new Image[rows * columns];
			
			for(int y = 0; y < rows; y++){
				for(int x = 0; x < columns; x++){
					image_explosions[(y*columns) + x] = explosion.getSubimage(x*width, y*height, width, height);
				}
			}
			
			sound_single = new Sound("bullet.wav");
			sound_multi = new Sound("multi.wav");
			sound_rapid = new Sound("pulsar.wav");
			
			sound_explosion = new Sound("explosion.wav");
			sound_bigExplosion = new Sound("bigexplosion.wav");
			
			sound_background = AudioSystem.getClip();
			sound_background.open(AudioSystem.getAudioInputStream(new File("Incoming_Attack.wav")));
		}
		catch(IOException e){
			e.printStackTrace();
			return;
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/** Resets the game and pauses it!*/
	public void reset(){
		//Throw away the old game
		timer.cancel();

		pause();
		
		if(spawner != null){
			spawner.setRemaining(0);
			spawner = null;
		}
		
		entities.clear();
		
		this.repaint();
		
		setTime(0);
		setScore(0);
		sound_background.setFramePosition(0);
		
		//Setup the new
		campaign = new Campaign();
		
		player = new Player(image_player, new Point(this.getGameWidth()*0.2, this.getGameHeight()*0.5));
		player.setSpeed(5);
		player.setWeapon(Weapon.MULTI);
		entities.add(player);
	}
	
	public boolean isPaused(){
		return this.pause;
	}
	
	/**
	 * Adds an amount to the players score
	 * @param amount The amount to add
	 */
	public void addScore(int amount){
		this.setScore(this.getScore() + amount);
	}
	/**
	 * Sets a players score, updating the display too
	 * @param amount The amount to set
	 */
	public void setScore(int amount){
		this.score = amount;
		Main.score.setText("Score: " + this.score);
	}
	/**
	 * Returns the players score
	 * @return the players score
	 */
	public int getScore(){
		return this.score;
	}
	
	/**
	 * Sets the curent game time
	 * @param time The time to set it to in milliseconds
	 */
	public void setTime(long time){
		this.time = time;
		//Only want seconds
		Main.time.setText("Time: " + (this.time/1000));
	}
	/**
	 * Increments the games time by a given amount
	 * @param time The time to increment by in milliseconds
	 */
	public void addTime(int time){
		this.setTime(this.time + time);
		this.skyline_x += 1;
	}
	/**
	 * The time in milliseconds since the game started
	 * @return The time in milliseconds since the game started
	 */
	public long getTime(){
		return this.time;
	}
	
	/** Pauses the game */
	public void pause(){
		pause = true;
		
		timer.cancel();
		timer.purge();
		
		sound_background.stop();
		
		tip = "Movement: WASD\nShoot: Spacebar\nNext Weapon: Shift\nPause: P\nPress any key to continue...";
		this.repaint();
	}
	
	/** Plays the game (e.g. after a pause) */
	public void play(){
		pause = false;
		timer = new Timer();
		timer.scheduleAtFixedRate(new Ticker(), 0, 1000/TICKS_PER_SECOND);
		timer.scheduleAtFixedRate(new Painter(), 0, 1000/FRAMES_PER_SECOND);
		
		if(spawner != null){
			spawner = spawner.clone();
			timer.scheduleAtFixedRate(spawner, spawner.getInterval(), spawner.getInterval());
		}
		
		sound_background.start();
		tip = "";
	}
	
	/**
	 * Notifies all entities that a tick has passed!
	 */
	public void tick(){
		//Copies the list as to avoid concurrent modification issues
		//Eg. Say an entity wants to remove itself after it dies.
		HashSet<Entity> tempEntities = new HashSet<Entity>(entities);
		
		for(Entity e : tempEntities){
			e.tick();
		}
		
		//Increment our time variable
		this.addTime(1000/TICKS_PER_SECOND);
		
		if(campaign.isReady()){
			campaign.nextWave();
		}
		
	}
	
	/**
	 * Adds a new entity to the world
	 * @param e The entity to add.
	 */
	public static void addEntity(Entity e){
		entities.add(e);
	}
	/**
	 * Removes an entity from the world.
	 * @param e The entity to remove.
	 */
	public static void removeEntity(Entity e){
		entities.remove(e);
	}
	
	/**
	 * Returns a HashSet of all entities in the world.
	 * @return a HashSet of all entities in the world.
	 */
	public static ConcurrentHashSet<Entity> getEntities(){
		return entities;
	}
	
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		
		drawBackground(g2);

		for(Entity e : entities){
			e.repaint(g2);
		}
		
		g2.setFont(new Font("Roman", Font.ITALIC, 18));
		g2.setColor(new Color(255, 170, 25, 90));
		String[] tips = tip.split("\n");
		for(int i = 0; i < tips.length; i++){
			g2.drawString(tips[i], 200, 250 + i * 18);
		}
		
		g2.dispose();
		g.dispose();
	}
	
	/**
	 * Paints the background. If it has run out of image, it loops over the same image again
	 * @param g2 The graphics object to paint with.
	 */
	public void drawBackground(Graphics2D g2){
		if(skyline_x >= image_skyline.getWidth() - 2){
			//We've reached the absolute end of the skyline - Restart!
			skyline_x = 0;
		}
		
		if(image_skyline.getWidth() >= this.skyline_x + GAME_WIDTH){
			//We only need the skyline once
			Image background = image_skyline.getSubimage(this.skyline_x, 0, GAME_WIDTH, GAME_HEIGHT);
			g2.drawImage(background, 0, 0, null);
		}
		else{
			//We've run out of image!
			
			//Okay, get what we can cut out the remainder of the old image
			Image background1 = image_skyline.getSubimage(this.skyline_x, 0, image_skyline.getWidth() - this.skyline_x, GAME_HEIGHT);
			g2.drawImage(background1, 0, 0, null);
			
			//And we can take the required slice of the new image
			Image background2 = image_skyline.getSubimage(0, 0, GAME_WIDTH - background1.getWidth(null)/*this.skyline_x - background1.getWidth(null)*/ , GAME_HEIGHT);
			g2.drawImage(background2, background1.getWidth(null), 0, null);
		}
	}
	
	/**
	 * Builds a bunch of test enemies
	 * @param num The number of test enemies to build
	 */
	public void spawnEnemies(int num, int msInterval, Enemy prototype){
		Spawner s = new Spawner(num, prototype, msInterval);
		spawner = s;
		timer.scheduleAtFixedRate(s, 0, msInterval);
	}
	
	/**
	 * Returns true if the spawner timer task is running
	 * @return true if the spawner timer task is running
	 */
	public boolean isSpawning(){
		return spawner != null && spawner.getRemaining() <= 0;
	}
	/**
	 * Represents a spawner task
	 * This class will spawn a given number of enemies until none are left to spawn.
	 * run() is called by a timer.
	 */
	class Spawner extends TimerTask{
		private int num;
		private Enemy prototype;
		private int msInterval;
		
		public Spawner(int num, Enemy prototype, int msInterval){
			this.num = num;
			this.prototype = prototype.clone();
			this.msInterval = msInterval;
		}
		
		/**
		 * The millisecond interval to spawn enemies at 
		 * @return The millisecond interval to spawn enemies at 
		 */
		public int getInterval(){
			return this.msInterval;
		}
		
		public int getRemaining(){
			return this.num;
		}
		public void setRemaining(int left){
			this.num = left;
		}
		
		public void run(){
			
			if(--num <= 0 || pause){
				Game.instance.spawner = null;
				this.cancel();
			}
			Enemy e = prototype.clone();
			Game.addEntity(e);
		}
		
		/**
		 * Copies this spawner in its current state and returns it
		 */
		public Spawner clone(){
			return new Spawner(num, prototype, msInterval);
		}
	}
	class Ticker extends TimerTask{
		public void run(){
			tick();
		}
	}
	class Painter extends TimerTask{
		public void run(){
			repaint();
		}
	}
	
	/**
	 * Returns true if the given letter is pressed on the keyboard
	 * @param l The number of the character to check if it is pressed
	 * @return true if the given letter is pressed on the keyboard
	 */
	public static boolean isPressed(int l){
		return pressed.contains(l);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		pressed.add(e.getKeyCode());
		if(e.getKeyCode() == Control.SHIFT.getValue()){
			player.nextWeapon();
		}
		else if(e.getKeyCode() == Control.P.getValue()){
			if(pause){
				this.play();
			}
			else{
				this.pause();
			}
		}
		else if(pause){
			this.play();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		pressed.remove(e.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//Just kidding. Shuttup compiler. I did it already.
	}
}