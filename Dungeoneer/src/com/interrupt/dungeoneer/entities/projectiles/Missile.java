package com.interrupt.dungeoneer.entities.projectiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.ItemStack;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.entities.triggers.Trigger;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.drawables.DrawableBeam;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.interfaces.Directional;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.StringManager;

import java.lang.Math;
import java.util.Random;

public class Missile extends Item implements Directional {
    /** Damage amount. */
    public int damage = 1;

    /** Strength of knockback effect. */
    public float knockback = 0;

    private boolean showHitEffect = false;

    public float trailTimer = 0;

    /** Should missile leave a particle trail? */
    public boolean leaveTrail = false;

    /** Particle trail spawn interval. */
    public float trailInterval = 1f;

    /** Particle trail lifetime. */
    public float effectLifetime = 0f;

    /** Damage type. */
    public DamageType damageType = DamageType.PHYSICAL;

    private float shakeTimer = 0f;
    private float bounceTimer = 0f;

    /** Is missile stuck in something? */
    boolean stuck = false;

    /** Stack type. */
    public String stackType = "ARROW";

    /** Should missile stick into walls? */
    @EditorProperty
    public boolean sticksInWall = true;

    /** Change to break when hitting something. */
    @EditorProperty
    public float breakChance = 0.1f;

    /** Missile rotation. */
    public Vector3 rotation = new Vector3();

    Vector3 t_direction = new Vector3(1, 0, 0);
    public transient Vector3 dirWork = new Vector3();

    /** Missile initial speed. */
    @EditorProperty
    public float initialSpeed = 0f;

    public enum HitEffect {NONE, BULLET, ARROW}

    /** Hit effect. */
    @EditorProperty
    public HitEffect hitEffect = HitEffect.ARROW;

    public Missile() {
        super(0, 0, 73, ItemType.thrown, StringManager.get("items.Missile.defaultNameText"));
        itemType = ItemType.thrown;
        ignorePlayerCollision = true;
        collision.set(0.1f, 0.1f, 0.1f);
        stepHeight = 0;
        yOffset = -0.45f;
        shadowType = ShadowType.BLOB;
        canStepUpOn = false;
        scale = 2f;
        floating = false;

        DrawableBeam beam = new DrawableBeam(tex, ArtType.item);
        beam.dir.set(1f, 0f, 0f);
        beam.artType = ArtType.item;
        beam.scale = 2f;
        beam.drawOffset.set(0f, 0f, -0.45f);
        beam.yOffset = -0.45f;
        beam.size = 2f;
        beam.beamCrossOffset = 0.016f;
        beam.centerOffset = 0f;

        this.drawable = beam;
    }

    @Override
    public void init(Level level, Level.Source source) {
        if (this.initialSpeed != 0f) {
            this.dirWork.set(this.getDirection()).nor().scl(this.initialSpeed);
            this.xa = this.dirWork.x;
            this.ya = this.dirWork.y;
            this.za = this.dirWork.z;
            this.dirWork.set(0, 0, 0);

            this.stuck = false;

            this.setRotation(this.rotation.x, this.rotation.y, this.rotation.z);
        }
    }

    public Missile(int tex, String name) {
        this();
        this.tex = tex;
        this.name = name;
        ignorePlayerCollision = true;
        floating = false;

        setDirection();

        canStepUpOn = false;
    }

    public Missile(Vector3 pos, Vector3 dir, int tex, Entity owner) {
        super(pos.x, pos.y, tex, ItemType.thrown, StringManager.get("items.Missile.defaultNameText"));

        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z + 0.55f;

        this.xa = dir.x;
        this.ya = dir.y;
        this.za = dir.z;

        this.tex = tex;

        floating = false;

        collision.set(1f / 64f, 1f / 64f, 1f / 32f);

        yOffset = -0.45f;
        stepHeight = 0;
        ignorePlayerCollision = true;

        setDirection();

        canStepUpOn = false;
    }

    public void rotate90() {
        this.rotation.z -= 90f;
    }

    public void rotate90Reversed() {
        this.rotation.z += 90f;
    }

