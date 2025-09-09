package com.interrupt.dungeoneer.gfx.animation;

import com.interrupt.dungeoneer.entities.Entity;

public class HitAction extends AnimationAction {
	
	public float power = 1.0f; // how hard of a hit is this? 1.0 = normal
	
	public HitAction() { }

	@Override
	public void doAction(Entity instigator) {
		// todo: attack!
	}
}
