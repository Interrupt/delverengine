package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.collision.Collision;
import com.interrupt.dungeoneer.collision.Collision.CollisionType;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.entities.projectiles.Missile;
import com.interrupt.dungeoneer.entities.triggers.Trigger;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.drawables.Drawable;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Random;

/** Base class for all entities in Delver Engine Levels. */
public class Entity {
	/** Id of Entity. */
	@EditorProperty( group = "General" )
	public String id;

	@EditorProperty( group = "Visual" )
	public Drawable drawable;

	/** Position x-component. */
	public float x;

	/** Position y-component. */
	public float y;

	/** Position z-component. */
	public float z;

	/** Velocity x-component. */
	public float xa;

	/** Velocity y-component. */
	public float ya;

	/** Velocity z-component. */
	public float za;

	/** Roll used when drawing the Entity. */
	public float roll;

	/** Sprite index. */
	@EditorProperty( group = "Visual", type = "SPRITE_ATLAS_NUM" )
	public int tex;

	/** Should the Entity be ticked and drawn? */
	public boolean isActive = true;

	/** Z component of the Entity position last tick. */
	protected float lastZ;

	/** Vertical offset used when drawing the Entity. */
	@EditorProperty( group = "Visual" )
	public float yOffset = 0;

	/** Shader name used to draw the Entity. */
	@EditorProperty( group = "Visual" )
	public String shader = null;

	@EditorProperty( group = "Visual" )
	public BlendMode blendMode = BlendMode.OPAQUE;

    public enum ArtType { entity, sprite, weapon, door, hidden, item, texture, particle };
	public enum EntityType { generic, item, monster };
	public enum CollidesWith { staticOnly, all, actorsOnly, nonActors };
	public enum EditorState { none, hovered, picked };
	public enum CollisionAxis { X, Y };

	/** Detail level for Entity. */
	public enum DetailLevel {
		/** Low detail level. Always create Entity. */
		LOW,

		/** Medium detail level. Create Entity if at least medium level of detail. */
		MEDIUM,

		/** High detail level. Create Entity if at least high level of detail. */
		HIGH,

		/** Ultra detail level. Only Create Entity if ultra level of detail. */
		ULTRA
	}

	/** Shadow for Entity. */
	public enum ShadowType {
		/** No shadow. */
		NONE,

		/** Round shadow. */
		BLOB,

		/** Rectangular shadow. */
		RECTANGLE
	}

	/** Draw distance for Entity */
	public enum DrawDistance {
		/** Near draw distance. */
		NEAR,

		/** Medium draw distance. */
		MEDIUM,

		/** Far draw distance. */
		FAR
	}
	public enum BlendMode { OPAQUE, ALPHA, ADD }
	public enum HaloMode { NONE, BOTH, STENCIL_ONLY, CORONA_ONLY }

	public ArtType artType;

	/** Sprite TextureAtlas name. */
	public String spriteAtlas = null;

	public EntityType type;

	/** Can other entities collide with this Entity? */
	@EditorProperty( group = "Physics" )
	public boolean isSolid = false;

	/** Dimensions of Entity bounding box. */
	@EditorProperty( group = "Physics" )
	public Vector3 collision = new Vector3(0.125f, 0.125f, 0.25f);

	@EditorProperty( group = "Physics" )
	public CollidesWith collidesWith = CollidesWith.all;

	/** Scale used when drawing the Entity. */
	@EditorProperty( group = "Visual" )
	public float scale = 1f;

	/** Should the Entity not be drawn? */
	@EditorProperty( group = "Visual" )
	public boolean hidden = false;

	/** Is this is a physics object? */
	@EditorProperty( group = "General" )
	public boolean isDynamic = true;

	/** Chance to be created. */
	@EditorProperty( group = "General" )
	public float spawnChance = 1f;

	/** Detail level at which to be created. */
	@EditorProperty( group = "General" )
	public DetailLevel detailLevel = DetailLevel.LOW;

