package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.game.*;
import com.interrupt.managers.EntityManager;

public class FusedBomb extends Item {
    enum triggerBehaviorType {light, explode}

    @EditorProperty(group = "Visual", type = "SPRITE_ATLAS_NUM")
    public int dudTex = 23;

    public boolean isLit = false;

    @EditorProperty
    public float chanceIsLit = 0.25f;

    @EditorProperty
    public float countdownTimer = 150f;

    @EditorProperty
    public float randomCountdownTimer = 50f;

    @EditorProperty
    public float chanceIsDud = 0.125f;

    public boolean isDud = false;

    public boolean wasSpawned = false;

    @EditorProperty
    public triggerBehaviorType triggerBehavior = triggerBehaviorType.light;

    public float explosionRadius = 3f;
    public float explosionImpulse = 0.2f;
    public float explosionDamage = 3f;
    public Weapon.DamageType explosionDamageType = Weapon.DamageType.PHYSICAL;
    public Color explosionColor = Colors.EXPLOSION;

    private final Color flashColor = new Color(Colors.BOMB_FLASH);

    private boolean isWet = false;

    Explosion explosion = new Explosion();

    Array<Entity> spawns = new Array<>();
    int spawnsCount = 1;
    public Vector3 spawnVelocity = new Vector3(0.0f, 0.0f, 0.0625f);
    public Vector3 spawnRandomVelocity = new Vector3(0.125f, 0.125f, 0.0625f);
    public Vector3 spawnSpread = new Vector3(0.125f, 0.125f, 0.0f);
    public float spawnMomentumTransfer = 1.0f;

    public float timerStart = -1f;

    public FusedBomb() {
        super.artType = ArtType.sprite;
        shadowType = ShadowType.BLOB;
        collision.set(0.15f, 0.15f, 0.25f);
        name = "Bomb";
    }

    @Override
    public void init(Level level, Level.Source source) {
        super.init(level, source);

        if (source == Level.Source.SPAWNED && !wasSpawned) {
            isLit = Game.rand.nextFloat() < chanceIsLit;
        }

        isDud = Game.rand.nextFloat() < chanceIsDud;
        wasSpawned = true;
    }

    @Override
    public void tick(Level level, float delta) {
        super.tick(level, delta);

        if (isWet) {
            isLit = false;
            color = Color.WHITE;
            fullbrite = false;
            return;
        }

        if (inwater) {
            fizzle(level);
        }

        isWet = inwater;

        if (timerStart < 0) {
            timerStart = countdownTimer + Game.rand.nextFloat() * randomCountdownTimer;
        }

        fullbrite = isLit;

        if (isLit) {
            color = flashColor;

            float sinMod = (float) Math.sin((timerStart / (countdownTimer + 22f)) * 15f) * 0.5f + 0.5f;
            if (sinMod < 0) sinMod = 0;
            if (sinMod > 1) sinMod = 1;

            flashColor.set(0.85f * sinMod + 0.15f, 0.35f * sinMod + 0.15f, 0.35f * sinMod + 0.15f, 1f);

            // Explode if the fuse runs out
            countdownTimer -= delta;
            if (countdownTimer <= 0) {
                if (isDud) {
                    tex = dudTex;
                    isLit = false;
                    isDud = Game.rand.nextFloat() < (chanceIsDud * 2f);
                    countdownTimer = timerStart * 0.25f;
                    color = Color.WHITE;
                    fullbrite = false;

                    fizzle(level);
                } else {
                    explode(level);
                }
            }
        }
    }

    @Override
    public void onTrigger(Entity instigator, String value) {
        // overload this to take actions when triggered
        if (triggerBehavior == triggerBehaviorType.light) {
            isLit = true;
        } else {
            explode(Game.instance.level);
        }
    }

    @Override
    public void tickEquipped(Player player, Level level, float delta, String equipLoc) {
        tick(level, delta);
    }

