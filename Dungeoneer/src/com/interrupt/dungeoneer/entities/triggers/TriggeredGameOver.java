package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.annotations.EditorProperty;

public class TriggeredGameOver extends Trigger {

	@EditorProperty
	public boolean won = true;

	public TriggeredGameOver() { hidden = true; spriteAtlas = "editor"; tex = 20; }
	
	@Override
	public void doTriggerEvent(String value) {
		GameApplication.ShowGameOverScreen(won);
		super.doTriggerEvent(value);
	}
}
