package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;

public class TriggeredAchievement extends Trigger {

	public TriggeredAchievement() { hidden = true; spriteAtlas = "editor"; tex = 11; triggerResets = false; triggerType = TriggerType.PLAYER_TOUCHED; }

	@EditorProperty
	public String achievementName;
	
	@Override
	public void doTriggerEvent(String value) {
		Game.achievementManager.achieve(achievementName);
		super.doTriggerEvent(value);
	}
}
