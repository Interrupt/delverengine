package com.interrupt.dungeoneer.gfx.animation;

import com.badlogic.gdx.math.Vector2;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Game;

public class DamageAction extends AnimationAction {
	
	private float rangeBoost = 0f;
	private int damageBoost = 0;
	private DamageType damageType = DamageType.PHYSICAL;
	private float knockback = 0.05f;
	
	public DamageAction() { }
	
	public DamageAction(float rangeBoost, int damageBoost, float knockback) {
		this.rangeBoost = rangeBoost;
		this.damageBoost = damageBoost;
	}

	@Override
	public void doAction(Entity instigator) {
		if(instigator instanceof Monster) {
			// let the monster do the hit
			Monster m = ((Monster)instigator);
			m.tryDamageHit(m.getAttackTarget(), rangeBoost, knockback);
		}
		else {
			// deal some damage manually
			Vector2 dir = new Vector2(Game.instance.player.x, Game.instance.player.y).sub(new Vector2(instigator.x, instigator.y));
			float playerdist = dir.len();
			dir = dir.nor();
			
			int dmg = Game.instance.player.damageRoll(damageBoost, damageType, instigator);
			if(playerdist < rangeBoost) Game.instance.player.hit(dir.x, dir.y, dmg, knockback, damageType, instigator);
		}
	}

}
