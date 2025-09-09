package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Trap.TrapType;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.entities.spells.SplashExplosion;
import com.interrupt.dungeoneer.entities.spells.Teleport;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;

public class TriggeredTrap extends Trigger {
	
	public enum TrapType { explosion, teleport, poison, random };
	@EditorProperty public TrapType trapType = TrapType.explosion;

	public TriggeredTrap() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@Override
	public void init(Level level, Source source) {
		super.init(level, source);
		
		// might need to set a random trap type
		if(trapType == TrapType.random) {
			trapType = TrapType.values()[Game.rand.nextInt(TrapType.values().length - 1)];
		}
	}
	
	@Override
	public void doTriggerEvent(String value) {
		
		Level level = Game.instance.level;
		if(trapType == TrapType.explosion) {
			SplashExplosion trap = new SplashExplosion(DamageType.FIRE, 6 + (int)(level.dungeonLevel * 0.5));
			trap.physicsForce = 0.1f;
			trap.doCast(new Vector3(x,y,z), Vector3.Zero);
		}
		else if(trapType == TrapType.teleport) {
			Teleport trap = new Teleport();
			trap.doCast(new Vector3(x,y,z), Vector3.Zero);
		}
		else if(trapType == TrapType.poison) {
			SplashExplosion trap = new SplashExplosion(DamageType.POISON, 2);
			trap.physicsForce = 0.1f;
			trap.doCast(new Vector3(x,y,z), Vector3.Zero);
		}
		
		Game.instance.player.history.activatedTrap(this);
		
		super.doTriggerEvent(value);
	}
}
