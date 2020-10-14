package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

public class PositionedSound extends Entity {
	protected transient Long soundInstance = null;
	
	@EditorProperty
	public float volume = 1f;
	
	@EditorProperty
	public float radius = 12f;
	
	@EditorProperty
	protected float pitch = 1f;
	
	protected float lifetime = 60f;
	protected transient Sound sound;
	protected transient Vector2 direction = new Vector2();

	protected boolean isPaused = false;
	
	public PositionedSound()
	{
		artType = ArtType.hidden;
		persists = false;
	}
	
	public PositionedSound(float x, float y, float z, Sound sound, float volume, float radius, float lifetime)
	{
		artType = ArtType.hidden;
		
		this.volume = volume;
		this.sound = sound;
		this.radius = radius;
		this.lifetime = lifetime;
		this.x = x;
		this.y = y;
		this.z = z;
		
		try {
			if(sound != null) {
				float v = getPositionalVolume();
				if(v > 0) {
					soundInstance = sound.play(v);
				}
			}
		} catch( Exception ex) {
			isActive = false;
		}
		
		persists = false;
	}
	
	public PositionedSound(float x, float y, float z, Sound sound, float volume, float pitch, float radius, float lifetime)
	{
		artType = ArtType.hidden;
		
		this.volume = volume;
		this.sound = sound;
		this.radius = radius;
		this.lifetime = lifetime;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		
		try {
			if(sound != null) {
				float v = getPositionalVolume();
				if(v > 0) {
					soundInstance = sound.play(v);
					sound.setPitch(soundInstance, pitch);
					sound.setPan(soundInstance, getPositionalPan(), v);
				}
			}
		} catch( Exception ex) {
			isActive = false;
		}
		
		persists = false;
	}
	
	@Override
	public void tick(Level level, float delta)
	{	
		lifetime -= delta;
		if(lifetime <= 0 && this.isActive)
		{
			stop();
		}
		
		if(soundInstance == null) {
			isActive = false;
		}
		if(!isActive || soundInstance == null) return;

		sound.setPan(soundInstance, getPositionalPan(), getPositionalVolume());
	}
	
	private float getPositionalVolume()
	{
		Player player = GameManager.getGame().player;
		
		float distance = Math.max( Math.abs(x - player.x), Math.abs(y - player.y) );
		float dmod = distance / radius;
		if(dmod > 1) dmod = 1;
		dmod = 1 - dmod;
		
		return (dmod * volume) * Options.instance.sfxVolume;
	}
	
	public void stop()
	{
		if(soundInstance != null)
			sound.stop(soundInstance);
		sound = null;
		soundInstance = null;
		isActive = false;
	}

	public void pause()
    {
        if(!isPaused) {
            if (soundInstance != null)
                sound.pause(soundInstance);

            isPaused = true;
        }
    }

    public void resume()
    {
        if(isPaused) {
            if (soundInstance != null) {
                sound.resume(soundInstance);
            } else {
                float v = getPositionalVolume();
                if (v > 0) {
                    soundInstance = sound.play(v);
                }
            }

            isPaused = false;
        }
    }

	private static Vector3 rightDirection = new Vector3();
	private static Vector2 temp1 = new Vector2();
	private static Vector2 temp2 = new Vector2();
	private static Vector2 temp3 = new Vector2();

	public float getPositionalPan() {
		com.badlogic.gdx.graphics.Camera camera = GameManager.renderer.camera;
		rightDirection.set(camera.direction).crs(camera.up).nor();

		temp1.set(camera.position.x, camera.position.z);
		temp2.set(x, y).sub(temp1);
		temp3.set(camera.direction.x, camera.direction.z);

		float r = temp2.angle(temp3);
		float angle = Math.abs(r);
		boolean left = r < 0;

		float reduced;
		if(angle <= 90.0) {
			reduced = angle / 90.0f;
		} else {
			reduced = (180 - angle) / 90.0f;
		}

		if(!left) reduced *= -1;
		return reduced;
	}
	
	@Override
	public void onDispose() {
		stop();
	}
}