	/** Distance at which to be drawn. */
	@EditorProperty( group = "General" )
	public DrawDistance drawDistance = DrawDistance.FAR;

	/** Is Entity out of draw distance? */
	public transient boolean outOfDrawDistance = false;

	/** Can Entity be stepped up on? */
	public boolean canStepUpOn = true;

	/** Is Entity floating? */
	@EditorProperty( group = "Physics" )
	public boolean floating = false;

	/** Mass of Entity. */
	@EditorProperty( group = "Physics" )
	public float mass = 1f;

	/** Sound played when Entity hits the floor. */
	public String dropSound = "drops/drop_soft.mp3";

	@EditorProperty( group = "Visual" )
	public ShadowType shadowType = ShadowType.NONE;

	/** Does Entity bounce? */
	public boolean bounces = true;

	/** How high can Entity step up? */
	public float stepHeight = 0.5f;
	public float calcStepHeight = stepHeight;

	@Deprecated
	public boolean pushable = false;

	/** Non-directional sprite. */
	public boolean isStatic = false;

	/** Is Entity on the floor? */
	public boolean isOnFloor = false;

	/** Is Entity on another Entity? */
	public boolean isOnEntity = false;

	/** Is Entity in water? */
	public transient boolean inwater = false;

	/** Ignore collision with the Player? */
	public boolean ignorePlayerCollision = false;

	protected boolean wasOnFloorLast = false;
	private float lastSplashTime = 0;

	private float tickcount = 0;

	/** Entity tint color. */
	public Color color = Color.WHITE;

	/** Draw Entity without shading? */
	@EditorProperty( group = "Physics" )
	public boolean fullbrite = false;

	public float lavaHurtTimer = 0;

	protected transient Collision hitLoc = new Collision();

	/** Should this Entity be saved to the level file? */
	public boolean persists = true;

	/** Turn off physics when this Entity is at rest. */
	public transient boolean physicsSleeping = false;
	public boolean canSleep = false;

	public transient EditorState editorState = EditorState.none;

	/** Position offset of attached Entities. */
	protected Vector3 attachmentTransform = null;

	/** Array of attached Entities. */
	protected Array<Entity> attached = null;

	/** The Entity this Entity is attached to. */
	public transient Entity owner = null;

	public float slideEffectTimer = 0;

	public transient Float drawUpdateTimer = null;

	public transient boolean skipTick = false;

	public Entity()
	{

	}

	public Entity(float x, float y, int tex, boolean isDynamic)
	{
		this.x = x;
		this.y = y;
		this.tex = tex;
		this.isDynamic = isDynamic;
		artType = ArtType.entity;

		drawable = new DrawableSprite(tex, artType);
	}

	public void checkAngles(Level level, float delta)
	{
		if(level.collidesWithAngles(x + xa * delta, y, collision, this)) xa = 0;
		if(level.collidesWithAngles(x, y + ya * delta, collision, this)) ya = 0;
	}

