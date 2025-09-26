package com.interrupt.dungeoneer.gfx.animation;

import com.interrupt.dungeoneer.entities.Entity;

public abstract class AnimationAction {
	public AnimationAction() { }
	
	public abstract void doAction(Entity instigator);
}
