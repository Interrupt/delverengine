package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.*;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;
import com.interrupt.managers.EntityManager;

public class Explosion extends Entity {
    /** Sound to play on explode. */
	public String explodeSound = "explode.mp3,explode_02.mp3,explode_03.mp3,explode_04.mp3";

	/** Direction to orient decal. */
	public Vector3 decalDirection = new Vector3(0.01f,0,-1f).nor();

	/** Decal */
	public ProjectedDecal hitDecal = null;

	/** Number of particles to spawn. */
	public int particleCount = 22;

	/** Initial particle speed. */
    public float particleForce = 1f;

    /** Particle sprite animation start index. */
    public int explosionStartTex = 1;

    /** Particle sprite animation end index. */
    public int explosionEndTex = 3;

    /** Particle sprite animation speed. */
    public float explosionAnimSpeed = 22f;
	
	// Physics
    /** Impulse range. */
	public float impulseDistance = 1f;

	/** Impulse strength */
	public float impulseAmount = 0.2f;

	/** Screen shake range. */
    public float shakeDistance = 5f;

    /** Screen shake strength. */
    public float shakeAmount = 3f;

    /** Light color scalar. */
    public float lightMod = 1.6f;

    /** Light color start value. */
    public Color explosionLightStartColor;

    /** Light color end value. */
    public Color explosionLightEndColor = Colors.EXPLOSION_LIGHT_END;

    /** Length of light color fade. */
    public float explosionLightLifetime = 40.0f;

    /** Time to delay explosion in milliseconds. */
    public float explodeDelay = 0;
    public float fuseTimer = 0;

    /** Create dust ring on explode? */
    public boolean makeDustRing = false;

    /** Dust ring color. */
    public Color dustRingColor;

    /** Create fly aways? */
    public boolean makeFlyAways = false;

    /** Fly away color. */
    public Color flyAwayColor;

    public float randomRoll = 0f;

    @Deprecated
    public boolean hasHalo = true;
    public HaloMode haloMode = HaloMode.NONE;
	
	// Damage!
    /** Damage amount. */
	public float damage = 0f;

	/** Damage type. */
	public DamageType damageType = DamageType.PHYSICAL;

    public transient Interpolation interpolation = Interpolation.pow2;

    /** List of entities to potentially spawn. */
    public Array<Entity> spawns = null;

    /** Number of entities to create. */
    int spawnsCount = 1;

    /** Spawned entity initial velocity. */
    public Vector3 spawnVelocity = new Vector3(0.0f, 0.0f, 0.0625f);

    /** Spawned entity initial random velocity. */
    public Vector3 spawnRandomVelocity = new Vector3(0.125f, 0.125f, 0.0625f);

    /** Spawned entity random position offset. */
    public Vector3 spawnSpread = new Vector3(0.125f, 0.125f, 0.0f);

    /** Amount of speed spawned entity receives. */
    public float spawnMomentumTransfer = 1.0f;

    /** Chance to apply status effect. */
    public float applyStatusEffectChance = 1.0f;

    /** Status effect. */
    public StatusEffect applyStatusEffect = null;
	
	public transient Entity owner = null;

	public Explosion() {
	    artType = ArtType.hidden;
        color = null;
        fullbrite = true;
	}

	public Explosion(float x, float y, float z, float impulse, float radius) {
		artType = ArtType.hidden;
        color = null;
        initExplosion(x, y, z, impulse, radius);
        fullbrite = true;
	}

    public void initExplosion(float x, float y, float z, float impulse, float radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.impulseAmount = impulse;
        this.impulseDistance = radius;
    }