    public void setRotation(float rotX, float rotY, float rotZ) {
        this.rotation.x = rotX;
        this.rotation.y = rotY;
        this.rotation.z = rotZ;

        Vector3 dir = this.getDirection();
        this.t_direction.set(dir.x, dir.z, dir.y);
        drawable.dir.set(dir.x, dir.z, dir.y);
    }

    public void rotate(float rotX, float rotY, float rotZ) {
        this.rotation.x += rotX;
        this.rotation.y += rotY;
        this.rotation.z += rotZ;

        Vector3 dir = this.getDirection();
        this.t_direction.set(dir.x, dir.z, dir.y);
        drawable.dir.set(dir.x, dir.z, dir.y);
    }

    public Vector3 getRotation() {
        return this.rotation;
    }

    public Vector3 getDirection() {
        Vector3 dir = dirWork.set(1, 0, 0);
        dir.rotate(Vector3.Y, -this.rotation.y);
        dir.rotate(Vector3.X, -this.rotation.x);
        dir.rotate(Vector3.Z, -this.rotation.z);
        return dir;
    }

    public void setDirection() {
        if (drawable == null) {
            drawable = new DrawableBeam(tex, ArtType.item);
        }

        // set rotation
        if (this.isOnFloor) za = 0;

        if (Math.abs(this.xa) > 0.0000001 || Math.abs(this.ya) > 0.0000001 || Math.abs(this.za) > 0.0000001) {
            Vector3 dir2 = t_direction.set(xa, za, ya);
            if (drawable instanceof DrawableMesh) {
                dir2.x *= -1f;
                dir2.y *= -1f;
            }
            dir2 = dir2.nor();

            if (!dir2.isZero())
                drawable.dir.set(dir2);
        }
        else {
            drawable.dir.set(t_direction);
        }
    }

    public void SetPositionAndVelocity(Vector3 pos, Vector3 dir) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z + 0.55f;

        this.xa = dir.x;
        this.ya = dir.y;
        this.za = dir.z;

