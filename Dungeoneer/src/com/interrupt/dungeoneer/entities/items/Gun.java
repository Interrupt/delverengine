package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.projectiles.Projectile;
import com.interrupt.dungeoneer.entities.spells.Spell;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.managers.EntityManager;
import com.interrupt.managers.StringManager;

public class Gun extends Weapon {
    /** Is automatic fire? */
	@EditorProperty
	public boolean automatic = true;

	/** Time between shots in milliseconds. */
	@EditorProperty
	public float cycleTime = 6f;

	private float cycleTimer = 0f;

	private boolean canFire = true;

	/** Ammo type. Must correspond to a ItemStack stackType. */
	@EditorProperty
    public String ammoType = "BULLET";

	/** Ammo consumed per shot. */
    @EditorProperty
    public int ammoPerShot = 1;

    /** Sound to play when attempting to fire with no ammo. */
	@EditorProperty(group = "Gun Sounds")
    public String outOfAmmoSound = "button.mp3";

	/** Sound to play when firing. */
	@EditorProperty(group = "Gun Sounds")
	public String fireSound = "explode.mp3,explode_02.mp3,explode_03.mp3,explode_04.mp3";

	/** Explosion on hit. */
	Explosion hitEffect = null;

	/** Muzzle flash effect played when firing. */
	Explosion muzzleFlash = null;

	/** Number of projectiles per shot. */
	@EditorProperty(group = "Gun Projectiles")
    public int projectileNum = 1;

	/** Projectile horizontal spread. */
	@EditorProperty(group = "Gun Projectiles")
    public float projectileSpreadX = 0f;

	/** Projectile vertical spread. */
	@EditorProperty(group = "Gun Projectiles")
    public float projectileSpreadY = 0f;

	/** Number of particles for each projectile hit. */
	@EditorProperty(group = "Gun Projectiles")
    public int hitParticles = 7;

	/** Projectile to fire. Will use hitscan if null. */
	public Projectile projectile = null;

    /** Spell to fire. Will use hitscan if null. */
	public Spell spell = null;

    public Gun() { itemType = ItemType.wand; chargesAttack = false; }

	public Gun(float x, float y) {
		super(x, y, 16, ItemType.wand, StringManager.get("items.Gun.defaultNameText")); speed = 0.01f;
	}

	public String GetInfoText() {
		return super.GetInfoText();
	}

    @Override
    public String GetItemText() {
        return super.GetItemText();
    }

	@Override
	public void doAttack(Player p, Level lvl, float attackPower) {

	    boolean hasAmmo = useAmmo(p);

        cycleTimer = cycleTime;
        canFire = false;

	    if(!hasAmmo) {
            Audio.playSound(outOfAmmoSound, 1f);
            return;
        }

        if(p.handAnimation != null) p.handAnimation.stop();
        p.playAttackAnimation(this, attackPower);

        if(projectile != null) {
            doProjectileFire(p, lvl);
        }
        else if (spell != null) {
            doSpellFire(p, lvl);
        }
        else {
            doHitScanFire(p, lvl);
        }

        // gunfire effect!
        if(muzzleFlash == null) {
            muzzleFlash = new Explosion();
            muzzleFlash.color = Color.ORANGE;
            muzzleFlash.tex = 18;
            muzzleFlash.explosionStartTex = 18;
            muzzleFlash.explosionEndTex = 23;
            muzzleFlash.explosionAnimSpeed = 12;
            muzzleFlash.damage = 0;
            muzzleFlash.hidden = true;
            muzzleFlash.decalDirection = new Vector3(Game.camera.direction);
            muzzleFlash.lightMod = 1f;
            muzzleFlash.x = x;
            muzzleFlash.y = y;
            muzzleFlash.z = z - 0.3f;
            muzzleFlash.scale = 0.25f;
            muzzleFlash.lightMod = 0.6f;
            muzzleFlash.particleCount = 0;
            muzzleFlash.shakeAmount = 2f;
            muzzleFlash.shakeDistance = 2f;
            muzzleFlash.explosionLightLifetime = 6f;
            muzzleFlash.impulseAmount = 0f;
            muzzleFlash.fullbrite = true;
            muzzleFlash.explodeSound = null;
        }

        Explosion muzzleFlashCopy = (Explosion)KryoSerializer.copyObject(muzzleFlash);
		if(muzzleFlashCopy != null) {
            muzzleFlashCopy.x = x;
            muzzleFlashCopy.y = y;
            muzzleFlashCopy.z = z - 0.3f;
            lvl.SpawnNonCollidingEntity(muzzleFlashCopy);
        }

        Audio.playSound(fireSound, 1f);
	}

