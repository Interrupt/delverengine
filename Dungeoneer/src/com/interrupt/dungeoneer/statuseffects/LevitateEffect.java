package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.managers.StringManager;

public class LevitateEffect extends StatusEffect {
	public LevitateEffect() {
		this(1800);
	}

	boolean ownerWasFloating = false;

	public LevitateEffect(int time) {
		this.name = StringManager.get("statuseffects.LevitateEffect.defaultNameText");
		this.timer = time;
		this.statusEffectType = StatusEffectType.LEVITATE;
	}

	@Override
	public void doTick(Actor owner, float delta) {

	}

	@Override
	public void onStatusBegin(Actor owner) {
		ownerWasFloating = owner.floating;
		owner.floating = true;
	}

	@Override
	public void onStatusEnd(Actor owner) {
		owner.floating = ownerWasFloating;
	}
}