    public void explode(Level level) {
        isActive = false;
        isSolid = false;

        if (explosion != null) {
            explosion.initExplosion(x, y, z + yOffset, explosionImpulse, explosionRadius);
            explosion.color = explosionColor;
            explosion.color.a = 1f;
            explosion.damage = (int) explosionDamage;
            explosion.damageType = explosionDamageType;
            explosion.explode(level, 1f);
        }

        if (spawns != null && spawns.size > 0) {
            for (int i = 0; i < spawnsCount; i++) {
                // Grab a random spawn element to create
                int idx = Game.rand.nextInt(spawns.size);
                Entity e = EntityManager.instance.Copy(spawns.get(idx));

                // Preserve momentum of thrown bomb and add in random velocity
                e.xa = xa * spawnMomentumTransfer + spawnVelocity.x + Game.rand.nextFloat() * spawnRandomVelocity.x - spawnRandomVelocity.x * 0.5f;
                e.ya = ya * spawnMomentumTransfer + spawnVelocity.y + Game.rand.nextFloat() * spawnRandomVelocity.y - spawnRandomVelocity.y * 0.5f;
                e.za = za * spawnMomentumTransfer + spawnVelocity.z + Game.rand.nextFloat() * spawnRandomVelocity.z - spawnRandomVelocity.z * 0.5f;

                // Randomize positions
                e.x += x + Game.rand.nextFloat() * spawnSpread.x - spawnSpread.x * 0.5f + e.xa * 0.125f;
                e.y += y + Game.rand.nextFloat() * spawnSpread.y - spawnSpread.y * 0.5f + e.ya * 0.125f;
                e.z += z + Game.rand.nextFloat() * spawnSpread.z - spawnSpread.z * 0.5f + e.za * 0.125f;

                // Add it to the level. Important to use SpawnEntity because it calls Entity.init();
                level.SpawnEntity(e);
            }
        }
    }

    public void fizzle(Level level) {
        // make a bunch of small particles
        int pCount = 3;
        pCount *= Options.instance.gfxQuality;
        float particleForce = 0.5f;
        for (int i = 0; i < pCount; i++) {
            Particle p = CachePools.getParticle(x + Game.rand.nextFloat() * 0.24f - 0.12f, y + Game.rand.nextFloat() * 0.24f - 0.12f, z + Game.rand.nextFloat() * 0.04f - 0.02f, 0, 0, (Game.rand.nextFloat() * 0.005f + 0.005f) * particleForce, 40 + Game.rand.nextInt(20), 1f, 0f, 0, color, false);
            p.playAnimation(18, 23, p.lifetime - 0.0001f);
            p.floating = true;
            p.isSolid = false;
            p.maxVelocity = 0.01f;
            p.dampenAmount = 0.9f;

            level.SpawnNonCollidingEntity(p);
        }
    }

    private void onDamage(int damage, Weapon.DamageType damageType) {
        if (isActive) {
            // don't mess with explosives, they're volatile!
            boolean shouldExplode = Game.rand.nextInt(10) <= damage;

            if (shouldExplode || damageType == Weapon.DamageType.LIGHTNING) {
                // delay the explosion a tiny bit, because chaining explosions is fun!
                isLit = true;
                countdownTimer = Math.min(countdownTimer, 5);
            } else if (damage > 0 && (damageType == Weapon.DamageType.FIRE)) {
                isLit = true;
            }
        }
    }

    @Override
    public void hit(float projx, float projy, int damage, float knockback, Weapon.DamageType damageType, Entity instigator) {
        if (isActive) {
            super.hit(projx, projy, damage, knockback, damageType, instigator);
            onDamage(damage, damageType);
        }
    }

    @Override
    protected void damageItem(int damage, Weapon.DamageType damageType) {
        if (isActive) {
            onDamage(damage, damageType);
        }
    }

    @Override
    public void onChargeStart() {
        isLit = !isWet;
        countdownTimer = Math.max(timerStart * 0.2f, countdownTimer);
    }

    @Override
    public void onPickup() {
        isLit = false;
    }
}
