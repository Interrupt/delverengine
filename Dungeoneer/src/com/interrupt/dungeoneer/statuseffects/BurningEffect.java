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
    // Status Effect properties
	private float damageTimer = 60;
    private float dtimer = 0;

	public int damage = 1;

	// Fire particle properties
	private int particleCount = 8;

	private float spreadMod = 1f;
	private float baseSpreadMod = 1f;
	private float playerSpreadMod = 2.75f;

	private int startFireTexture = 64;
	private int stopFireTexture = 69;
	private float animationSpeed = 40f;

	private float particleLifetime = 90f;
    private float scale = 0.5f;
    private float startScale = 1f;
    private float endScale = 0.125f;

    private float upwardVelocity = 0.004f;

	// Audio settings
	private String burnSound = "mg_pass_poison.mp3";
	private float soundVolume = 0.5f;
	private float soundRange = 6f;

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

		if (dtimer > damageTimer) {
			dtimer = 0;
			owner.takeDamage(damage, DamageType.PHYSICAL, null);
			doFireEffect(owner);
			Audio.playPositionedSound(burnSound, new Vector3(owner.x,owner.y,owner.z), soundVolume, soundRange);
		}

		if (isEntityInWater()){
            active = false;
        }
	}

	public void doFireEffect(Entity owner) {
		if (!this.showParticleEffect) {
			return;
		}

		for (int i = 0; i < particleCount; i++) {
		    spawnFireParticle();
		}
	}

	@Override
	public void onStatusBegin(Actor owner) {
	    calculateSpreadMod();
		doFireEffect(owner);
		Fire fire = (Fire)owner.getAttached(Fire.class);

		// Attach a fire entity so that the fire can spread
		if (fire == null) {
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
		if (fire != null) fire.isActive = false;
	}


    /** Used for putting out the fire */
	private boolean isEntityInWater() {
        if (owner.inwater) {
            Tile waterTile = Game.instance.level.findWaterTile(owner.x, owner.y, owner.z, owner.collision);
            return waterTile.data != null && (waterTile.data.hurts <= 0 || waterTile.data.damageType != DamageType.FIRE);
        }
        return false;
    }

    /** Constructs new fire particle for spawning */
    private void spawnFireParticle() {

        Particle p = CachePools.getParticle();
        p.tex = startFireTexture;
        p.lifetime = particleLifetime;
        p.scale = scale;
        p.startScale = startScale;
        p.endScale = endScale;
        p.fullbrite = true;
        p.checkCollision = false;
        p.floating = true;
        p.x = owner.x + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
        p.y = owner.y + (Game.rand.nextFloat() * scale - (scale * 0.5f)) * spreadMod;
        p.z = owner.z + (Game.rand.nextFloat() * owner.collision.z) - 0.5f;
        p.playAnimation(startFireTexture, stopFireTexture, animationSpeed);

        p.za = Game.rand.nextFloat() * upwardVelocity + upwardVelocity;

        Game.GetLevel().SpawnNonCollidingEntity(p);
    }

    /** Calculates the Spread Modifier based on if the Entity is a Player instance */
    private void calculateSpreadMod() {
	    this.spreadMod = (owner instanceof Player) ? playerSpreadMod : baseSpreadMod;
    }

    public float getDamageTimer() {
        return damageTimer;
    }

    public void setDamageTimer(float damageTimer) {
        this.damageTimer = damageTimer;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void setParticleCount(int particleCount) {
        this.particleCount = particleCount;
    }

    public float getBaseSpreadMod() {
        return baseSpreadMod;
    }

    public void setBaseSpreadMod(float baseSpreadMod) {
        this.baseSpreadMod = baseSpreadMod;
    }

    public float getPlayerSpreadMod() {
        return playerSpreadMod;
    }

    public void setPlayerSpreadMod(float playerSpreadMod) {
        this.playerSpreadMod = playerSpreadMod;
    }

    public int getStartFireTexture() {
        return startFireTexture;
    }

    public void setStartFireTexture(int startFireTexture) {
        this.startFireTexture = startFireTexture;
    }

    public int getStopFireTexture() {
        return stopFireTexture;
    }

    public void setStopFireTexture(int stopFireTexture) {
        this.stopFireTexture = stopFireTexture;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(float animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public float getParticleLifetime() {
        return particleLifetime;
    }

    public void setParticleLifetime(float particleLifetime) {
        this.particleLifetime = particleLifetime;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getStartScale() {
        return startScale;
    }

    public void setStartScale(float startScale) {
        this.startScale = startScale;
    }

    public float getEndScale() {
        return endScale;
    }

    public void setEndScale(float endScale) {
        this.endScale = endScale;
    }

    public float getUpwardVelocity() {
        return upwardVelocity;
    }

    public void setUpwardVelocity(float upwardVelocity) {
        this.upwardVelocity = upwardVelocity;
    }

    public String getBurnSound() {
        return burnSound;
    }

    public void setBurnSound(String burnSound) {
        this.burnSound = burnSound;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }

    public float getSoundRange() {
        return soundRange;
    }

    public void setSoundRange(float soundRange) {
        this.soundRange = soundRange;
    }
}
