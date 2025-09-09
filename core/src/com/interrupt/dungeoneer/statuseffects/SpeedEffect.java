package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.managers.StringManager;

public class SpeedEffect extends StatusEffect {
	public SpeedEffect() {
		this(2, 500);
	}

	public SpeedEffect(float speedMod, int time) {
		this.name = StringManager.get("statuseffects.SpeedEffect.defaultNameText");
		this.speedMod = speedMod;
		this.timer = time;
		this.statusEffectType = StatusEffectType.SPEED;
		
		if(speedMod < 1) speedMod = 1;
	}
}
