package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.overlays.MessageOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class TriggeredShake extends Trigger {

	@EditorProperty
	public float shakeAmount = 10f;
	
	@EditorProperty
	public float shakeRange = 1000f;

	public TriggeredShake() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@Override
	public void doTriggerEvent(String value) {
		Game.instance.player.shake(shakeAmount, shakeRange, new Vector3(x,y,z));
		super.doTriggerEvent(value);
	}
}
