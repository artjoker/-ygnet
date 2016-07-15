package com.cygnet.ourdrive.swingTail;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;

public class Filter implements Runnable{
	private String triggerValue					= "";
	private boolean startsWith					= false;
	private boolean contains					= false;
	private boolean endsWith					= false;
	private boolean markAsBold					= false;
	private boolean markWithColor				= false;
	private Color color							= Color.WHITE;
	private boolean soundWarning				= false;
	private boolean hide						= false;
	private boolean filterActive				= false;
	private boolean pauseOnFind					= false;
	private String soundFilePath				= "";
	private Clip clip;
	
	public Filter() {
	}

	public String getTriggerValue() {
		return triggerValue;
	}

	public boolean isStartsWith() {
		return startsWith;
	}

	public boolean isContains() {
		return contains;
	}

	public boolean isEndsWith() {
		return endsWith;
	}

	public boolean isMarkAsBold() {
		return markAsBold;
	}

	public boolean isMarkWithColor() {
		return markWithColor;
	}

	public boolean isSoundWarning() {
		return soundWarning;
	}

	public boolean isHide() {
		return hide;
	}

	public void setTriggerValue(String triggerValue) {
		this.triggerValue = triggerValue;
	}

	public void setStartsWith(boolean startsWith) {
		this.startsWith = startsWith;
	}

	public void setContains(boolean contains) {
		this.contains = contains;
	}

	public void setEndsWith(boolean endsWith) {
		this.endsWith = endsWith;
	}

	public void setMarkAsBold(boolean markAsBold) {
		this.markAsBold = markAsBold;
	}

	public void setMarkWithColor(boolean markWithColor) {
		this.markWithColor = markWithColor;
	}

	public void setSoundWarning(boolean soundWarning) {
		this.soundWarning = soundWarning;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isFilterActive() {
		return filterActive;
	}

	public void setFilterActive(boolean filterActive) {
		this.filterActive = filterActive;
	}

	public boolean isPauseOnFind() {
		return pauseOnFind;
	}

	public void setPauseOnFind(boolean pauseOnFind) {
		this.pauseOnFind = pauseOnFind;
	}

	public String getSoundFilePath() {
		return soundFilePath;
	}

	public void setSoundFilePath(String soundFilePath) {
		this.soundFilePath = soundFilePath;
	}

	public void loadSoundFile(){
		
		if(clip != null){
			clip.close();
		}		
		
		if(soundWarning && soundFilePath != null && !soundFilePath.trim().isEmpty()){
			
			AudioInputStream inputStream = null;
			
			try {

				clip = AudioSystem.getClip();

				
				if (!soundFilePath.equalsIgnoreCase("/swingtail/Blip.wav")){
				
					File file = new File(soundFilePath);
					
					inputStream = AudioSystem.getAudioInputStream(file);
					
				}else{
					
					inputStream = AudioSystem.getAudioInputStream(this.getClass().getResourceAsStream(soundFilePath));
				}

				clip.open(inputStream);			
				
			} catch (Exception e) {

				if(clip != null){
					clip.close();
				}
							
				JOptionPane.showMessageDialog(null,"Something is wrong with the sound file " + soundFilePath + ", \nthe program will be unable to play sound. Please check that the file is correct and working or change the file", "Error playing sound", JOptionPane.ERROR_MESSAGE);
				
			}finally{
				
				if(inputStream != null){
					
					try {
						inputStream.close();
					} catch (IOException e) {}
				}				
			}
			
		}
	}
	
	public void playSound(){
		
		new Thread(this).start();
	}

	@Override
	public void run() {
		
		//Play da sound!
		
		if(clip != null){
			clip.setFramePosition(0);
			clip.start();
		}
	}
}