	private transient Vector3 t_drawUpdateVector = null;
	public void tick(Level level, float delta)
	{
		// wake up if moving again
		if(physicsSleeping && Math.abs(xa) > 0.0001f || Math.abs(ya) > 0.0001f || Math.abs(za) > 0.0001f) physicsSleeping = false;

		// sometimes entities don't need to do any physics
		if(!isDynamic || !isActive || outOfDrawDistance) return;
		if(physicsSleeping) {
			tickAttached(level, delta);
			return;
		}

		tickcount += delta;

		lastZ = z;

		// check collision
		float nextx = x + xa * delta;
		float nexty = y + ya * delta;
		float nextz = z + za * delta;

		// then check for solid entities
		boolean didHitX = false;

		// room to move in X?
		if (level.isFree(nextx, y, z, collision, calcStepHeight, false, hitLoc)) {
			Entity encroaching = null;
			if(isSolid) encroaching = level.getHighestEntityCollision(nextx, y, z, collision, this);

			if(encroaching == null || z > encroaching.z + encroaching.collision.z - stepHeight) {
				// are we touching an Entity?
				if(encroaching != null) {
					// maybe we can climb on it
					if(z > encroaching.z + encroaching.collision.z - stepHeight &&
							level.collidesWorldOrEntities(nextx, y, encroaching.z + encroaching.collision.z, collision, this)
							&& encroaching.canStepUpOn) {
						stepUp((encroaching.z + encroaching.collision.z) - z);
						z = encroaching.z + encroaching.collision.z;
						encroaching.steppedOn(this);
					}
					else {
						didHitX = true;

						encroaching.encroached(this);
						this.encroached(encroaching);
					}
				}
			}
			else {
				if(encroaching != null) {
					encroaching.encroached(this);
					encroached(encroaching);
				}
				didHitX = true;
			}
		}
		else {
			didHitX = true;

			if (this instanceof Missile) {
				this.sweepCollisionWorld(nextx, nexty, nextz, level, delta);
			}
		}

		if(!didHitX) {
			x += xa * delta;
		}
		else {
			if(bounces)
			{
				if(hitLoc != null && hitLoc.colType == CollisionType.angledWall) {
					// bounce off of angled walls using bounceFactor * (-2 * (velocity dot normal)*normal + velocity)
					Vector3 velocity = new Vector3(xa, ya, za);
					Vector3 tempNormal = new Vector3(hitLoc.colNormal);

					tempNormal = tempNormal.scl(-2f * velocity.dot(hitLoc.colNormal)).add(velocity).scl(0.25f);

					xa = tempNormal.x;
					ya = tempNormal.y;
				}
				else xa = -xa * 0.25f;

				if(Math.abs(xa) > 0.007f) {
					hitWorld(xa,0,0);
				}
			}
			else
				xa = 0;
		}

		boolean didHitY = false;
		// room to move in Y?
		if (level.isFree(x, nexty, z, collision, calcStepHeight, false, hitLoc)) {
			Entity encroaching = null;
			if(isSolid) encroaching = level.getHighestEntityCollision(x, nexty, z, collision, this);

			if(encroaching == null || z > encroaching.z + encroaching.collision.z - stepHeight) {
				// are we touching an Entity?
				if(encroaching != null) {
					// maybe we can climb on it
					if(z > encroaching.z + encroaching.collision.z - stepHeight &&
							level.collidesWorldOrEntities(x, nexty, encroaching.z + encroaching.collision.z, collision, this)
							&& encroaching.canStepUpOn) {
						stepUp((encroaching.z + encroaching.collision.z) - z);
						z = encroaching.z + encroaching.collision.z;
						encroaching.steppedOn(this);
					}
					else {
						didHitY = true;

						encroaching.encroached(this);
						this.encroached(encroaching);
					}
				}
			}
			else {
				if(encroaching != null) {
					encroaching.encroached(this);
					encroached(encroaching);
				}
				didHitY = true;
			}
		}
		else {
			didHitY = true;

			if (this instanceof Missile) {
				this.sweepCollisionWorld(nextx, nexty, nextz, level, delta);
			}
		}

		if(!didHitY) {
			y += ya * delta;
		}
		else {
			if(bounces)
			{
				if(hitLoc != null && hitLoc.colType == CollisionType.angledWall) {
					// bounce off of angled walls using bounceFactor * (-2 * (velocity dot normal)*normal + velocity)
					Vector3 velocity = new Vector3(xa, ya, za);
					Vector3 tempNormal = new Vector3(hitLoc.colNormal);

					tempNormal = tempNormal.scl(-2f * velocity.dot(hitLoc.colNormal)).add(velocity).scl(0.25f);

					xa = tempNormal.x;
					ya = tempNormal.y;
				}
				else ya = -ya * 0.25f;

				if(Math.abs(ya) > 0.007f) {
					hitWorld(0,ya,0);
				}
			}
			else
				ya = 0;
		}

		// ceiling collision?
		if (za > 0 && !level.isFree(x, y, z + za, collision, calcStepHeight, false, null)) {
			za = 0;
		}

		// Falling and stepping physics
		isOnEntity = false;
		Array<Entity> allStandingOn = level.getEntitiesColliding(x, y, (z + za * delta) - 0.02f, this);
		Entity standingOn = null;

		// check which Entity standing on is the highest
		for(Entity on : allStandingOn) {
			if(!on.isDynamic || (isSolid)) {
				if(standingOn == null || on.z + on.collision.z > standingOn.z + standingOn.collision.z) {
					standingOn = on;
					isOnEntity = true;
				}
			}
		}

		boolean wasOnFloor = isOnFloor;

		float floorHeight = level.maxFloorHeight(x, y, z, collision.x);
		isOnFloor = z <= (floorHeight + 0.5f) + 0.035f;

		// floor drop sound
		if(wasOnFloor == false && isOnFloor == true) {
			if(Math.abs(za) > 0.03f) {
				if (this instanceof Missile) {
					this.sweepCollisionWorld(nextx, nexty, nextz, level, delta);
				}

				hitWorld(0,0,za);
			}
		}

		// friction!
		if((isOnFloor || isOnEntity) && !floating)
		{
			// floor friction
			if(isOnFloor) {
				xa -= (xa - (xa * 0.8f)) * delta;
				ya -= (ya - (ya * 0.8f)) * delta;
			}
			else {
				// offset some for the player not clamping to the Entity as easily as the floor
				xa -= (xa - (xa * 0.7f)) * delta;
				ya -= (ya - (ya * 0.7f)) * delta;
			}

			Vector3 speedVector = CachePools.getVector3(xa, ya, 0);
			float entitySpeed = speedVector.len();
			CachePools.freeVector3(speedVector);

			if (entitySpeed > 0.025f) {
				onSlide(entitySpeed, delta);
			}

			// sleep to save collision cycles if not moving very fast
			if(canSleep && (isOnFloor || (standingOn == null || (!standingOn.isDynamic))) && Math.abs(xa) < 0.0001f && Math.abs(ya) < 0.0001f && Math.abs(za) < 0.0001f) physicsSleeping = true;
		}
		else
		{
			xa -= (xa - (xa * 0.98)) * delta;
			ya -= (ya - (ya * 0.98)) * delta;
		}

		if(!isOnFloor && !isOnEntity && !floating) za -= 0.0035f * delta; // falling; add gravity
		else
		{
			if(isOnFloor)
				za = 0;
			else if(isOnEntity && standingOn.za < 0)
				za = Math.max(za - (0.0035f * delta), standingOn.za);
			else
				za = 0;

			float stepUpToHeight = floorHeight + 0.5f;
			if(isOnEntity && standingOn.z + standingOn.collision.z - z < stepHeight && standingOn.z + standingOn.collision.z > stepUpToHeight) stepUpToHeight = standingOn.z + standingOn.collision.z;

			if(stepUpToHeight > z) {
				if (level.collidesWorldOrEntities(x, y, stepUpToHeight, collision, this)) {
					stepUp(stepUpToHeight - z);
					z = stepUpToHeight;
				}
			}
		}

		if(standingOn instanceof Actor || standingOn instanceof Trigger) {
			// bounce off of actors
			if(z > standingOn.z) za = 0.002f;
			xa += (Game.rand.nextFloat() * 0.01f - 0.005f) * delta;
			ya += (Game.rand.nextFloat() * 0.01f - 0.005f) * delta;
		}

		// water movement
		boolean wasInWater = inwater;
		inwater = false;

		Tile cTile = level.getTile((int)Math.floor(x), (int)Math.floor(y));
		if(cTile.data.isWater && z < cTile.floorHeight + 0.32f)
		{
			// water friction!
			xa -= (xa - (xa * 0.8)) * delta;
			ya -= (ya - (ya * 0.8)) * delta;

			calcStepHeight = stepHeight + (cTile.floorHeight + 0.5f - z);
			inwater = true;

			if(!wasInWater) {
				if(cTile.data.hurts > 0) {
					lavaHurtTimer = 0;
				}
				splash(level, cTile.floorHeight + 0.5f, true, cTile);
			}
		}
		else calcStepHeight = stepHeight;

		// check if this tile hurts
		if(cTile != null && cTile.data.hurts > 0 && z <= cTile.floorHeight + 0.5f) {
			lavaHurtTimer -= delta;
			if(lavaHurtTimer <= 0) {
				lavaHurtTimer = 30;
				this.hit(0, 0, cTile.data.hurts, 0, cTile.data.damageType, null);
			}
		}

		// check if this tile applies status effects
		if(cTile != null && cTile.data.applyStatusEffect != null && (this instanceof Actor) && z <= cTile.floorHeight + 0.505f && isOnFloor) {
			cTile.data.applyStatusEffect((Actor)this);
		}

		if(!isOnFloor && standingOn != null && !standingOn.isDynamic) isOnFloor = true;

		// dust effect timer
		if(slideEffectTimer > 0) {
			slideEffectTimer -= delta;
		}

		if(standingOn == null) {
			z += za * delta;
		}
		else {
			standingOn.encroached(this);
		}

		tickAttached(level, delta);
	}

