package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;

public class TriggeredDelete extends Trigger {

	@EditorProperty
	public String deleteId = "ENTITY_TO_DELETE";

	public TriggeredDelete() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@Override
	public void doTriggerEvent(String value) {
		Array<Entity> entities = Game.GetLevel().getEntitiesById(deleteId);
		for(int i = 0; i < entities.size; i++) {
			entities.get(i).isActive = false;
		}

		super.doTriggerEvent(value);
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		deleteId = makeUniqueIdentifier(deleteId, idPrefix);
	}
}
