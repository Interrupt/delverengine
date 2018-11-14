package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.managers.StringManager;

public class DrunkEffect extends StatusEffect {

	public DrunkEffect() {
		this(2700);
	}

	public DrunkEffect(int time) {
		this.name = StringManager.get("statuseffects.DrunkEffect.defaultNameText");
		this.timer = time;
		this.statusEffectType = StatusEffectType.DRUNK;
	}
	
	@Override
	public void doTick(Actor owner, float delta) {
		owner.drunkMod += delta * 0.025f;
	}
}