	protected static Array<Entity> attachedToRemove = new Array<Entity>();
	public void tickAttached(Level level, float delta) {
		if(attached != null) {

			// let attachments preserve their offsets
			if(attachmentTransform == null) attachmentTransform = new Vector3(0,0,0);

			for(int i = 0; i < attached.size; i++) {
				Entity attachment = attached.get(i);
				attachment.x += x - attachmentTransform.x;
				attachment.y += y - attachmentTransform.y;
				attachment.z += z - attachmentTransform.z;
				attachment.owner = this;
				attachment.isSolid = false;	// attachments are always non solid
				attachment.tick(level, delta);
				attachment.tickAttached(level, delta);

				if(!attachment.isActive) attachedToRemove.add(attachment);
			}

			if(attachedToRemove.size > 0) {
				for(Entity e : attachedToRemove) {
					e.onDispose();
					attached.removeValue(e, true);
				}
				attachedToRemove.clear();
			}

			attachmentTransform.set(x,y,z);
		}
	}

	public void attach(Entity toAttach) {
		if(attached == null) {
			attached = new Array<Entity>();
		}
		else {
			if(attachmentTransform != null) {
				toAttach.x += attachmentTransform.x;
				toAttach.y += attachmentTransform.y;
				toAttach.z += attachmentTransform.z;
			}
		}
		attached.add(toAttach);
	}

