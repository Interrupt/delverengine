package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Game;

public class DamageTrigger extends BasicTrigger {

	@EditorProperty
	public int damageThreshold = 1;

	public DamageTrigger() { hidden = true; spriteAtlas = "editor"; tex = 11; }

	@EditorProperty
	public String triggersOnFail = null;
	
	@Override
	public void doTriggerEvent(String value) { }

	@Override
	public void hit(float projx, float projy, int damage, float knockback, Weapon.DamageType damageType, Entity instigator)
	{
		super.hit(projx, projy, damage, knockback, damageType, instigator);

		if(damage >= damageThreshold) {
			super.doTriggerEvent(triggerValue);
		}
		else {
			Game.instance.level.trigger(this, triggersOnFail, triggerValue);
		}
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		triggersOnFail = makeUniqueIdentifier(triggersOnFail, idPrefix);
	}
}