        setDirection();
    }

    private transient Vector3 levelIntersection = new Vector3();
    private transient Vector3 intersectionNormal = new Vector3();
    private transient Ray ray = new Ray(new Vector3(), new Vector3());

    public boolean lineCollidesWithLevel(Vector3 position, Vector3 nextPosition) {
        if (drawable == null) return false;

        v0.set(nextPosition).sub(position);
        float distanceTraveled = v0.len();

        // Ray for intersection tests.
        ray.set(position, v0.nor());

        boolean hit = Collidor.intersectRayTriangles(ray, GameManager.renderer.GetCollisionTrianglesNear(this), levelIntersection, intersectionNormal);
        if (hit) {
            float worldHitDistance = v0.set(position).sub(levelIntersection).len();
            return worldHitDistance <= distanceTraveled;
        }

        return false;
    }

    // Ugly, should move to a generic method in Entity
    // Don't collide with static meshes, already checking against triangles there
    public boolean canHit(Entity checking, Entity e) {
        if (e == owner || checking == e) return false;
        else if (checking instanceof Model && checking.isStatic)
            return false; // don't let missiles collide with static meshes
        else if (checking != null && checking.ignorePlayerCollision && Game.instance.player != null && e == Game.instance.player)
            return false;
        else if (checking != null && e.ignorePlayerCollision && Game.instance.player != null && checking == Game.instance.player)
            return false;
        else if (checking != null && e.isDynamic && checking.collidesWith == CollidesWith.staticOnly)
            return false;
        else if (checking != null && e.collidesWith == CollidesWith.staticOnly && checking.isDynamic)
            return false;
        else if (checking != null && e.collidesWith == CollidesWith.nonActors && checking instanceof Actor)
            return false;
        else if (checking != null && e.collidesWith == CollidesWith.actorsOnly && !(checking instanceof Actor))
            return false;
        else if (checking != null && checking.collidesWith == CollidesWith.nonActors && e instanceof Actor)
            return false;
        else if (checking != null && checking.collidesWith == CollidesWith.actorsOnly && !(e instanceof Actor))
            return false;
        return true;
    }

    private final transient Array<Entity> entityHitList = new Array<>();
    private final transient Vector3 v0 = new Vector3();

    public Array<Entity> lineCollidesWithEntities(Vector3 position, Vector3 nextPosition, Vector3 levelHitLocation) {
        Array<Entity> possibles = Game.instance.level.getEntitiesAlongLine(position.x, position.z, nextPosition.x, nextPosition.z);
        entityHitList.clear();

        // Calculate distance to world hit.
        float worldHitDistance = Float.POSITIVE_INFINITY;
        if (levelHitLocation != null) {
            worldHitDistance = v0.set(levelHitLocation).sub(position).len();
        }

        // Movement delta between current and next position.
        v0.set(nextPosition).sub(position);
        float distanceTraveled = v0.len();

        // Ray for intersection tests.
        ray.set(position, v0.nor());

        for (Entity e : possibles) {
            if (!e.isActive) continue;
            if (!e.isDynamic) continue;
            if (!canHit(this, e)) continue;

            BoundingBox b = CachePools.getAABB(e);

            if (Intersector.intersectRayBounds(ray, b, v0)) {
                float hitDistance = v0.sub(position).len();
                if (hitDistance <= distanceTraveled && hitDistance < worldHitDistance) {
                    entityHitList.add(e);
                }
            }

            CachePools.aabbPool.free(b);
        }

        return entityHitList;
    }

    private transient Vector3 nextPosition = new Vector3();
    private transient Vector3 currentPosition = new Vector3();

    @Override
    public void tick(Level level, float delta) {
        yOffset = -0.45f;

        if (bounceTimer > 0) bounceTimer -= delta;

        // setup some position vectors for line checks
        currentPosition.set(x, z + yOffset, y);
        nextPosition.set(x + xa, z + za + yOffset, y + ya);

        workVec.set(xa, ya, za);

        // Moving fast enough to damage actors?
        isSolid = false;
        collidesWith = (workVec.len() > 0.01f) ? CollidesWith.all : CollidesWith.nonActors;

        // If not moving fast enough, only let things collide with us when we're stuck so that we can unstick
        if (!stuck && collidesWith == CollidesWith.nonActors)
            collidesWith = CollidesWith.staticOnly;

        setDirection();

        if (!stuck) {
            boolean hitLevel = lineCollidesWithLevel(currentPosition, nextPosition);
            Array<Entity> colliding = lineCollidesWithEntities(currentPosition, nextPosition, hitLevel ? levelIntersection : null);

            if (!hitLevel && colliding.size == 0) {
                // The space is free, arrow can move
                x += xa;
                y += ya;
                z += za;

                // apply gravity
                if (!floating && !stuck) za -= 0.0035f * delta;
            }
            else {
                for (Entity e : colliding) {
                    // Hit an entity!
                    this.encroached(e);
                    e.encroached(this);
                }

                if (isActive && hitLevel) {
                    hitWorld(xa, ya, za);
                }
            }

            // Splash?
            Tile cTile = level.getTile((int) Math.floor(x), (int) Math.floor(y));
            if (cTile.data.isWater && nextPosition.y < cTile.floorHeight + 0.32f) {
                if (currentPosition.y >= cTile.floorHeight + 0.32f) {
                    splash(level, cTile.floorHeight + 0.5f, true, cTile);
                }
            }
        }

        // Handle being exploded or other physics impulses
        if (this.stuck && (Math.abs(this.xa) > 0.00001 || Math.abs(this.ya) > 0.00001 || Math.abs(this.za) > 0.00001)) {
            this.stuck = false;
        }

        // wiggle!
        float shake = 0f;
        if (shakeTimer > 0) {
            shake = (float) Math.sin(Game.instance.time * 0.9f) * shakeTimer * (2f);
            shakeTimer -= delta * 0.02f;
        }

        if (drawable instanceof DrawableBeam) {
            ((DrawableBeam) drawable).size = 2f;
            ((DrawableBeam) drawable).fullbrite = false;
            ((DrawableBeam) drawable).scale = 1.0f;
            ((DrawableBeam) drawable).centerOffset = 0f;
            ((DrawableBeam) drawable).beamCrossOffset = 0.016f;

            if (shake != 0) {
                ((DrawableBeam) drawable).dir.rotate(Vector3.X, shake);
                ((DrawableBeam) drawable).dir.rotate(Vector3.Y, shake);
            }

            this.fullbrite = false;
        }

        if (this.showHitEffect) {
            this.doHitEffect(x, y, z - 0.5f, level);
            this.showHitEffect = false;
        }

        if (leaveTrail) {
            if (trailTimer > 0) {
                trailTimer -= delta;
            }
            else {
                trailTimer = trailInterval + Game.rand.nextFloat() * (trailInterval * 5f);
                trailTimer += (1f - Options.instance.gfxQuality);
                float driftSpeed = 0.001f;
                float driftSpeedHalf = driftSpeed / 2f;
                float upwardVelocity = 0.00625f;
                float xPositionVariance = (0.1f * Game.rand.nextFloat()) - 0.05f;
                float yPositionVariance = (0.1f * Game.rand.nextFloat()) - 0.05f;
                Particle p = CachePools.getParticle(x - xa + xPositionVariance, y - ya + yPositionVariance, z - za - 0.5f, (Game.rand.nextFloat() * driftSpeed) - driftSpeedHalf, (Game.rand.nextFloat() * driftSpeed) - driftSpeedHalf, (Game.rand.nextFloat() * driftSpeed) - driftSpeedHalf + upwardVelocity, 0, this.color, true);
                p.floating = true;
                p.lifetime = (int) (10 * Game.rand.nextFloat()) + 30;
                p.startScale = 1.0f;
                p.endScale = 1.0f;
                p.scale = 0.5f;
                p.checkCollision = false;
                p.playAnimation(18, 23, p.lifetime);
                Game.GetLevel().SpawnNonCollidingEntity(p);
            }
        }

        if (this.effectLifetime > 0) {
            this.effectLifetime -= delta;
        }
        else {
            if (this.leaveTrail) {
                this.showHitEffect = true;
            }

            this.leaveTrail = false;

            if (this.attached != null) {
                for (int i = 0; i < this.attached.size; i++) {
                    Entity e = this.attached.get(i);

                    if (e instanceof DynamicLight) {
                        e.isActive = false;
                    }
                }
            }
        }

        tickAttached(level, delta);
    }

    public void bounceOffOf(Entity hit) {

        if (bounceTimer > 0) {
            bounceTimer = 3f;
            return;
        }

        bounceTimer = 3f;

        // Reflection off of a plane is: −(2(n · v) n − v)
        Vector3 normal = new Vector3(x, y, 0).sub(new Vector3(hit.x, hit.y, 0));
        Vector3 forward = new Vector3(this.xa, this.ya, this.za);

        if (stuck) forward.set(normal).scl(-2f);

        float mag = forward.len();
        forward = forward.nor();

        Vector3 newAngle = new Vector3(normal).sub(forward).scl((2 * normal.dot(forward)));

        // Add some friction as well, arrows are not perfectly elastic
        newAngle.scl(-0.05f);
        xa = newAngle.x;
        ya = newAngle.y;
        za = newAngle.z;

        x += xa * 2f;
        y += ya * 2f;
        z += za * 2f;

        stuck = false;
    }

    public void maybeBreak() {
        if (!sticksInWall || (!stuck && Game.rand.nextFloat() <= breakChance)) {
            this.doBreak();
        }
    }

    public void doBreak() {
        isActive = false;
        slideEffectTimer = 0;

        super.hitWorld(xa, ya, za);
        Level lvl = Game.GetLevel();

        if (hitEffect == HitEffect.ARROW) {
            // TODO: Play audio break

            // Create broken arrow particles
            Vector3 breakVelocity = CachePools.getVector3((Game.rand.nextFloat() * 2.0f) - 1.0f, (Game.rand.nextFloat() * 2.0f) - 1.0f, Game.rand.nextFloat());
            breakVelocity.scl(0.01f);

            float brokenArrowLifetime = 1000f;

            Particle p = CachePools.getParticle(x, y, z, breakVelocity.x, breakVelocity.y, breakVelocity.z, brokenArrowLifetime, 43, Color.WHITE, false);
            p.scale = 0.5f;
            p.endScale = 0.5f;
            p.movementRotateAmount = 10f;
            lvl.SpawnNonCollidingEntity(p);

            breakVelocity.set(-breakVelocity.x, -breakVelocity.y, breakVelocity.z);
            p = CachePools.getParticle(x, y, z, breakVelocity.x, breakVelocity.y, breakVelocity.z, brokenArrowLifetime, 44, Color.WHITE, false);
            p.scale = 0.5f;
            p.endScale = 0.5f;
            p.movementRotateAmount = -10f;
            lvl.SpawnNonCollidingEntity(p);

            CachePools.freeVector3(breakVelocity);
        }
    }

    public void addArrowLootToMonster(Monster m) {
        if (m.loot == null) {
            m.loot = new Array<Item>();
        }

        boolean addedArrow = false;
        for (int i = 0; i < m.loot.size; i++) {
            Item check = m.loot.get(i);
            if (check instanceof ItemStack && ((ItemStack) check).stackType.equals(this.stackType)) {
                ItemStack stack = (ItemStack) check;
                stack.count++;
                addedArrow = true;
                break;
            }
        }

        // Don't add twice
        if (!addedArrow) {
            ItemStack stack = new ItemStack(this, 1, name);
            stack.tex = tex;
            stack.spriteAtlas = spriteAtlas;
            stack.stackType = stackType;
            stack.name = name;

            m.loot.add(stack);
            stack.collidesWith = CollidesWith.staticOnly;
        }
    }

    @Override
    public void encroached(Player player) {
        float speed = (Math.abs(xa) + Math.abs(ya) + Math.abs(za));

        if (speed > 0.2f && this.canHit(this, player)) {
            this.encroached((Entity) player);
            this.doBreak();
        }
    }

    @Override
    public void encroached(Entity hit) {
        if (owner != null && hit == owner) {
            return;
        }

        if (hit.isStatic || hit instanceof Trigger) {
            bounceOffOf(hit);
            maybeBreak();
        }
        else if (hit.isDynamic) {
            float speed = (Math.abs(xa) + Math.abs(ya) + Math.abs(za));

            Vector2 knockbackDirection = new Vector2(xa, ya).nor();

            if (speed < 0.15f) {
                bounceOffOf(hit);
                return;
            }

            hit.hit(knockbackDirection.x, knockbackDirection.y, damage, !stuck ? knockback : 0.05f, this.damageType, owner);
            Audio.playSound("hit.mp3,hit_02.mp3,hit_03.mp3,hit_04.mp3", speed * 6f);

            if (speed > 0.2f && hit instanceof Monster) {
                Monster m = (Monster) hit;
                maybeBreak();
                if (isActive) {
                    addArrowLootToMonster(m);
                    isActive = false;
                }
            }
            else {
                bounceOffOf(hit);
                if (hit instanceof Missile) {
                    ((Missile) hit).bounceOffOf(this);
                }
                maybeBreak();
            }
        }

        doHitEffect(x, y, z - 0.5f, Game.instance.level);
    }

    @Override
    public void encroached(float hitx, float hity) {
        // bounce!
    }

    @Override
    public void stepUp(float posOffset) {
        if (!this.floating) {
            z = posOffset;
        }
    }

    @Override
    protected void pickup(Player player) {
        this.resetState();

        for (int i = 0; i < Game.instance.player.inventory.size; i++) {
            Item check = Game.instance.player.inventory.get(i);
            if (check instanceof ItemStack && ((ItemStack) check).stackType.equals(this.stackType)) {
                ItemStack stack = (ItemStack) check;
                stack.count++;
                isActive = false;
                return;
            }
        }

        if (!Game.instance.player.addToInventory(new ItemStack(Game.instance.itemManager.Copy(Missile.class, this), 1, name))) {
            Game.ShowMessage(StringManager.get("items.Missile.noRoomText"), 1f);
        }
    }

    @Override
    public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator) {
        this.stuck = false;

        // Push the missile a little bit away from whatever it is stuck in
        Vector3 away = CachePools.getVector3();
        away.set(this.drawable.dir.x, this.drawable.dir.z, 0).nor();
        away.scl(-0.0125f);
        this.applyPhysicsImpulse(away);
        CachePools.freeVector3(away);

        super.hit(projx, projy, damage, knockback, damageType, instigator);
    }

    @Override
    public void hitWorld(float xSpeed, float ySpeed, float zSpeed) {
        this.stuck = false;

        Vector3 velocity = new Vector3(this.xa, this.ya, this.za);
        float mag = velocity.len();

        Vector3 forward = new Vector3(this.xa, this.ya, this.za).nor();
        Vector3 normal = new Vector3(intersectionNormal.x, intersectionNormal.z, intersectionNormal.y).scl(-1f);

        float incident = forward.dot(normal);
        float targetAngle = (float) Math.toRadians(15.0f);

        // Set hit position
        x = levelIntersection.x;
        y = levelIntersection.z;
        z = levelIntersection.y - yOffset;

        Vector3 newPosition = new Vector3(forward).scl(-0.125f);
        this.x += newPosition.x;
        this.y += newPosition.y;
        this.z += newPosition.z;

        // Stick into wall?
        if (Math.abs(incident) > targetAngle || mag < 0.1f) {
            this.maybeBreak();

            if (this.isActive) {
                this.stuck = true;
                this.xa = 0;
                this.ya = 0;
                this.za = 0;
            }
        }
        else {
            // bounce off of wall!
            // Reflection off of a plane is: −(2(n · v) n − v)
            Vector3 newAngle = new Vector3(normal);
            newAngle.sub(forward).scl((2 * normal.dot(forward)));

            // Add some friction as well, arrows are not perfectly elastic
            newAngle.scl(-mag * 0.6f);
            xa = newAngle.x;
            ya = newAngle.y;
            za = newAngle.z;
        }

        // Kick up sparks/dust when ricocheting off a wall
        slideEffectTimer = 0;
        super.hitWorld(xa, ya, za);
        this.showHitEffect = true;

        if (xa != 0 || ya != 0) {
            ignorePlayerCollision = false;
        }
    }

    public void doHitEffect(float xLoc, float yLoc, float zLoc, Level lvl) {
        // TODO: Get sound effect for this
        //Audio.playSound(wallHitSound, 0.25f, Game.rand.nextFloat() * 0.1f + 0.95f);

        Random r = Game.rand;
        for (int ii = 0; ii < r.nextInt(5) + 3; ii++) {
            Particle p = CachePools.getParticle(xLoc + 0.5f, yLoc + 0.5f, zLoc + 0.6f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.03f - 0.015f, 420 + r.nextInt(500), 1f, 0f, 0, this.color, this.damageType != DamageType.PHYSICAL);
            p.movementRotateAmount = 10f;
            lvl.SpawnNonCollidingEntity(p);
        }

        // start shaking
        shakeTimer = 1f;

        //makeHitDecal(xLoc + 0.5f, yLoc + 0.5f, zLoc + 0.18f, new Vector3(Game.camera.direction.x, Game.camera.direction.z, Game.camera.direction.y));
    }

    public void attach(Entity e) {
        if (this.attached == null) {
            this.attached = new Array<Entity>();
        }

        this.attached.add(e);
    }

    private void resetState() {
        this.isActive = false;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.xa = 0;
        this.ya = 0;
        this.za = 0;
        this.effectLifetime = 200f;
        this.showHitEffect = false;
        this.isOnFloor = false;
        this.wasOnFloorLast = false;
        this.trailTimer = 0.0f;
        this.slideEffectTimer = 0f;

        if (this.attached != null) {
            this.attached.clear();
        }
        this.attachmentTransform = null;

        this.resetTickCount();
    }
}
