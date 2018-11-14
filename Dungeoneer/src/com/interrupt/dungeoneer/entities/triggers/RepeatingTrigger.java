package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;

public class RepeatingTrigger extends Trigger {
	
	@EditorProperty
	public boolean waitForTrigger = false;

	public RepeatingTrigger() {
		super();
		triggerDelay = 100;
		triggerResets = true;
	}

	@Override
	public void onTrigger(Entity instigator, String value) {
		waitForTrigger = !waitForTrigger;
	}
	
	public void tick(Level level, float delta) {
		super.tick(level, delta);

		if(!waitForTrigger) {
			fire(null);
		}
	}
}
