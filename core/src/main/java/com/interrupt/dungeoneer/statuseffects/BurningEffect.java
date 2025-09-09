package com.interrupt.dungeoneer.statuseffects;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.StringManager;

public class BurningEffect extends StatusEffect {
	public float damageTimer = 60;
	public int damage = 1;
	private float dtimer = 0;

	public BurningEffect() {
		this(600, 160, 1);
	}

	public BurningEffect(int time, int damageTimer, int damage) {
		this.name = StringManager.get("statuseffects.BurningEffect.defaultNameText");
		this.timer = time;
		this.damageTimer = damageTimer;
		this.damage = damage;
		this.statusEffectType = StatusEffectType.BURNING;
	}
	
	@Override
	public void doTick(Actor owner, float delta) { 
		dtimer += delta;

		if(dtimer > damageTimer) {
			dtimer = 0;
			owner.takeDamage(damage, DamageType.PHYSICAL, null);
			doFireEffect(owner);
			Audio.playPositionedSound("mg_pass_poison.mp3", new Vector3(owner.x,owner.y,owner.z), 0.5f, 6f);
		}

		// put out the fire, if this is water
		if(owner.inwater) {
			Tile waterTile = Game.instance.level.findWaterTile(owner.x, owner.y, owner.z, owner.collision);
			if(waterTile != null && waterTile.data != null && (waterTile.data.hurts <= 0 || waterTile.data.damageType != DamageType.FIRE)) {
				active = false;
			}
		}
	}

	public void doFireEffect(Entity owner) {
		if (!this.showParticleEffect) {
			return;
		}

		for(int i = 0; i < 8; i++) {
			float spreadMod = 1f;

			if (owner instanceof Player) {
				spreadMod = 2.75f;
			}

			float scale = 0.5f;
			Particle p = CachePools.getParticle();
			p.tex = 64;
			p.lifetime = 90;
			p.scale = scale;
			p.startScale = 1.0f;
			p.endScale = 0.125f;
			p.fullbrite = true;
			p.checkCollision = false;
			p.floating = true;
			p.x = owner.x + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
			p.y = owner.y + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
			p.z = owner.z + (Game.rand.nextFloat() * owner.collision.z) - 0.5f;
			p.playAnimation(64, 69, 40f);

			p.za = Game.rand.nextFloat() * 0.004f + 0.004f;

			Game.GetLevel().SpawnNonCollidingEntity(p);
		}
	}

	@Override
	public void onStatusBegin(Actor owner) {
		doFireEffect(owner);
		Fire fire = (Fire)owner.getAttached(Fire.class);

		// Attach a fire entity so that the fire can spread
		if(fire == null) {
			Fire f = new Fire();
			f.lifeTime = timer;
			f.playAnimation();
			f.hurtTimer = f.hurtTime * 0.75f;
			f.spreadTimer = f.spreadTime * 0.75f;
			f.z = 0.125f;
			owner.attach(f);
		}
		else {
			fire.lifeTime = timer;
		}
	}

	@Override
	public void onStatusEnd(Actor owner) {
		this.active = false;
		Fire fire = (Fire)owner.getAttached(Fire.class);
		if(fire != null) fire.isActive = false;
	}
}
