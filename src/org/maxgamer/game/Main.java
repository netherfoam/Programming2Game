package org.maxgamer.game;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Main{
	public static JFrame frame = new JFrame("Minimus Game");
	public static JLabel time = new JLabel("Time: 0");
	public static JLabel score = new JLabel("Score: 0");
	public static JLabel health = new JLabel("Health: 0");
	
	public static void main(String[] args){
		frame.setSize(486,534);
		frame.setResizable(false);
		
		
		JPanel master = new JPanel();
		
		frame.add(master);
		
		master.setLayout(new BoxLayout(master, BoxLayout.Y_AXIS));
		
		JPanel dashboard = new JPanel();
		dashboard.setBackground(Color.orange);
		master.add(dashboard);
		
		//This will only respect the horizontal size and not the vertical size.
		dashboard.setMaximumSize(new Dimension(500, 0));
		
		dashboard.add(health);
		dashboard.add(score);
		dashboard.add(time);
		//Important
		//Setting the frame to visible stop us being able to add a key listener...
		//Because java is weird like that.
		Game game = new Game(480, 480);
		
		master.add(game);
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}