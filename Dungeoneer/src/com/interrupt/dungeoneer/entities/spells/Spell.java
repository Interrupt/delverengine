package com.interrupt.dungeoneer.entities.spells;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;


public class Spell {
	public int mpCost = 1;
	public int baseDamage = 1;
	public int randDamage = 1;
	public DamageType damageType = DamageType.MAGIC;
	public Color spellColor = Colors.MAGIC;
	public String castSound = "spell-missile-2.mp3,spell-missile-2_02.mp3,spell-missile-2_03.mp3,spell-missile-2_04.mp3";
	public float castSoundVolume = 0.5f;

	public float minDistanceToTarget = 0f;
	public float maxDistanceToTarget = 30f;

	public StatusEffect applyStatusEffect = null;

	public boolean doCastVfx = true;

	public Entity castVfx = null;
	
	public Spell() { }
	
	// casting directly costs spell points
	public void cast(Actor owner, Vector3 direction) {
		if(owner.mp < mpCost) return;
		
		owner.mp -= mpCost;
		if(owner.mp < 0) owner.mp = 0;
		if(owner.mp > owner.maxMp) owner.mp = owner.maxMp;
		
		doCast(owner, direction, new Vector3(owner.x, owner.y, owner.z));
		playCastSound(owner);
	}
	
	// zapping from a wand or scroll costs no spell points
	public void zap(Actor owner, Vector3 direction) {
		zap(owner, direction, new Vector3(owner.x, owner.y, owner.z));
	}

	// zap with a position AND direction
    public void zap(Actor owner, Vector3 direction, Vector3 position) {
        doCast(owner, direction, position);

		if(doCastVfx) {
			if(castVfx == null) {
				doCastEffect(position, Game.GetLevel(), owner);
			}
			else {
				Entity vfx = (Entity)KryoSerializer.copyObject(castVfx);

				vfx.x += position.x;
				vfx.y += position.y;
				vfx.z += position.z;

				Game.GetLevel().SpawnEntity(vfx);
			}
		}

        playCastSound(owner);
    }
	
	// Override this for specific spell effects
	public void doCast(Entity owner, Vector3 direction, Vector3 position) { }

	private void doCastEffect(Level level, Entity owner) {
		doCastEffect(new Vector3(owner.x, owner.y, owner.z), level, owner);
	}

	// Override this for different spell casting effects
	protected void doCastEffect(Vector3 pos, Level level, Entity owner) { }
	
	// Override this for different casting sounds
	public void playCastSound(Actor owner) {
		if(owner == Game.instance.player) {
			Audio.playSound(castSound, castSoundVolume);
		}
		else {
			Audio.playPositionedSound(castSound, new Vector3(owner.x, owner.y, owner.z), castSoundVolume, 12);
		}
	}
	
	public int doAttackRoll()
	{	
		Random r = new Random();
		int dmg = baseDamage;
		dmg += r.nextInt(randDamage + 1);
		
		return dmg;
	}
}
