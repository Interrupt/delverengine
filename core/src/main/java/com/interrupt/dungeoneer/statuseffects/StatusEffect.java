package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;


public class StatusEffect {
	public enum StatusEffectType { BURNING, DRUNK, INVISIBLE, PARALYZE, POISON, RESTORE, SHIELD, SLOW, LEVITATE, SPEED, SLOW_TIME }

	/** Name of status effect. Shown on player HUD. */
	public String name = StringManager.get("statuseffects.StatusEffect.defaultNameText");

	/** Duration of status effect in milliseconds. */
	public float timer = 1000;

	/** Speed multiplier for effected player and monsters. */
	public float speedMod = 1;

	/** Damage multiplier for effected actors. */
	public float damageMod = 1;

	/** Magic damage multiplier for effected actors. */
	public float magicDamageMod = 1;

	/** Is status effect active? */
	public boolean active = true;

	/** Show a particle effect while active? */
	public boolean showParticleEffect = true;

	/** Amount to adjust the field of view while active */
	public float fieldOfViewMod = 1;

	/** Shader name to draw effected actor with. */
	public String shader = null;

	public transient Entity owner = null;

	/** Status effect type. */
	public StatusEffectType statusEffectType;

	public static StatusEffect getStatusEffect(DamageType damageType) {
		StatusEffect effect = null;

		switch (damageType) {
			case PHYSICAL:
				break;
			case MAGIC:
				break;
			case FIRE:
				if(Game.rand.nextBoolean()) return new BurningEffect();
				break;
			case ICE:
				return new SlowEffect();
			case LIGHTNING:
				break;
			case POISON:
				return new PoisonEffect();
			case HEALING:
				break;
			case PARALYZE:
				return new ParalyzeEffect();
		}

		return effect;
	}

	public static StatusEffect getStatusEffect(StatusEffectType effectType) {
		switch (effectType) {
			case BURNING:
				return new BurningEffect();

			case DRUNK:
				return new DrunkEffect();

			case INVISIBLE:
				return new InvisibilityEffect();

			case PARALYZE:
				return new ParalyzeEffect();

			case POISON:
				return new PoisonEffect();

			case RESTORE:
				return new RestoreHealthEffect();

			case SHIELD:
				return new ShieldEffect();

			case SLOW:
				return new SlowEffect();

			case SPEED:
				return new SpeedEffect();

            case SLOW_TIME:
                return new SlowTimeEffect();
		}

		return null;
	}

	public StatusEffect() { }
	
	public void tick(Actor owner, float delta) {
		if (this.timer > 0) {
			this.timer -= 1 * delta;
		}
		else if (this.active) {
			this.active = false;
		}

		if (this.active) {
			this.doTick(owner, delta);
		}
	}

	// Override this to animate the field of view modifier
	public float getFieldOfViewMod() {
	    return fieldOfViewMod;
    }

	public void forPlayer(Player player) { }
	
	// Override this for different status effects
	public void doTick(Actor owner, float delta) {}

	// Override this for beginning any special status effects
	public void onStatusBegin(Actor owner) {}

	// Override this for ending any special status effects
	public void onStatusEnd(Actor owner) {}
}
