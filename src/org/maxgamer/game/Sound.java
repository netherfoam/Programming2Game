package org.maxgamer.game;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Sound{
	private Clip[] sounds;
	private int id = 0;
	
	/**
	 * Creates a new sound. This is playable and has by default 8 copies.
	 * Thus this can overlap itself upto 8 times.  This sound is loaded
	 * into memory 8 times, and each time it plays it does NOT use the
	 * harddrive.
	 * @param filename The file to load from.
	 */
    public Sound(String filename) {
    	try {
			sounds = new Clip[10];
			
			for(int i = 0; i < sounds.length; i++){
				Clip sound = AudioSystem.getClip();
				sound.open(AudioSystem.getAudioInputStream(new File(filename)));
				
				sounds[i] = sound;
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
    }

    /**
     * Plays this sound.  Will overlap with itself up to 8 times.
     */
    public void play() {
    	sounds[id].setFramePosition(0);
    	sounds[id].start();
    	id++;
    	id = id % sounds.length;
    }
    
    public void stop(){
    	for(Clip clip : sounds){
    		clip.stop();
    	}
    }
}