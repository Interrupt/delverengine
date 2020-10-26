package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.projectiles.MagicMissileProjectile;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.managers.EntityManager;

public class MagicMissile extends Spell {
	/** Sound to play on hit. */
	public String hitSound = null;

	/** Spell sprite. */
    public Material appearance = null;

	/** Spell explosion. */
    public Explosion explosion = null;

	/** Spell projectile speed. */
    float speed = 0.17f;

	/** Particle trail spawn interval. */
	float trailInterval = 1f;

	/** Force of spell splash damage. */
	float splashForce = 0.1f;

	/** Radius of spell splash. */
	float splashRadius = 3f;

	/** Does spell cause splash damage? */
	public boolean splashDamage = false;

	/** Does spell projectile float? */
	boolean floating = true;

	/** Spell aim accuracy. */
	float shotAccuracy = 1.0f;

	/** Spell projectile. */
    public MagicMissileProjectile magicMissileProjectile = null;

    public MagicMissile() { }

	@Override
	public void doCast(Entity owner, Vector3 direction, Vector3 position) {
		
		int dmg = doAttackRoll();

		if (Math.abs(this.shotAccuracy) < 1.0f) {
			Vector3 axis = direction.cpy();
			direction.rotate(Game.rand.nextFloat() * (1.0f - Math.abs(this.shotAccuracy)) * 45f, 0, 1, 0);
			direction.rotate(axis, Game.rand.nextFloat() * 360f);
		}

		MagicMissileProjectile projectile = makeProjectile(position, direction, dmg, owner);
		Game.GetLevel().entities.add(projectile);
	}

	// Make the projectile to fire
	protected MagicMissileProjectile makeProjectile(Vector3 position, Vector3 direction, int dmg, Entity owner) {

		Player p = Game.instance.player;
		float xOffset = (owner == p) ? 0f : 0f;
		float yOffset = (owner == p) ? 0f : 0f;
		float zOffset = (owner == p) ? 0f : 0.31f;

		MagicMissileProjectile projectile;

		if(magicMissileProjectile == null) {

			// Old and busted way
			projectile = new MagicMissileProjectile(position.x + xOffset, position.y + yOffset, position.z + zOffset, direction.x * speed, direction.z * speed, dmg, damageType, new Color(spellColor), owner);

			if (explosion != null) projectile.explosion = explosion;
			projectile.trailInterval = trailInterval;
			projectile.splashForce = splashForce;
			projectile.splashRadius = this.splashRadius;
			projectile.splashDamage = this.splashDamage;
			projectile.floating = this.floating;

			if(appearance != null) {
				projectile.spriteAtlas = appearance.texAtlas;
				projectile.tex = appearance.tex;
			}
		}
		else {

			// New, data driven way
			projectile = (MagicMissileProjectile)EntityManager.instance.Copy(magicMissileProjectile);

			projectile.x = position.x + xOffset;
			projectile.y = position.y + yOffset;
			projectile.z = position.z + zOffset + 0.1f;

			projectile.xa = direction.x * speed;
			projectile.ya = direction.z * speed;

			projectile.owner = owner;
			projectile.damage = dmg;
			projectile.damageType = damageType;

			if(spellColor != null) projectile.color = spellColor;
		}

		// Offset projectiles for monsters
		if(owner instanceof Monster) {
			projectile.z += ((Monster)owner).projectileOffset;
		}

		projectile.za = direction.y * speed;
		if(hitSound != null) projectile.hitSound = hitSound;

		return projectile;
	}
}
