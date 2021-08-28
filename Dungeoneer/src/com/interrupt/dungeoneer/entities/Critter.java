package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.entities.projectiles.Projectile;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

import java.util.Random;

public class Critter extends AnimatedSprite {
    public Critter() { tex = 0; spriteAtlas = "critters"; dropSound = null; floating = true; animSpeed = 5f; }

    protected Vector3 wanderTarget = new Vector3();
    protected transient Vector3 tempCalc = new Vector3();

    @EditorProperty
    public float wanderTime = 5f;

    @EditorProperty
    public float randomMoveTimerAmount = 30f;

    @EditorProperty
    public float moveAmount = 0.3f;

    @EditorProperty
    public float moveSpeed = 1f;

    @EditorProperty
    public float moveFriction = 0.8f;

    @EditorProperty(type = "Squishing")
    public Actor.BloodType bloodType = Actor.BloodType.Insect;

    @EditorProperty(type = "Squishing")
    public boolean squishable = true;

    @EditorProperty(type = "Squishing")
    public String dieSound = "";

    @EditorProperty(type = "Scattering")
    public boolean scatters = true;

    @EditorProperty(type = "Scattering")
    public float scatterRadius = 0.75f;

    protected ProjectedDecal bloodPoolDecal = null;

    public float wanderTimer = 0f;

    protected Vector3 startLocation = new Vector3();
    protected Vector3 t_checkVec = new Vector3();

    private transient boolean otherTick = false;

    @Override
    public void tick(Level level, float delta) {
        super.tick(level, delta);

        otherTick = !otherTick;

        if(Game.instance != null) {
            Player p = Game.instance.player;
            hidden = (Math.abs(p.x - x) > 12 || Math.abs(p.y - y) > 12);
        }

        if(!hidden) {
            wanderTimer -= delta;
            if (wanderTimer <= 0) {
                float halfMoveAmount = moveAmount * 0.5f;

                float nextX = startLocation.x + Game.rand.nextFloat() * moveAmount - halfMoveAmount;
                float nextY = startLocation.y + Game.rand.nextFloat() * moveAmount - halfMoveAmount;
                float nextZ = startLocation.z + Game.rand.nextFloat() * moveAmount - halfMoveAmount;

                if(freeToMoveTo(nextX, nextY, nextZ, level)) {
                    wanderTimer = wanderTime + Game.rand.nextFloat() * randomMoveTimerAmount;
                    wanderTarget.set(nextX, nextY, nextZ);
                }
            }

            // scatter away from things!
            if(scatters && otherTick) {
                Array<Entity> near = level.spatialhash.getEntitiesAt(x, y, 1f);
                for(int i = 0; i < near.size; i++) {
                    Entity e = near.get(i);
                    if(e instanceof Actor || e instanceof Projectile) {
                        float offset = (e instanceof Player) ? 0.5f : 0.0f;
                        if((Math.abs(e.x - x + offset) + Math.abs(e.y - y + offset)) < scatterRadius) {
                            if(Math.abs(e.z - z) < e.collision.z) {
                                t_checkVec.set(x, y, z).sub(e.x + offset, e.y + offset, e.z).nor().scl(scatterRadius);
                                wanderTarget.set(startLocation).add(t_checkVec);
                                wanderTimer = wanderTime + Game.rand.nextFloat() * randomMoveTimerAmount;
                            }
                        }
                    }
                }
            }

            tempCalc.set(wanderTarget.x - x, wanderTarget.y - y, wanderTarget.z - z).scl(moveSpeed);

            // This is not accurate friction but critters should not do anything too expensive
            float cappedDelta = Math.min(delta, 1.0f);
            xa *= moveFriction * cappedDelta;
            ya *= moveFriction * cappedDelta;
            za *= moveFriction * cappedDelta;

            xa += tempCalc.x * 0.1f;
            ya += tempCalc.y * 0.1f;
            if(floating) za += tempCalc.z * 0.1f;

            if(isSolid) {
                stepHeight = collision.z * 2f;
                tickPhysics(level, delta);

                if(floating) {
                    za += tempCalc.z * 0.25f;
                    z += za;
                }
            }
            else {
                x += xa;
                y += ya;
                z += za;
            }
        }
    }

    @Override
    public void init(Level level, Level.Source source) {
        super.init(level, source);

        if(source == Level.Source.LEVEL_START) {
            wanderTarget.set(x,y,z);
            startLocation.set(x,y,z);
        }

        otherTick = Game.rand.nextBoolean();
    }

    @Override
    public void steppedOn(Entity e) {
        if (squishable && (e instanceof Actor)) {
            squish(0, 0);
        }
    }

    @Override
    public void hit(float projx, float projy, int damage, float knockback, Weapon.DamageType damageType, Entity instigator) {
        if(squishable && damage > 0) {
            squish(projx * knockback, projy * knockback);
        }
    }

    public boolean freeToMoveTo(float nx, float ny, float nz, Level level) {
        if(scatters) {
            Array<Entity> near = level.spatialhash.getEntitiesAt(nx, ny, 1f);
            for(int i = 0; i < near.size; i++) {
                Entity e = near.get(i);
                if(e instanceof Actor || e instanceof Projectile) {
                    float offset = (e instanceof Player) ? 0.5f : 0.0f;
                    if((Math.abs(e.x - nx + offset) + Math.abs(e.y - ny + offset)) < scatterRadius) {
                        if(Math.abs(e.z - nz) < e.collision.z) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void squish(float xv, float yv) {

        if(!isActive)
            return;

        isActive = false;

        Random r = Game.rand;
        int particleCount = 4;
        particleCount *= Options.instance.gfxQuality;

        for(int i = 0; i < particleCount; i++)
        {
            float xPos = x;
            float yPos = y;
            float zPos = z;

            float xVel = (r.nextFloat() - 0.5f) * 0.01f;
            float yVel = (r.nextFloat() - 0.5f) * 0.01f;

            xVel += xv * 0.5f;
            yVel += yv * 0.5f;

            Game.GetLevel().SpawnNonCollidingEntity( CachePools.getParticle(xPos, yPos, zPos, xVel, yVel, 0.0f, 420 + r.nextInt(600), 1f, 0f, Actor.getBloodTexture(bloodType), Actor.getBloodColor(bloodType), false)) ;
        }

        if (bloodPoolDecal != null && bloodPoolDecal.isActive) {
            ProjectedDecal proj = new ProjectedDecal(bloodPoolDecal);
            proj.decalHeight -= Game.rand.nextFloat() * 0.2f;
            proj.decalWidth = proj.decalHeight;
            proj.x = x;
            proj.y = y;
            proj.z = z + 0.2f;
            proj.direction = new Vector3(0.05f, 0, -0.95f).nor();
            proj.roll = Game.rand.nextFloat() * 360f;
            proj.end = 1f;
            proj.start = 0.01f;
            proj.isOrtho = true;

            Game.instance.level.entities.add(proj);
        }

        if(dieSound != null) {
            Audio.playPositionedSound(dieSound, new Vector3(x, y, z), 0.5f, 3f);
        }
    }
}