    private void doProjectileFire(Player p, Level lvl) {
        for(int pi = 0; pi < projectileNum; pi++) {
            Projectile fire_projectile = (Projectile) EntityManager.instance.Copy(projectile);
            fire_projectile.x = x;
            fire_projectile.y = y;
            fire_projectile.z = z;
            fire_projectile.owner = owner;
            fire_projectile.damage = doAttackRoll(1.0f, p);
            fire_projectile.damageType = damageType;

            Vector3 crosshairDirection = getCrosshairDirection(-0.4f);

            Vector3 fireDirection;
            if(crosshairDirection != null) {
                fireDirection = new Vector3(crosshairDirection);
            }
            else {
                fireDirection = new Vector3(Game.camera.direction);
            }

            if (projectileSpreadX != 0)
                fireDirection.rotate(Vector3.Y, (Game.rand.nextFloat() - 0.5f) * projectileSpreadX);

            if (projectileSpreadY != 0)
                fireDirection.rotate(getRightDirection(), (Game.rand.nextFloat() - 0.5f) * projectileSpreadY);

            fire_projectile.xa = fireDirection.x * fire_projectile.speed;
            fire_projectile.ya = fireDirection.z * fire_projectile.speed;
            fire_projectile.za = fireDirection.y * fire_projectile.speed;

            fire_projectile.x += fireDirection.x * 0.2f;
            fire_projectile.y += fireDirection.z * 0.2f;
            fire_projectile.z += fireDirection.y * 0.2f;

            fire_projectile.owner = p;

            lvl.SpawnEntity(fire_projectile);
        }
    }

    private Vector3 getRightDirection() {
        Vector3 rightDirection = new Vector3(Game.camera.direction).crs(Game.camera.up).nor();
        rightDirection.scl(-1);
        return rightDirection;
    }

    private void doSpellFire(Player p, Level lvl) {
        for(int pi = 0; pi < projectileNum; pi++) {
            Vector3 position = new Vector3(x, y, z);
            Vector3 crosshairDirection = getCrosshairDirection(-0.4f);

            Vector3 direction;
            if(crosshairDirection != null) {
                direction = new Vector3(crosshairDirection);
            }
            else {
                direction = new Vector3(Game.camera.direction);
            }

            if (projectileSpreadX != 0) {
                direction.rotate(Vector3.Y, (Game.rand.nextFloat() - 0.5f) * projectileSpreadX);
            }

            if (projectileSpreadY != 0) {
                direction.rotate(getRightDirection(), (Game.rand.nextFloat() - 0.5f) * projectileSpreadY);
            }

            spell.doCast(p, direction, position);
        }
    }

