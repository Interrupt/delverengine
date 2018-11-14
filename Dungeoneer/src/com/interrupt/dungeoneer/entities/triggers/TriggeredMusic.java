package com.interrupt.dungeoneer.entities.triggers;

import java.util.Random;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.PositionedSound;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

public class TriggeredMusic extends Trigger {
	public TriggeredMusic() { hidden = true; spriteAtlas = "editor"; tex = 15; isSolid = false; }
	
	@EditorProperty
	public String musicFile = "entrance.mp3";
	
	@EditorProperty
	public boolean loops = true;
	
	@EditorProperty
	public float fadeTime = 120f;
	
	public transient float startTimer = -1f;
	
	private boolean didStartPlaying = false;
	
	@Override
	public void doTriggerEvent(String value) {
		
		if(didStartPlaying) return;
		didStartPlaying = true;
		
		Music m = Audio.getPlayingMusic();
		if(m == null || !m.isPlaying()) {
			Audio.playMusic(musicFile, loops);
		}
		else {
			startTimer = fadeTime;
		}
	}
	
	@Override
	public void tick(Level level, float delta) {
		
		if(!didStartPlaying)
			super.tick(level, delta);
		
		if(startTimer > 0) {
			startTimer -= delta;
			
			if(startTimer <= 0) {		
				Audio.setMusicVolume(1f);
				Audio.playMusic(musicFile, loops);
			}
			else {
				Audio.setMusicVolume(Math.min(startTimer / fadeTime, 1f));
			}
		}
	}
}
