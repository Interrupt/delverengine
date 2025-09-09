package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.managers.StringManager;

public class RestoreHealthEffect extends StatusEffect {
	public float effectTimer = 60;
	public int healing = 1;
	private float dtimer = 0;
	
	public RestoreHealthEffect() {
		this(1800,160, 1);
	}

	public RestoreHealthEffect(int time, int effectTimer, int healing) {
		this.name = StringManager.get("statuseffects.RestoreHealthEffect.defaultNameText");
		this.timer = time;
		this.statusEffectType = StatusEffectType.RESTORE;
		this.effectTimer = effectTimer;
		this.healing = healing;
	}
	
	@Override
	public void doTick(Actor owner, float delta) { 
		dtimer += delta;
		
		if(dtimer > effectTimer) {
			dtimer = 0;
			owner.hp += healing;
			if(owner.hp > owner.getMaxHp()) owner.hp = owner.getMaxHp();
		}
	}
}
