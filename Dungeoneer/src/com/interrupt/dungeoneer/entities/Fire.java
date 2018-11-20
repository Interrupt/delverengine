package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.managers.EntityManager;

public class Fire extends AnimatedSprite {
    public float hurtTimer = 0f;

    @EditorProperty
    public float hurtTime = 160f;

    @EditorProperty
    public float spreadChance = 0.75f;

    public float spreadTimer = 0f;

    @EditorProperty
    public float spreadTime = 80f;

    @EditorProperty
    public float hurtChance = 1f;

    @EditorProperty
    public float radius = 1f;

    @EditorProperty
    public int hurtAmount = 1;

    @EditorProperty
    public float lifeTime = 400;

    @EditorProperty
    public float randomLifeTime = 80;

    @EditorProperty
    public String particleEffect = "Fire Effect";

    public float lifeTimeTimer = 0f;

    public float scaleMod = 1f;

    private Vector3 fireCollision = new Vector3(0.5f, 0.5f, 1.0f);

    public Fire() {
        this.isSolid = false;
        this.floating = true;
        this.tex = 0;
        this.endAnimTex = 7;
        this.spriteAtlas = "temple_particles";
        this.fullbrite = true;
        this.color = Color.WHITE;
        this.collision.x = 0.5f;
        this.collision.y = 0.5f;
        this.collision.z = 0.6f;
    }

    @Override
    public void init(Level level, Level.Source source) {
        super.init(level, source);

        this.lifeTime += Game.rand.nextFloat() * this.randomLifeTime;
        this.fireCollision.set(this.radius, this. radius, this.radius * 2);
    }

    DynamicLight light = null;
    Entity particleEmitter = null;
    AmbientSound ambientSound = null;

    @EditorProperty
    boolean burnsOut = true;

    @EditorProperty
    boolean makeLight = true;

    @EditorProperty
    boolean makeParticles = true;

    @EditorProperty
    boolean makeSound = true;

    @Override
    public void tick(Level level, float delta) {
        super.tick(level, delta);

        spreadTimer -= delta;
        if(spreadTimer < 0) {
            spreadTimer = spreadTime;
            spread(level);
        }

        hurtTimer -= delta;
        if(hurtTimer < 0) {
            hurtTimer = hurtTime;
            burn(level);
        }

        if(burnsOut) {
            lifeTimeTimer += delta;
            if (lifeTimeTimer > lifeTime) isActive = false;

            scale = Interpolation.circleOut.apply((lifeTime - lifeTimeTimer) / lifeTime) * scaleMod;
        }

        if(makeLight) {
            if (light == null) {
                light = new DynamicLight();
                light.lightType = DynamicLight.LightType.fire;
                attach(light);
            }

            light.range = scale * 1.75f;
            light.lightColor.set(2f, 1.6f, 1f);
            light.lightColor.set(1f, 0.5f, 0.2f);
            light.haloMode = HaloMode.BOTH;
        }

        if (particleEmitter == null && makeParticles) {
            particleEmitter = EntityManager.instance.getEntity("Generator", particleEffect);

            if(particleEmitter != null) {
                particleEmitter.z -= 0.2f;
                attach(particleEmitter);
            }
        }

        if(makeSound && ambientSound == null) {
            ambientSound = new AmbientSound();
            ambientSound.volume = 0.6f;
            ambientSound.soundFile = "torch.mp3";
            attach(ambientSound);
        }

        tickAttached(level, delta);
    }

    public void burn(Level level) {
        //Array<Entity> entities = level.getEntitiesColliding(x - 0.5f, y - 0.5f, z, collision, this);
        Array<Entity> entities = level.getEntitiesColliding(x, y, z, this.fireCollision, this);

        if(owner != null) {
            if(!entities.contains(owner, true)) {
                entities.add(owner);
            }
        }

        for(Entity e : entities) {
            boolean isDoorOrBreakable = e instanceof Breakable || (e instanceof Door && ((Door) e).breakable);
            if(e instanceof Actor || isDoorOrBreakable || e instanceof Player) {
                boolean isOnFire = e.getAttached(Fire.class) != null;
                if((!isOnFire || isDoorOrBreakable) && Game.rand.nextFloat() <= hurtChance) {
                    e.hit(0, 0, hurtAmount, hurtAmount, Weapon.DamageType.FIRE, this);
                }
            }
            else {
                e.hit(0, 0, hurtAmount, hurtAmount, Weapon.DamageType.FIRE, this);
            }
        }
    }

    public void spread(Level level) {
        Array<Entity> entities = level.getEntitiesColliding(x, y, z, collision, this);
        for(Entity e : entities) {
            if(e instanceof Breakable) {
                spreadTo(e, level);
            }
        }
    }

    public void spreadTo(Entity e, Level level) {
        Fire currentFire = (Fire)e.getAttached(Fire.class);
        boolean isOnFire = currentFire != null;

        if (Game.rand.nextFloat() <= spreadChance) {
            if (owner != e) {
                if(!isOnFire) {
                    Fire f = new Fire();
                    f.lifeTime = 300;
                    f.playAnimation();
                    f.hurtTimer = hurtTime * 0.75f;
                    f.spreadTimer = spreadTime * 0.75f;

                    if (e instanceof Breakable) {
                        f.z = e.collision.z / 1.25f;
                    }

                    e.attach(f);
                }
                else {
                    currentFire.lifeTimeTimer -= 75;
                    if(currentFire.lifeTimeTimer < 0) currentFire.lifeTimeTimer = 0;
                }
            }
        }
    }

    @Override
    public void updateDrawable() {
        super.updateDrawable();

        if(drawable != null && makeParticles) {
            drawable.scale = GameManager.renderer.editorIsRendering ? 1f : 0f;
        }

        // pull this sprite closer to the camera so that it doesn't overlap the owner
        if(owner != null) {
            if (drawable instanceof DrawableSprite) {
                DrawableSprite d = ((DrawableSprite) drawable);
                d.cameraPull = 0.02f;
                if(owner instanceof Player) {
                    d.cameraPull = -0.2f;
                }
            }
        }
    }
}
