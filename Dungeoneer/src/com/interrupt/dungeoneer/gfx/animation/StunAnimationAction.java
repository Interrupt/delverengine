package com.interrupt.dungeoneer.gfx.animation;

import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Monster;

public class StunAnimationAction extends AnimationAction {
	
	private Float stun;
	private Float attackDelay;
	
	public StunAnimationAction() { }

	@Override
	public void doAction(Entity instigator) {
		if(instigator instanceof Monster) {
			Monster m = (Monster)instigator;
			if(stun != null) m.stun(stun);
			if(attackDelay != null) m.delayAttack(attackDelay);
		}
	}
}
