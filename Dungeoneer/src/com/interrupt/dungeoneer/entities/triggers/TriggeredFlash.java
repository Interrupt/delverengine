package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;

public class TriggeredFlash extends Trigger {

	@EditorProperty
	public Color flashColor = new Color(1f, 1f, 1f, 1f);

	@EditorProperty
	public int flashTime = 100;

	public TriggeredFlash() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@Override
	public void doTriggerEvent(String value) {
		Game.flash(flashColor, flashTime);
		super.doTriggerEvent(value);
	}
}
