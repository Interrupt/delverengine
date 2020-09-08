package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Game;

public class DamageTrigger extends BasicTrigger {
    /** Damage amount needed to send trigger event. */
	@EditorProperty
	public int damageThreshold = 1;

	/** Entity to send trigger event when damage fails to exceed {@link #damageThreshold}. */
	@EditorProperty
	public String triggersOnFail = null;

	public DamageTrigger() { hidden = true; spriteAtlas = "editor"; tex = 11; }

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
