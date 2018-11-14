package com.interrupt.dungeoneer.statuseffects;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.managers.StringManager;

public class InvisibilityEffect extends StatusEffect {
	public InvisibilityEffect() {
		this(1800);
	}

	public InvisibilityEffect(int time) {
		this.name = StringManager.get("statuseffects.InvisibilityEffect.defaultNameText");
		this.timer = time;
		this.statusEffectType = StatusEffectType.INVISIBLE;
	}

	@Override
	public void doTick(Actor owner, float delta) {

	}

	@Override
	public void onStatusBegin(Actor owner) {
		owner.invisible = true;
	}

	@Override
	public void onStatusEnd(Actor owner) {
		owner.invisible = false;
	}
}
