package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.game.Options;

public class AmbientSound extends PositionedSound {
	
	@EditorProperty(type = "FILE_PICKER", params = "audio", include_base = false)
	public String soundFile = "torch.mp3";

	@EditorProperty
	public boolean loadOnStartup = false;
	
	public AmbientSound() { hidden = true; spriteAtlas = "editor"; tex = 15; isSolid = false; persists = true; }
	
	public transient boolean canUpdate = true;

	public transient float curVolume = 1f;

	public AmbientSound(float x, float y, float z, String soundFile, float volume, float pitch, float radius) {
		artType = ArtType.hidden;
		
		this.volume = volume;
		this.soundFile = soundFile;
		this.radius = radius;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		
		persists = true;
	}
	
	@Override
	public void tick(Level level, float delta)
	{	
		// ambient looping sounds aren't supported on mobile
		if(Game.isMobile) {
			isActive = false;
			return;
		}

		curVolume = getPositionalVolume();
		
		try {
			if(curVolume > 0) {
				if(canUpdate) {
					// play if not playing already and close enough
					if(soundInstance == null || sound == null) {
						sound = Audio.loadSound(soundFile);
						soundInstance = sound.play(curVolume);
						sound.setPitch(soundInstance, pitch);
						sound.setLooping(soundInstance, true);
						sound.setPan(soundInstance, getPositionalPan(), curVolume * Options.instance.sfxVolume);
						Audio.addAmbientSound(this);
					}
					else {
						// otherwise just set the volume
						sound.setPan(soundInstance, getPositionalPan(), curVolume * Options.instance.sfxVolume);
					}
				}
			}
			else {
				// stop if too far away
				if(soundInstance != null) {
					if(!loadOnStartup) {
						Audio.removeAmbientSound(this);
						sound.stop(soundInstance);
						soundInstance = null;
					}
					else if(sound != null) {
						sound.setVolume(soundInstance, 0f);
					}
				}
			}
		}
		catch( Exception ex) {
			canUpdate = false;
			Gdx.app.log("Delver", ex.toString());
		}	
	}

	public void refreshVolume() {
		if(soundInstance != null && sound != null) {
			sound.setVolume(soundInstance, curVolume * Options.instance.sfxVolume);
		}
	}
	
	private float getPositionalVolume()
	{
		Camera camera = GameManager.renderer.camera;
		
		float distance = Math.max( Math.abs(x - camera.position.x), Math.abs(y - camera.position.z) );
		float dmod = distance / radius;
		if(dmod > 1) dmod = 1;
		dmod = 1 - dmod;
		
		return (dmod * volume);
	}
	
	@Override
	public void editorTick(Level level, float delta) {
		tick(level,delta);
	}
	
	@Override
	public void editorStartPreview(Level level) {
		editorStopPreview(level);
		sound = null;
		soundInstance = null;
		canUpdate = true;
	}
	
	@Override
	public void editorStopPreview(Level level) {
		canUpdate = false;
		if(soundInstance != null && sound != null) {
			sound.stop(soundInstance);
			soundInstance = null;
		}
	}
	
	@Override
	public void onDispose() {
		if(soundInstance != null && sound != null) {
			sound.stop(soundInstance);
			soundInstance = null;
		}
	}
	
	@Override
	public void init(Level level, Source source) {
		if(loadOnStartup && !Game.isMobile) {
			sound = Audio.loadSound(soundFile);
			if(sound != null) {
				soundInstance = sound.play(0f);
				sound.setPitch(soundInstance, pitch);
				sound.setLooping(soundInstance, true);
				Audio.addAmbientSound(this);
			}
		}

		// Don't autoplay in the editor
		if(source == Source.EDITOR) {
			canUpdate = false;
		}
	}
}