    public void explode(Level level, float particleAmountMod) {

	    if(!isActive) return;

	    // make sure there's a color set
	    if(color == null) color = new Color(Color.WHITE);

        int detailLevel = Options.instance.graphicsDetailLevel;

        // make a bunch of small particles
        int pCount = particleCount;
        pCount *= Options.instance.gfxQuality;
        pCount *= particleAmountMod;
        for(int i = 0; i < pCount; i++)
        {
            Particle p = CachePools.getParticle(x - xa, y - ya, z + 0.48f, (Game.rand.nextFloat() * 0.04f - 0.02f) * particleForce, (Game.rand.nextFloat() * 0.04f - 0.02f) * particleForce, (Game.rand.nextFloat() * 0.08f - 0.02f) * particleForce, 200 + Game.rand.nextInt(100), 1f, 0f, 0, color, true);
            p.movementRotateAmount = 5f;

            // add a glow on high graphics
            if(detailLevel > 3) p.haloMode = HaloMode.CORONA_ONLY;

            level.SpawnNonCollidingEntity(p);
        }

        // play a sound
        Vector3 pos = new Vector3(x, y, z);
        Audio.playPositionedSound(explodeSound, pos, 0.75f, 12f);
        Game.instance.player.shake(shakeAmount, shakeDistance, pos);

        // make a decal
        makeDecal();

        // play an explosion animation
        Particle p = CachePools.getParticle();
        p.x = x;
        p.y = y;
        p.z = z;
        p.fullbrite = fullbrite;
        p.floating = true;
        p.artType = ArtType.particle;
        p.yOffset = yOffset;
        p.color.set(color);
        p.setSpriteAtlas(this.spriteAtlas);
        p.playAnimation(explosionStartTex, explosionEndTex, explosionAnimSpeed);
        p.checkCollision = false;
        p.scale = scale;
        p.endScale = scale;
        p.roll = (Game.rand.nextFloat() - 0.5f) * (randomRoll * 2f);
        p.collision.set(0.1f,0.1f,0.8f);

        level.non_collidable_entities.add(p);

        // make a light at this location
        Vector3 explosionLight = new Vector3(this.color.r, this.color.g, this.color.b);
        if (this.explosionLightStartColor != null) {
            explosionLight.set(this.explosionLightStartColor.r, this.explosionLightStartColor.g, this.explosionLightStartColor.b);
        }
        explosionLight.scl(lightMod);

        DynamicLight l = new DynamicLight(x, y, z + 0.2f, 4f, explosionLight);
        l.haloMode = HaloMode.BOTH;
        l.haloSizeMod = 3f;
        l.startLerp(new Vector3(this.explosionLightEndColor.r, this.explosionLightEndColor.g, this.explosionLightEndColor.b), 1, this.explosionLightLifetime, true);
        level.non_collidable_entities.add(l);


        // apply inpulses to physics objects
        Vector3 temp1 = new Vector3(x,y,z);
        Vector3 temp2 = new Vector3();
        Array<Entity> nearby = level.spatialhash.getEntitiesAt(x, y, impulseDistance);
        for(int i = 0; i < nearby.size; i++) {
            Entity n = nearby.get(i);
            if(n != owner) {
                temp2.set(n.x,n.y,n.z);

                // find out how far away from the explosion center this entity is
                temp2 = temp2.sub(temp1);
                float len = temp2.len();

                if(len <= impulseDistance) {
                    float mod = 1 - Math.min(len / impulseDistance, 1f);
                    temp2.nor();

                    // apply a physics impulse
                    n.applyPhysicsImpulse(temp2.scl(impulseAmount * mod));

                    // cause damage
                    int damageAmount = (int) (this.interpolation.apply(mod) * damage);
                    if (damageAmount > 0 || damageType != DamageType.PHYSICAL)
                        n.hit(temp2.x, temp2.y, damageAmount, 0f, damageType, this);

                    // might need to apply status effects
                    if(applyStatusEffect != null && n instanceof Actor) {
                        Actor a = (Actor)n;
                        float chance = applyStatusEffectChance;
                        if(Game.rand.nextFloat() < chance) {
                            StatusEffect copiedEffect = (StatusEffect)KryoSerializer.copyObject(applyStatusEffect);
                            if(copiedEffect != null) {
                                copiedEffect.timer *= 0.7f;
                                a.addStatusEffect(copiedEffect);
                            }
                        }
                    }
                }
            }
        }

        if(makeDustRing && Options.instance.graphicsDetailLevel >= 2) {
            makeDustRing(level);
        }

        if(this.makeFlyAways && Options.instance.graphicsDetailLevel >= 2) {
            this.makeFlyAways(level);
        }

        spawnStuff(level);
    }