	public boolean hasAttached(Class type) {
		if(attached == null || attached.size == 0) return false;
		for(Entity e : attached) {
			if(e.getClass() == type) return true;
		}
		return false;
	}

	public Entity getAttached(Class type) {
		if(attached != null && attached.size > 0) {
			for (Entity e : attached) {
				if (e.getClass() == type) return e;
			}
		}
		return null;
	}

	// Override this to do something useful when hitting the world
	public void hitWorld(float xSpeed, float ySpeed, float zSpeed) {
		if(zSpeed != 0) {
			Audio.playPositionedSound(dropSound, new Vector3(x,y,z), Math.abs(zSpeed) * 5f, 1f, 10f);
		}
		else if(xSpeed != 0) {
			Audio.playPositionedSound(dropSound, new Vector3(x,y,z), Math.abs(xSpeed) * 6f, 1f, 10f);
		}
		else if(ySpeed != 0) {
			Audio.playPositionedSound(dropSound, new Vector3(x,y,z), Math.abs(ySpeed) * 6f, 1f, 10f);
		}

		makeDustEffect();
	}

	// only called in the editor
	public void editorTick(Level level, float delta) {
		if (attached == null) {
			return;
		}

		if (attachmentTransform == null) {
			attachmentTransform = new Vector3();
		}

		for (Entity entity : attached) {
			if (entity == null) {
				continue;
			}

			entity.x += x - attachmentTransform.x;
			entity.y += y - attachmentTransform.y;
			entity.z += z - attachmentTransform.z;

			entity.editorTick(level, delta);
		}

		attachmentTransform.set(x, y ,z);
	}
	public void editorStartPreview(Level level) { }
	public void editorStopPreview(Level level) { }

