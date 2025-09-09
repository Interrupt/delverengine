package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.projectiles.MagicMissileProjectile;
import com.interrupt.dungeoneer.game.Game;

public class SpreadMagicMissile extends MagicMissile {
    /** Projectile count. */
    public int numProjectiles = 5;

    /** Horizontal spread distance. */
    public float xSpread = 5f;

    /** Vertical spread distance. */
    public float ySpread = 5f;

    Vector3 tempDir = new Vector3();

    public SpreadMagicMissile() { trailInterval = 2f; splashForce = 0.005f; }

    @Override
	public void doCast(Entity owner, Vector3 direction, Vector3 position) {
		
		int dmg = doAttackRoll() / numProjectiles;
        if(dmg < 1) dmg = 1;

        for(int i = 0; i < numProjectiles; i++) {
            MagicMissileProjectile projectile = makeProjectile(position, direction, dmg, owner);

            if(magicMissileProjectile == null) {
                projectile.particleAmoundMod = 1f / numProjectiles;
                projectile.lightMod = 0.5f;
            }

            // rotate in the right direction
            tempDir.set(direction);
            tempDir.rotate((xSpread * -0.5f) + xSpread * ((float)i / (float)(numProjectiles - 1)), 0, 1f, 0f);
            tempDir.rotate((Game.rand.nextFloat() * ySpread) - (ySpread * 0.5f), 0, 0, 1f);

            projectile.za = tempDir.y * speed;
            projectile.xa = tempDir.x * speed;
            projectile.ya = tempDir.z * speed;

            if (hitSound != null) projectile.hitSound = hitSound;
            Game.GetLevel().entities.add(projectile);
        }
	}
}