    public void makeDustRing(Level level) {
        int amt = 16;
        float rotAmount = (3.14159f * 2f) / amt;
        float rot = 0;

        Vector3 cameraDir = Game.camera.direction;
        Particle spark = CachePools.getParticle(x, y, z, 0, 0, 0, 4, Color.WHITE, fullbrite);
        spark.x += cameraDir.x * 0.04f;
        spark.y += cameraDir.z * 0.04f;
        spark.z += cameraDir.y * 0.04f;
        spark.floating = true;
        spark.lifetime = 27;
        spark.scale = 2f;
        spark.color.set(1f, 1f, 1f, 0.1f);
        spark.endColor = new Color(1f, 1f, 1f, 1f);
        spark.shader = "spark";
        spark.spriteAtlas = "dust_puffs";
        level.SpawnNonCollidingEntity(spark);

        for(int i = 0; i < amt; i++) {
            // Make dust particle!
            Color particleColor = this.dustRingColor == null ? color : this.dustRingColor;
            Particle p = CachePools.getParticle(x, y, z - 0.12f + yOffset, 0, 0, 0, Game.rand.nextInt(3), particleColor, fullbrite);

            // Randomize location a tad
            p.x += (0.125f * Game.rand.nextFloat()) - 0.0625f;
            p.y += (0.125f * Game.rand.nextFloat()) - 0.0625f;

            p.checkCollision = false;
            p.floating = true;
            p.lifetime = (int) (44 * Game.rand.nextFloat()) + 60;
            p.shader = fullbrite ? "dust-fullbrite" : "dust";
            p.spriteAtlas = "dust_puffs";
            p.scale = 1.25f;
            p.endColor = new Color(p.color);
            p.endColor.a = 0f;

            p.za = (0.00125f * Game.rand.nextFloat()) + 0.0025f;
            p.za *= 4f;

            p.xa = (0 * (float) Math.cos(rot) + 1 * (float) Math.sin(rot)) * 1;
            p.ya = (1 * (float) Math.cos(rot) - 0 * (float) Math.sin(rot)) * 1;

            p.x += p.xa * 0.2f;
            p.y += p.ya * 0.2f;

            p.xa *= 0.15f;
            p.ya *= 0.15f;

            p.maxVelocity = 0.0001f;
            p.dampenAmount = 0.85f;
            //p.airFriction = 0.94f;

            level.SpawnNonCollidingEntity(p);

            rot += rotAmount;
        }
    }

    public void makeFlyAways(Level level) {
        int amt = 4;
        float rotAmount = (3.14159f * 2f) / amt;
        float rot = 0;

        for (int i = 0; i < amt; i++) {
            // Make dust particle!
            Color particleColor = this.flyAwayColor == null ? color : this.flyAwayColor;
            particleColor.a = 1f;

            Particle p = CachePools.getParticle(x, y, z - 0.12f, 0, 0, 0, 18, particleColor, fullbrite);

            // Randomize location a tad
            p.x += (0.125f * Game.rand.nextFloat()) - 0.0625f;
            p.y += (0.125f * Game.rand.nextFloat()) - 0.0625f;

            p.lifetime = 65;
            p.tex = 0;

            p.za = (0.05f * Game.rand.nextFloat()) + 0.05f;

            p.xa = (0 * (float) Math.cos(rot) + 1 * (float) Math.sin(rot)) * 1;
            p.ya = (1 * (float) Math.cos(rot) - 0 * (float) Math.sin(rot)) * 1;

            p.x += p.xa * 0.2f;
            p.y += p.ya * 0.2f;
            p.z += 0.65f;

            p.xa *= 0.05f;
            p.ya *= 0.05f;

            ParticleEmitter pe = new ParticleEmitter();
            pe.particleLifetime = 50;
            pe.particleStartTex = 0;
            pe.particleEndTex = 2;
            pe.pickRandomSprite = true;
            pe.shader = "dust";
            pe.particleAtlas = "dust_puffs";
            pe.particlesFloat = true;
            pe.particleSpawnInterval = 2;
            pe.particleSpawnCount = 1;
            pe.particlesCollide = false;
            pe.particlesFloat = true;
            pe.particleVelocity = new Vector3();
            pe.particleRandomVelocity = new Vector3();
            pe.particleRandomLifetime = 20;
            pe.particleStartScale = 0.75f;
            pe.particlesFullbrite = fullbrite;
            pe.particlesMoveRelativeToParent = false;
            pe.particleColor.set(this.flyAwayColor == null ? color : this.flyAwayColor);
            pe.particleEndColor = new Color(pe.particleColor);
            pe.particleEndColor.a = 0;

            pe.z = -0.5f;

            p.attach(pe);

            level.SpawnNonCollidingEntity(p);

            rot += rotAmount;
        }
    }
	
