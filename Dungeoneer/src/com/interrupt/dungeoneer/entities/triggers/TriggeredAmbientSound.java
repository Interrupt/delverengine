package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;

public class TriggeredAmbientSound extends Trigger {
	public TriggeredAmbientSound() { hidden = true; spriteAtlas = "editor"; tex = 15; isSolid = false; collision.set(0.5f, 0.5f, 1f); triggerType = TriggerType.PLAYER_TOUCHED; }

	@EditorProperty(type = "FILE_PICKER", params = "audio", include_base = false)
	public String ambientSound = "env_indoor.mp3";

	@EditorProperty
	public float ambientVolume = 0.6f;

	@EditorProperty
	public float changeSpeed = 0.1f;
	
	@Override
	public void doTriggerEvent(String value) {
		Audio.playAmbientSound(ambientSound, ambientVolume, changeSpeed);
	}
	
	@Override
	public void tick(Level level, float delta) {
		super.tick(level, delta);
	}
}
