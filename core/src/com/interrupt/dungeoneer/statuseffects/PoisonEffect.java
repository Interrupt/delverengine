package com.interrupt.dungeoneer.statuseffects;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class PoisonEffect extends StatusEffect {
	public Particle effectParticle;
	public Vector3 effectOffset = new Vector3(0.0f, 0.0f, 0.55f);
	public float damageTimer = 60;
	public int damage = 1;
	private float dtimer = 0;
	private boolean canKill = false;
	private float particleInterval = 20f;
	private float particleTimer = 0;
	
	public PoisonEffect() {
		this(1000, 160, 1, false);
	}
	
	public PoisonEffect(int time, int damageTimer, int damage, boolean canKill) {
		this.name = StringManager.get("statuseffects.PoisonEffect.defaultNameText");
		this.timer = time;
		this.statusEffectType = StatusEffectType.POISON;
		this.damageTimer = damageTimer;
		this.damage = damage;
		this.canKill = canKill;
	}
	
	@Override
	public void doTick(Actor owner, float delta) { 
		dtimer += delta;
		this.particleTimer += delta;

		if (this.particleTimer > this.particleInterval) {
			this.particleTimer = 0f;
			this.createPoisonParticle(owner, Game.rand.nextFloat() * 0.25f + 0.25f);
		}

		if(dtimer > damageTimer) {
			dtimer = 0;
			if(owner.hp - damage <= 0 && !canKill ) return;
			
			owner.takeDamage(damage, DamageType.PHYSICAL, null);
			this.doPoisonEffect(owner);
			
			Audio.playPositionedSound("mg_pass_poison.mp3", new Vector3(owner.x,owner.y,owner.z), 0.5f, 6f);
		}
	}

	@Override
	public void onStatusBegin(Actor owner) {
		this.doPoisonEffect(owner);
	}

	private void doPoisonEffect(Actor owner) {
		int impactParticleCount = Game.rand.nextInt(8) + 2;
		for (int i = 0; i < impactParticleCount; i++) {
			this.createPoisonParticle(owner, 0.5f);
		}
	}

	private void createPoisonParticle(Actor owner, float scale) {
		if (!this.showParticleEffect) {
			return;
		}

		float spreadMod = 1f;
		float zMod = 0f;
		if(owner instanceof Player) {
			spreadMod = 2.75f;
			zMod = -0.3f;
		}

		Particle p = CachePools.getParticle();
		p.tex = 80 + Game.rand.nextInt(3);
		p.lifetime = 90;
		p.scale = scale;
		p.startScale = 1.0f;
		p.endScale = 0.125f;
		p.fullbrite = true;
		p.checkCollision = false;
		p.floating = true;
		p.x = owner.x + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
		p.y = owner.y + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
		p.z = owner.z + zMod;

		p.za = Game.rand.nextFloat() * 0.004f + 0.004f;

		Game.GetLevel().SpawnNonCollidingEntity(p);
	}

	@Override
	public void forPlayer(Player player) {
		player.history.poisoned();
	}

	@Override
	public void onStatusEnd(Actor owner) {
		this.active = false;
	}
}