    private void doHitScanFire(Player p, Level lvl) {
        Vector3 levelIntersection = new Vector3();
        Vector3 testPos = new Vector3();

        for(int pi = 0; pi < projectileNum; pi++) {
            Ray ray = Game.camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);

            if(projectileSpreadX != 0)
                ray.direction.rotate(Vector3.Y, (Game.rand.nextFloat() - 0.5f) * projectileSpreadX);

            if(projectileSpreadY != 0)
                ray.direction.rotate(getRightDirection(), (Game.rand.nextFloat() - 0.5f) * projectileSpreadY);

            Vector3 hitNormal = new Vector3();
            boolean hitLevel = Collidor.intersectRayTriangles(ray, GameManager.renderer.GetCollisionTrianglesAlong(ray, 20f), levelIntersection, hitNormal);
            float worldHitDistance = new Vector3(ray.origin).sub(levelIntersection).len();
            float hitDistance = worldHitDistance;

            Entity hit = null;
            Vector3 hitEntityAt = new Vector3();

            Vector3 hitLocation = null;
            for (int i = 0; i < lvl.entities.size; i++) {
                Entity e = lvl.entities.get(i);
                if (!(e instanceof Sprite || e instanceof Light) && e.isSolid) {
                    testPos.x = e.x;
                    testPos.z = e.y;
                    testPos.y = e.z;

                    if(Intersector.intersectRayBounds(ray, CachePools.getAABB(e), hitEntityAt)) {
                        Vector3 start = new Vector3(ray.origin);
                        Vector3 end = new Vector3(hitEntityAt);
                        float distance = start.sub(end).len();

                        if (!hitLevel || (distance < hitDistance)) {
                            hitDistance = distance;
                            hit = e;
                            hitLocation = hitEntityAt.cpy();
                        }
                    }
                }
            }

            if (hit != null) {
                hit.hit(Game.camera.direction.x, Game.camera.direction.z, this.doAttackRoll(1.0f, p), 0.07f, getDamageType(), owner);
                hitNormal = Game.camera.direction.cpy().scl(-1f);
            } else if (hitLevel) {
                hitLocation = levelIntersection;
            }

            if (hitLocation != null) {
                Vector3 fromRay = new Vector3(ray.origin).add(new Vector3(hitLocation).scl(-1f));
                float length = fromRay.len();

                hitLocation = ray.getEndPoint(hitLocation, length - 0.1f);

                // hit!
                if (hitEffect == null) {
                    hitEffect = new Explosion();
                    hitEffect.color = Color.WHITE;
                    hitEffect.tex = 18;
                    hitEffect.explosionStartTex = 18;
                    hitEffect.explosionEndTex = 23;
                    hitEffect.explosionAnimSpeed = 22;
                    hitEffect.damage = 0;
                    hitEffect.decalDirection = new Vector3(hitNormal.x, hitNormal.z, hitNormal.y).scl(-1f).add(Game.rand.nextFloat() * 0.001f, Game.rand.nextFloat() * 0.001f, Game.rand.nextFloat() * 0.001f);
                    hitEffect.lightMod = 1f;
                    hitEffect.x = hitLocation.x;
                    hitEffect.y = hitLocation.z;
                    hitEffect.z = hitLocation.y;
                    hitEffect.lightMod = 0.2f;
                    hitEffect.particleCount = 0;
                    hitEffect.shakeAmount = 2f;
                    hitEffect.shakeDistance = 2f;
                    hitEffect.explosionLightLifetime = 6f;
                    hitEffect.impulseAmount = 0f;
                    hitEffect.hitDecal = new ProjectedDecal(ArtType.sprite, 19, 0.5f);
                    hitEffect.explodeSound = null;
                    hitEffect.fullbrite = false;
                    hitEffect.haloMode = HaloMode.NONE;
                }

                Explosion hitEffectCopy = (Explosion) KryoSerializer.copyObject(hitEffect);
                if (hitEffectCopy != null) {
                    hitEffectCopy.x = hitLocation.x;
                    hitEffectCopy.y = hitLocation.z;
                    hitEffectCopy.z = hitLocation.y;
                    hitEffectCopy.decalDirection = new Vector3(hitNormal.x, hitNormal.z, hitNormal.y).scl(-1f).add(Game.rand.nextFloat() * 0.001f, Game.rand.nextFloat() * 0.001f, Game.rand.nextFloat() * 0.001f);
                    lvl.SpawnEntity(hitEffectCopy);
                }

                if(hit == null) {
                    for (int ii = 0; ii < hitParticles; ii++) {
                        Particle part = CachePools.getParticle(hitLocation.x, hitLocation.z, hitLocation.y + 0.4f, hitNormal.x * 0.015f, hitNormal.z * 0.015f, hitNormal.y * 0.015f, 0, Color.GOLD, true);
                        part.x += hitNormal.x * 0.08f;
                        part.y += hitNormal.z * 0.08f;
                        part.z += hitNormal.y * 0.08f;
                        part.xa += (Game.rand.nextFloat() - 0.5f) * 0.02f;
                        part.ya += (Game.rand.nextFloat() - 0.5f) * 0.02f;
                        part.za += (Game.rand.nextFloat() - 0.5f) * 0.02f;
                        part.za += 0.02f;
                        part.lifetime = 2f + Game.rand.nextFloat() * 50f;
                        part.checkCollision = true;
                        part.endScale = 0.1f;
                        lvl.SpawnEntity(part);
                    }
                }
            }
        }
    }

    public boolean useAmmo(Player player) {

	    // infinite guns?
	    if(ammoType == null || ammoType.equals(""))
	        return true;

        for(int i = 0; i < player.inventory.size; i++) {
            Item check = player.inventory.get(i);
            if(check instanceof ItemStack) {
                ItemStack stack = (ItemStack)check;
                if(stack.stackType.equals(ammoType) && stack.count >= (Math.max(Math.abs(this.ammoPerShot), 1))) {
                    stack.count -= Math.max(Math.abs(this.ammoPerShot), 1);
                    if(stack.count == 0) player.removeFromInventory(stack);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void tickEquipped(Player player, Level level, float delta, String equipLoc) {
	    super.tickEquipped(player, level, delta, equipLoc);

        if(!canFire && automatic) {
            canFire = true;
        }

        if(cycleTimer > 0) cycleTimer -= delta;
    }

    public void resetTrigger() {
	    canFire = true;
    }

    public boolean canFire() {
	    return canFire && cycleTimer <= 0;
    }
}
