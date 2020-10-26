package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.entities.Explosion;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.ProjectedDecal;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class SplashExplosion extends Spell {
	/** Damage amount. */
    public int damage = 2;

	/** Splash radius. */
    public float radius = 0.5f;

	/** Explosion force. */
    public float physicsForce = 0.2f;

	/** Sound to play when exploding. */
    public String explodeSound = "explode.mp3,explode_02.mp3,explode_03.mp3,explode_04.mp3";

	private Entity owner = null;
	
	public SplashExplosion() { }
	
	public SplashExplosion(DamageType damageType, int damage) {
		this.damageType = damageType;
		this.damage = damage;
	}
	
	@Override
	public void doCast(Entity owner, Vector3 direction, Vector3 position) {
		this.owner = owner;
		doCast(new Vector3((float)owner.x + 0.5f,(float)owner.y + 0.5f,(float)owner.z + 0.3f), direction);
	}
	
	public void doCast(Vector3 pos, Vector3 direction) {
		Level level = Game.GetLevel();
		
		Color color = Weapon.getEnchantmentColor(this.damageType);
		
		Particle ring = new Particle(pos.x, pos.y, pos.z, 0, 0, 0, 0, color, true);
		((DrawableSprite)ring.drawable).billboard = false;
		((DrawableSprite)ring.drawable).dir.set(Vector3.Y).nor();
		ring.xa = 0;
		ring.ya = 0;
		ring.za = 0;
		ring.artType = ArtType.particle;
		ring.tex = 16;
		ring.lifetime = 30;
		ring.startScale = 0.01f;
		ring.fullbrite = true;
		ring.endScale = radius * 18f;
		ring.scale = 0f;
		ring.floating = true;
		ring.yOffset = -0.45f;
		ring.checkCollision = false;
		ring.color.set(color);
		ring.isActive = true;
		ring.initialized = false;
		ring.isDynamic = true;
		Game.instance.level.non_collidable_entities.add(ring);
		
		// make explosion
		Explosion explosion = new Explosion();
		explosion.explosionLightLifetime = 25;
		explosion.x = pos.x;
		explosion.y = pos.y;
		explosion.z = pos.z - 0.45f;
		explosion.owner = owner;
		explosion.makeDustRing = true;
		
		explosion.impulseDistance = radius * 4f;
		explosion.impulseAmount = physicsForce;
		explosion.damage = damage;
		explosion.damageType = damageType;
		explosion.color = new Color(color);
		
		explosion.scale = 2f;

		explosion.hitDecal = new ProjectedDecal(ArtType.sprite, 19, 1.25f);

		// some spells apply status effects
		explosion.applyStatusEffect = applyStatusEffect;
		
		if(damageType == DamageType.POISON) {
			explosion.explodeSound = "trap_poison.mp3";
		}
		else if(castSound != null) {
			explosion.explodeSound = explodeSound;
		}
		
		level.entities.add(explosion);
	}
}
