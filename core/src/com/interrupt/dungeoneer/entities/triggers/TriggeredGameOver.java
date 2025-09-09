package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.overlays.MessageOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class TriggeredGameOver extends Trigger {

	@EditorProperty
	public boolean won = true;

	public TriggeredGameOver() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@Override
	public void doTriggerEvent(String value) {
		GameApplication.ShowGameOverScreen(won);
		super.doTriggerEvent(value);
	}
}
