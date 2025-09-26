package com.interrupt.dungeoneer.gfx.animation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.managers.EntityManager;

public class ProjectileAttackAction extends AnimationAction {

	/** The entity to use as a projectile */
	public Entity projectile;

	/** Scales the offset for the projectile when spawning */
	public Vector3 projectileOffset = new Vector3();

	/** Scales how much arc the monster gives to projectile. */
	float projectileBallisticsMod = 0.0775f;

	/** Initial velocity of projectile. */
	protected float projectileSpeed = 0.15f;

	/** Number of projectiles to spawn */
	protected float projectileNum = 1;

	/** Projectile horizontal spread. */
	protected float projectileSpreadX = 0f;

	/** Projectile vertical spread. */
	protected float projectileSpreadY = 0f;

	public ProjectileAttackAction() { }

	// ugh, when will Java get value types?
	private static transient Vector3 workVector3d_1 = new Vector3();
	private static transient Vector3 workVector3d_2 = new Vector3();
	private static transient Vector2 workVector2d_1 = new Vector2();
	private static transient Vector2 workVector2d_2 = new Vector2();

	@Override
	public void doAction(Entity instigator) {
		if(projectile == null)
			return;

		for(int i = 0; i < projectileNum; i++) {
			Entity pCopy = null;
			if (projectile instanceof Prefab) {
				Prefab p = (Prefab) projectile;
				pCopy = EntityManager.instance.getEntity(p.category, p.name);
			} else {
				pCopy = (Entity) KryoSerializer.copyObject(projectile);
			}

			if (pCopy != null) {
				Player player = Game.instance.player;
				pCopy.owner = instigator;
				pCopy.ignorePlayerCollision = false;

				// spawns from this entity
				pCopy.x = instigator.x;
				pCopy.y = instigator.y;
				pCopy.z = instigator.z + (instigator.collision.z * 0.6f);

				// initial instigator to player direction, for the projectile offset
				Vector3 dirToPlayer = workVector3d_1.set(player.x, player.y, 0);
				dirToPlayer.sub(pCopy.x, pCopy.y, 0);
				float playerdist = dirToPlayer.len();

				// have the distance, can normalize now
				dirToPlayer.nor();

				// offset projectile based on the rotation
				Vector2 dirToPlayer2d = workVector2d_1.set(dirToPlayer.x, dirToPlayer.y);
				Vector2 spawnOffset = workVector2d_2.set(projectileOffset.x, projectileOffset.y);
				spawnOffset.rotate(dirToPlayer2d.angle());

				pCopy.x += spawnOffset.x;
				pCopy.y += spawnOffset.y;
				pCopy.z += projectileOffset.z;

				// get direction from the projectile to the player. aim for center mass!
				dirToPlayer.set(player.x, player.y, player.z + player.collision.z * 0.5f);
				dirToPlayer.sub(pCopy.x, pCopy.y, pCopy.z);
				dirToPlayer.nor();

				// apply spread, if needed
				Vector2 rightHandDirection2d = workVector2d_2.set(0.0f, 1.f);
				rightHandDirection2d.rotate(dirToPlayer2d.angle());
				Vector3 rightHandDirection = workVector3d_2.set(rightHandDirection2d.x, rightHandDirection2d.y, 0f);

				// offset for spread if needed
				if (projectileSpreadX != 0)
					dirToPlayer.rotate(Vector3.Z, (Game.rand.nextFloat() - 0.5f) * projectileSpreadX);

				if (projectileSpreadY != 0)
					dirToPlayer.rotate(rightHandDirection, (Game.rand.nextFloat() - 0.5f) * projectileSpreadY);

				// offset direction for ballistics if needed
				if (!pCopy.floating) {
					// Go ballistics
					dirToPlayer.rotate(rightHandDirection, (playerdist * playerdist) * -projectileBallisticsMod);
				}

				// initial speed
				dirToPlayer.scl(projectileSpeed);

				pCopy.xa = dirToPlayer.x;
				pCopy.ya = dirToPlayer.y;
				pCopy.za = dirToPlayer.z;

				// Some items trigger effects when thrown
				if(pCopy instanceof Item)
					((Item)pCopy).tossItem(Game.instance.level, 1.0f);

				Game.instance.level.SpawnEntity(pCopy);
			}
		}
	}
}