	protected void splash(Level level, float splashZ, boolean first, Tile tile)
	{
		if(tickcount - lastSplashTime < 30 && !first) return;
		lastSplashTime = tickcount;

		Tile cTile = level.getTile((int)(x), (int)(y));
		if(z > cTile.floorHeight + 0.5 && !first) return;

		Random r = Game.rand;
		int particleCount = 10;
		particleCount *= Options.instance.gfxQuality;

		for(int i = 0; i < particleCount; i++)
		{
            Particle p = CachePools.getParticle(x, y, splashZ, r.nextFloat() * 0.02f - 0.01f, r.nextFloat() * 0.02f - 0.01f, r.nextFloat() * (-za * 0.25f) + 0.015f, tile.data.particleTex, tile.data.particleColor, tile.data.particleFullbrite);
            p.endScale = 0.1f;
			level.SpawnNonCollidingEntity(p);
		}

		int amt = 5;
		float rotAmount = (3.14159f * 2f) / amt;
		float rot = Game.rand.nextFloat();

		for(int i = 0; i < amt; i++) {
			// Make splash circle!
			Particle p = CachePools.getParticle(x, y, splashZ, 0, 0, 0, 18, tile.data.particleColor, tile.data.particleFullbrite);

			// Randomize location a tad
			p.x += (0.125f * Game.rand.nextFloat()) - 0.0625f;
			p.y += (0.125f * Game.rand.nextFloat()) - 0.0625f;

			p.checkCollision = false;
			p.floating = true;
			p.lifetime = (int) (24 * Game.rand.nextFloat()) + 40;
			p.playAnimation(49, 51, 20 + r.nextInt(10));

			p.xa = (0 * (float) Math.cos(rot) + 1 * (float) Math.sin(rot)) * 0.05f;
			p.ya = (1 * (float) Math.cos(rot) - 0 * (float) Math.sin(rot)) * 0.05f;

			p.xa += xa * 0.7f;
			p.ya += ya * 0.7f;

			p.xa *= 0.4f;
			p.ya *= 0.4f;

			p.x += p.xa * 0.1f;
			p.y += p.ya * 0.1f;

			p.maxVelocity = 0.1f;
			p.dampenAmount = 0.65f;
			//p.airFriction = 0.94f;

			level.SpawnNonCollidingEntity(p);

			rot += rotAmount;
		}

		float volume = Math.min(Math.abs(za) * 2.25f, 0.35f);
		Audio.playPositionedSound("drops/drop_water_01.mp3,drops/drop_water_02.mp3,drops/drop_water_03.mp3,drops/drop_water_04.mp3", new Vector3(x, y, z), volume * 2f, 1f, 12f);
	}

	public void use(Player p, float projx, float projy)
	{
		// overload this
	}