	@Override
	public void tick(Level level, float delta) {
	    if(explodeDelay == 0) {
            explode(level, 1f);
            isActive = false;
        }
        else {
            if(fuseTimer > explodeDelay) {
                explode(level, 1f);
                isActive = false;
            }
            fuseTimer += delta;
        }
	}
	
	public void makeDecal() {
		if(hitDecal != null) {
            if (makeDustRing) {
                for (int i = 0; i <= 5; i++) {
                    Vector3 direction = new Vector3(Vector3.Z).scl(-1f);
                    if (i == 1) direction.set(Vector3.X);
                    if (i == 2) direction.set(Vector3.Y);
                    if (i == 3) direction.set(Vector3.X).scl(-1f);
                    if (i == 4) direction.set(Vector3.Y).scl(-1f);
                    if (i == 5) direction.set(Vector3.Z);
                    direction.rotate(0.00001f, 1f, 1f, 1f);

                    ProjectedDecal decal = (ProjectedDecal) KryoSerializer.copyObject(hitDecal);
                    decal.direction = direction;
                    decal.x = x;
                    decal.y = y;
                    decal.z = z + yOffset;
                    if (i == 0) decal.z += 0.1f;
                    decal.roll = Game.rand.nextFloat() * 360f;
                    decal.end = hitDecal.decalWidth * 0.6f;
                    decal.start = 0.01f;
                    Game.instance.level.entities.add(decal);
                }
            }
            else {
                ProjectedDecal decal = (ProjectedDecal) KryoSerializer.copyObject(hitDecal);
                decal.direction = new Vector3(decalDirection);
                decal.x = x;
                decal.y = y;
                decal.z = z + yOffset;
                decal.roll = Game.rand.nextFloat() * 360f;
                decal.end = hitDecal.decalWidth * 0.6f;
                decal.start = 0.01f;
                Game.instance.level.entities.add(decal);
            }
        }
	}

	public void spawnStuff(Level level) {
        if (this.spawns != null && this.spawns.size > 0) {
            for (int i = 0; i < this.spawnsCount; i++) {
                // Grab a random spawn element to create
                int idx = Game.rand.nextInt(this.spawns.size);
                Entity e = EntityManager.instance.Copy(this.spawns.get(idx));

                // Preserve momentum of thrown bomb and add in random velocity
                e.xa = this.xa * this.spawnMomentumTransfer + this.spawnVelocity.x + Game.rand.nextFloat() * this.spawnRandomVelocity.x - this.spawnRandomVelocity.x * 0.5f;
                e.ya = this.ya * this.spawnMomentumTransfer + this.spawnVelocity.y + Game.rand.nextFloat() * this.spawnRandomVelocity.y - this.spawnRandomVelocity.y * 0.5f;
                e.za = this.za * this.spawnMomentumTransfer + this.spawnVelocity.z + Game.rand.nextFloat() * this.spawnRandomVelocity.z - this.spawnRandomVelocity.z * 0.5f;

                // Randomize positions
                e.x += this.x + Game.rand.nextFloat() * this.spawnSpread.x - this.spawnSpread.x * 0.5f + e.xa * 0.125f;
                e.y += this.y + Game.rand.nextFloat() * this.spawnSpread.y - this.spawnSpread.y * 0.5f + e.ya * 0.125f;
                e.z += this.z + Game.rand.nextFloat() * this.spawnSpread.z - this.spawnSpread.z * 0.5f + e.za * 0.125f;

                // Add it to the level. Important to use SpawnEntity because it calls Entity.init();
                level.SpawnEntity(e);
            }
        }
    }
}
