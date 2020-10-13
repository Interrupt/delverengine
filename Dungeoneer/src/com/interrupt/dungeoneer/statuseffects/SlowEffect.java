package com.interrupt.dungeoneer.statuseffects;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class SlowEffect extends StatusEffect {
	public Particle effectParticle;
	public Vector3 effectOffset = new Vector3(0.0f, 0.0f, 0.55f);
	private float particleInterval = 60f;
	private float particleTimer = 0;

	public SlowEffect() {
		this(0.5f, 500);
		shader = "magic-item-white";
	}

	public SlowEffect(float speedMod, int time) {
		this.name = StringManager.get("statuseffects.SlowEffect.defaultNameText");
		this.speedMod = speedMod;
		this.timer = time;
		this.statusEffectType = StatusEffectType.SLOW;

		if (speedMod > 1) speedMod = 1;
	}

	@Override
	public void doTick(Actor owner, float delta) {
		this.particleTimer += delta;

		if (this.particleTimer > this.particleInterval) {
			this.particleTimer = 0f;
			this.createSlowParticle(owner, 1.0f);
		}
	}

	private void createSlowParticle(Actor owner, float scale) {
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
		p.tex = 83;
		p.lifetime = 200;
		p.scale = 1f;
		p.fullbrite = true;
		p.checkCollision = false;
		p.floating = true;
		p.startScale = 1.0f;
		p.endScale = 0.125f;
		p.x = owner.x + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
		p.y = owner.y + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
		p.z = owner.z + zMod;

		p.turbulenceAmount = 0.025f;
		p.turbulenceMoveModifier = 0.025f;

		p.za = 0.003125f;

		Game.GetLevel().SpawnNonCollidingEntity(p);
	}
}