	public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator)
	{
		// overload this
	}

	// touching Entity
	public void encroached(Entity hit)
	{
		// overload this
	}

	// touching world
	public void encroached(float hitx, float hity)
	{
		// overload this
	}

	// touching player
	public void encroached(Player player)
	{
		// overload this
	}

	// player is pushing
	public void push(Player player, Level level, float delta, CollisionAxis collisionAxis)
	{
		// Overload this
	}

	public void steppedOn(Entity e) {
		// Overload this
	}

	public void resetTickCount()
	{
		tickcount = 0;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	// overload this to initialize on level start
	public void init(Level level, Source source) {
		updateDrawable();

		if(isSolid && !isDynamic) {
				Tile t = level.getTile((int)x, (int)y);
				if(t != null && t.getFloorHeight() + 0.6f > z && collision.z > 0.2f && (collision.x >= 0.4f && collision.y >= 0.4f)) {
					t.canNav = false;
				}
		}

		// grab a sprite atlas if one hasn't been set yet
		if(spriteAtlas == null && artType != null) spriteAtlas = artType.toString();

		// convert the hidden artType (deprecated) to the replacement boolean
		if(artType == ArtType.hidden && !hidden) hidden = true;

		calcStepHeight = stepHeight;

		if(attached != null) {
			for(int i = 0; i < attached.size; i++) {
				attached.get(i).init(level, source);
			}
		}
	}

	// overload this to do any cleanup work when unloaded
	public void onDispose() {
		if(attached != null) {
			for(Entity e : attached) {
				e.onDispose();
			}
		}
	}

	public void updateDrawable() {
		if(drawable != null)
			drawable.update(this);
		else if(artType != ArtType.hidden) {
			if(spriteAtlas == null && artType != null) spriteAtlas = artType.toString();
			drawable = new DrawableSprite(tex, artType);
			drawable.update(this);
		}

		updateAttachedDrawables();
	}

	public Array<Entity> getAttached() {
		return attached;
	}

	public void updateAttachedDrawables() {
		if(attached != null) {
			for(int i = 0; i < attached.size; i++) {
				attached.get(i).updateDrawable();
			}
		}
	}

	public boolean checkDetailLevel() {
		int gameDetailLevel = Options.instance.graphicsDetailLevel;
		if(detailLevel == DetailLevel.ULTRA && gameDetailLevel < 4) return false;
		else if(detailLevel == DetailLevel.HIGH && gameDetailLevel < 3) return false;
		else if(detailLevel == DetailLevel.MEDIUM && gameDetailLevel < 2) return false;
		else return true;
	}

	public void onTrigger(Entity instigator, String value) {
		// overload this to take actions when triggered
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	public void rotate90() {
		float temp = collision.x;
		collision.x = collision.y;
		collision.y = temp;
	}

	public void rotate90Reversed() {
		float temp = collision.x;
		collision.x = collision.y;
		collision.y = temp;
	}

	public void setRotation(float rotX, float rotY, float rotZ) {
		// override this to do stuff
	}

    public void rotate(float rotX, float rotY, float rotZ) {
        // override this to do stuff
    }

    public Vector3 getRotation() {
		// override this to do stuff
		return Vector3.Zero;
	}

	public void setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setPosition(Entity other) {
	    if (other == null) {
	        return;
        }

	    x = other.x;
	    y = other.y;
	    z = other.z;
    }

	public void matchEntity(Entity toMatch) {
		matchPositionAndVelocity(toMatch);
		artType = toMatch.artType;
		tex = toMatch.tex;
		yOffset = toMatch.yOffset;
		roll = toMatch.roll;
		collision.set(toMatch.collision);
		stepHeight = toMatch.stepHeight;
		mass = toMatch.mass;
		isSolid = toMatch.isSolid;
		collidesWith = toMatch.collidesWith;
		color = new Color(toMatch.color);
	}

	public void matchPositionAndVelocity(Entity toMatch) {
		x = toMatch.x;
		y = toMatch.y;
		z = toMatch.z;
		xa = toMatch.xa;
		ya = toMatch.ya;
		za = toMatch.za;
		yOffset = toMatch.yOffset;
	}

	public void applyPhysicsImpulse(Vector3 impulse) {
		xa += impulse.x / mass;
		ya += impulse.y / mass;
		za += impulse.z / mass;
		this.physicsSleeping = false;
	}

	public boolean canCollide(Entity checking) {
		if(collidesWith == CollidesWith.all) return true;
		else if(collidesWith == CollidesWith.staticOnly && !checking.isDynamic) return true;
		else if(collidesWith == CollidesWith.actorsOnly && checking instanceof Actor) return true;
		else if(collidesWith == CollidesWith.nonActors && !(checking instanceof Actor)) return true;
		return false;
	}

	public void updateLight(Level level) {
		// override this to do something
	}

	public boolean isHidden() {
		return hidden || artType == ArtType.hidden;
	}

	public void stepUp(float posOffset) {
		// override this to make smoother steps
	}

	public void sweepCollisionWorld(float nextX, float nextY, float nextZ, Level level, float delta) {
		// Calculate a previous good non-colliding position in case of point blank shots
		float lastFreeX = x - xa * delta;
		float lastFreeY = y - ya * delta;
		float lastFreeZ = z - za * delta;
		float incr = 0f;

		// Sweep towards next position from current position
		for (int i = 1; i < 4; i++) {
			incr = 1.0f - (1.0f / (float) i);

			x = (1.0f - incr) * x + incr * nextX;
			y = (1.0f - incr) * y + incr * nextY;
			z = (1.0f - incr) * z + incr * nextZ;

			if (!level.isFree(x, y, z, collision, calcStepHeight, false, hitLoc)) {
				break;
			}

			// Update good non-colliding position
			lastFreeX = x;
			lastFreeY = y;
			lastFreeZ = z;
		}

		// Sweep out of world back towards last known good non-colliding position
		for (int i = 1; i < 8; i++) {
			incr = 1.0f - (1.0f / (float) i);

			x = (1.0f - incr) * x + incr * lastFreeX;
			y = (1.0f - incr) * y + incr * lastFreeY;
			z = (1.0f - incr) * z + incr * lastFreeZ;

			if (level.isFree(x, y, z, collision, calcStepHeight, false, hitLoc)) {
				break;
			}
		}
	}

	// Generated level chunks need to make Entity IDs unique per-chunk, in case of duplicates
	public void makeEntityIdUnique(String idPrefix) {
		id = makeUniqueIdentifier(id, idPrefix);
	}

	public String makeUniqueIdentifier(String identifier, String idPrefix) {
		if(identifier == null || identifier.equals("")) return null;
		return idPrefix + "_" + identifier;
	}

	public void onSlide(float slideSpeed, float delta) {
		if(slideEffectTimer <= 0) {
			makeDustEffect();
		}
	}

	public void makeDustEffect() {
		if(slideEffectTimer <= 0) {
			slideEffectTimer = 2;

			Particle p = CachePools.getParticle(x - xa, y - ya, z - za - 0.45f, 0, 0, 0, Game.rand.nextInt(3), Color.WHITE, false);

			// Randomize location a tad
			p.x += (0.125f * Game.rand.nextFloat()) - 0.0625f;
			p.y += (0.125f * Game.rand.nextFloat()) - 0.0625f;

			p.checkCollision = false;
			p.floating = true;
			p.lifetime = (int) (15 * Game.rand.nextFloat()) + 40;
			p.shader = "dust";
			p.spriteAtlas = "dust_puffs";
			p.startScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
			p.endScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
			p.endColor = new Color(1f, 1f, 1f, 0f);
			p.scale = 0.5f;

			p.xa = (0.00125f * Game.rand.nextFloat());
			p.ya = (0.00125f * Game.rand.nextFloat());
			p.za = (0.00125f * Game.rand.nextFloat()) + 0.0025f;

			Game.GetLevel().SpawnNonCollidingEntity(p);
		}
	}

	public void resetDrawable() {
		drawable = null;
		if(attached != null) {
			for (Entity e : attached) {
				e.resetDrawable();
			}
		}
		updateDrawable();
	}

	public String getShader() {
		return shader;
	}

	// for stencil shadows and halos
	public HaloMode getHaloMode() { return HaloMode.NONE; }
	public boolean hasShadow() { return shadowType != ShadowType.NONE; }
}
