package com.interrupt.dungeoneer.gfx.animation;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Entity;

public class SoundAction extends AnimationAction {
	
	public String soundFile = null;
	public float volume = 1.0f;
	public float pitch = 1.0f;
	public float range = 8.0f;
	
	public SoundAction() { }
	
	public SoundAction(String soundFile, float volume, float pitch, float range) {
		this.soundFile = soundFile;
		this.volume = volume;
		this.pitch = pitch;
		this.range = range;
	}

	@Override
	public void doAction(Entity instigator) {
		Audio.playPositionedSound(soundFile, new Vector3(instigator.x, instigator.y, instigator.z), volume, range, pitch);
	}
}
