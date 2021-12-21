package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.items.Sword;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;

import java.util.Random;

public class Breakable extends Model {
	public Breakable() { isDynamic = true; shadowType = ShadowType.BLOB; }

    /** Current health. */
	@EditorProperty
	public int hp = 2;

	/** Starting possible gib range sprite index. */
	@EditorProperty
	public int gibSpriteTexStart = 40;

	/** Ending possible gib range sprite index. */
	@EditorProperty
	public int gibSpriteTexEnd = 42;

	/** Number of gib particles to create. */
	@EditorProperty
	public int gibNum = 8;

	/** Gib particle lifetime. */
	@EditorProperty
	public float gibLifetime = 2000f;

	/** Sound played when broken. */
	@EditorProperty
	public String breakSound = "break/break_wood_01.mp3,break/break_wood_02.mp3,break/break_wood_03.mp3";

	/** Chance that loot is dropped. */
	@EditorProperty(group = "Loot")
	public float lootSpawnChance = 0.225f;

	/** Can the loot dropped be gold? */
	@EditorProperty(group = "Loot")
	public boolean lootCanBeGold = true;

	/** Chance that a surprise is spawned. */
	@EditorProperty(group = "Loot")
	public float surpriseSpawnChance = 0.1f;

	/** Draw breakable as a sprite? */
    @EditorProperty
    public boolean drawAsSprite = false;

    /** Entity to send trigger event when triggered. */
	@EditorProperty(group = "Triggers")
	public String triggerWhenBreaks = null;

	/** Does this break when stepped on? */
	@EditorProperty
	public boolean breaksWhenSteppedOn = false;

	/** Gib particle initial velocity range. */
	public Vector3 gibVelocity = new Vector3(0.03f,0.03f,0.04f);

	/** Magnitude of shake effect. */
	@EditorProperty
	public float shakeAmount = 4f;

	/** Can this be pushed? */
	@EditorProperty
	public boolean canBePushed = false;

	/** Can this break? */
	@EditorProperty
	public boolean canBreak = true;

	public transient float shakeTimer = 0f;

	/** Time to delay break in milliseconds. */
	@EditorProperty
	public float breakDelay = 0f;

	private float breakDelayTimer = 0f;

	private transient Array<Entity> entityStandingOnUsCache = new Array<>();

	/** List of random Entities to spawn when broken. */
	public Array<Entity> spawns = new Array<Entity>();

	public Breakable(String meshFile, String textureFile) {
		this.meshFile = meshFile;
		this.textureFile = textureFile;
		isSolid = true;
		stepHeight = 0f;
	}

	// player is pushing
	@Override
	public void push(Player player, Level level, float delta, CollisionAxis collisionAxis)
	{
		if(!canBePushed)
			return;

		float massModPrimary = 1.0f - (mass * 0.1f);
		float massModSecondary = 1.0f - (mass * 0.05f);

		if(collisionAxis == CollisionAxis.X) {
			player.xa *= massModPrimary;
			player.ya *= massModSecondary;
			xa = player.xa;
			ya = player.ya;
		}
		else if(collisionAxis == CollisionAxis.Y) {
			player.ya *= massModPrimary;
			player.xa *= massModSecondary;
			ya = player.ya;
			xa = player.xa;
		}

		// Move this breakable now to allow the player room to move
		tick(level, delta);

		// Skip the next tick, since we already moved
		skipTick = true;
	}

	private transient Vector3 lastHitDir = new Vector3();
	@Override
	public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator) {
		if (Game.instance == null) {
			return;
		}

		super.hit(projx, projy, damage, knockback, damageType, instigator);

		if(canBePushed)
			this.applyPhysicsImpulse(new Vector3(projx,projy,0).scl(knockback));

		if(!canBreak)
			return;

			hp -= damage;

			shakeTimer = 50f;

			// Only set lastHitDir when taking lethal damage
			if (damage + hp > 0 && hp <= 0) {
				lastHitDir.set(projx, projy, 0);
			}

			if(damage <= 0)
				return;

			// start on fire sometimes when taking fire damage
			if(damageType == DamageType.FIRE && Game.rand.nextBoolean()) {
			Fire f = new Fire();
			f.lifeTime = 300;
			f.playAnimation();
			f.hurtTimer = f.hurtTime * 0.75f;
			f.spreadTimer = f.spreadTime * 0.75f;
			f.z = collision.z / 1.25f;
			attach(f);
		}
	}

	private void onBreak(float projx, float projy) {
		if(!isActive) return;
		isActive = false;

		Level level = Game.instance.level;
			gib(level, new Vector3(projx * 0.04f, projy * 0.04f, 0));

			if (this.spawns != null && this.spawns.size > 0) {
				int idx = Game.rand.nextInt(this.spawns.size);
				Entity e = EntityManager.instance.Copy(this.spawns.get(idx));
				e.x = x;
				e.y = y;
				e.z = z;

				if (e.isSolid && level.checkEntityCollision(e.x, e.y, e.z, e.collision, e) == null)
					level.SpawnEntity(e);
				else
					level.SpawnEntity(e);
			}
			else {
				// Spawn loot or surprises sometimes
				if (Game.rand.nextFloat() <= surpriseSpawnChance) {
					Entity ent = Game.GetEntityManager().GetRandomSurprise();
					if (ent != null) {
						ent.x = x;
						ent.y = y;
						ent.z = z + 0.15f;
						ent.za = 0.02f;

						// Only spawn when there is room
						Tile t = level.getTile((int) x, (int) y);
						if (!t.blockMotion) {
							float floorHeightHere = t.getFloorHeight(ent.x, ent.y);

							if (ent.z < floorHeightHere)
								ent.z = floorHeightHere;

							if(level.checkEntityCollision(ent.x, ent.y, ent.z, ent.collision, ent) == null)
								level.SpawnEntity(ent);
						}
					}
				} else if (Game.rand.nextFloat() <= lootSpawnChance) {
					Item itm = Game.GetItemManager().GetMonsterLoot(Game.instance.player.level, lootCanBeGold);
					if (itm != null) {
						itm.x = x;
						itm.y = y;
						itm.z = z + 0.15f;
						itm.za = 0.02f;

						if(level.checkEntityCollision(itm.x, itm.y, itm.z, itm.collision, itm) == null)
							level.SpawnEntity(itm);
					}
				}
			}

			if(triggerWhenBreaks != null) {
				level.trigger(this, triggerWhenBreaks, "");
			}
	}

	public void tick(Level level, float delta) {

		// Might need to keep track of how much we moved
		float xBefore = x;
		float yBefore = y;

		if(canBePushed)
			stepHeight = 0.1f;

		super.tick(level, delta);

		if(shakeTimer > 0 && Game.instance != null) {
			shakeTimer -= delta;
			roll = (float)Math.sin(Game.instance.time * 0.5f) * shakeTimer * (shakeAmount * 0.03f);
			rotation.z += roll * 0.1f;

			if(shakeTimer <= 0) shakeTimer = 0;
		}

        if(hp <= 0) {
            if (breakDelayTimer >= breakDelay) {
                this.onBreak(lastHitDir.x, lastHitDir.y);
            }
            breakDelayTimer += delta;
        }

		// Make sure non pushable breakables go to sleep right away
		physicsSleeping = isOnFloor && !canBePushed;

		if(!canBePushed)
			return; // Can't be pushed, so can stop here

		// move any entities standing on us
		entityStandingOnUsCache.addAll(level.getEntitiesColliding(x, y, z + 0.06f, this));

		for(int i = 0; i < entityStandingOnUsCache.size; i++) {
			Entity e = entityStandingOnUsCache.get(i);

			if(e.isDynamic) {
				e.physicsSleeping = false;

				// Update the velocity, accounting for some friction. (0.3 seems to be the magic value)
				e.xa += (x - xBefore) * 0.3f;
				e.ya += (y - yBefore) * 0.3f;

				// Tick this entity now, so it can tick any entities on top of itself
				if(!e.skipTick) {
					e.skipTick = true;
					e.tick(level, delta);
				}
			}
		}

		entityStandingOnUsCache.clear();
	}

	public void gib(Level level, Vector3 gibVel) {
		if (Game.instance == null) {
			return;
		}

		for(int i = 0; i < gibNum; i++) {
			int range = gibSpriteTexEnd - gibSpriteTexStart;
			int ptex = gibSpriteTexStart;

			Particle p = CachePools.getParticle(x, y, z + (collision.z * 0.7f) - collision.z * (Game.rand.nextFloat() * 0.65f), (Game.rand.nextFloat() * gibVelocity.x) - gibVelocity.x * 0.5f + gibVel.x, (Game.rand.nextFloat() * gibVelocity.y) - gibVelocity.y * 0.5f + gibVel.y, (Game.rand.nextFloat() * gibVelocity.z) - gibVelocity.z * 0.5f, gibLifetime + Game.rand.nextFloat() * gibLifetime, ptex, Color.WHITE, false);
			p.movementRotateAmount = (Game.rand.nextFloat() - 0.5f) * 5f;

			// Make most of the gibs the small variant.
			if(i < gibNum * 0.75f) {
				p.tex = gibSpriteTexStart + range;
				p.mass = 0.1f;
				p.lifetime = gibLifetime * 0.65f;
			}
			// Otherwise choose at random from the remaining gibs.
			else {
				p.tex = gibSpriteTexStart + Game.rand.nextInt(range);
			}

			level.non_collidable_entities.add(p);
		}

		Audio.playPositionedSound(breakSound, new Vector3(x,y,z), 0.4f, 12);
	}

	@Override
	public void applyPhysicsImpulse(Vector3 impulse) {
		xa += impulse.x / mass;
		ya += impulse.y / mass;
		za += impulse.z / mass;
		this.physicsSleeping = false;
	}

	// never draw as a static mesh
	@Override
	public void updateDrawable() {
        if(!drawAsSprite) {

            if(drawable != null && !(drawable instanceof DrawableMesh)) {
                drawable = null;
                lastTextureFile = null;
            }

            super.updateDrawable();

            if(drawable != null && drawable instanceof DrawableMesh) {
                DrawableMesh drbl = (DrawableMesh) drawable;
                drbl.isStaticMesh = false;
            }
        }
        else {
            if(drawable != null && !(drawable instanceof DrawableSprite)) drawable = null;

            if(artType != ArtType.hidden && drawable == null) {
                if(spriteAtlas == null && artType != null) spriteAtlas = artType.toString();
                drawable = new DrawableSprite(tex, artType);
                drawable.update(this);
            }

            drawable.update(this);

            updateAttachedDrawables();
        }
	}

	@Override
	public void init(Level level, Source source) {
		stepHeight = 0;

		super.init(level, source);

		if(drawable != null && source != Source.EDITOR) {
            if(!drawAsSprite) {
                DrawableMesh drawableMesh = (DrawableMesh) drawable;
                drawableMesh.isStaticMesh = false;
            }
		}

        if(drawAsSprite) {
            drawable = new DrawableSprite(tex, artType);
        }
	}

	// effect to show when hit by a melee weapon
	public void doHitEffect(float xLoc, float yLoc, float zLoc, Sword sword, Level lvl) {
		if(hp > 0) {
			Audio.playPositionedSound(breakSound, new Vector3(x,y,z), 0.1f, Game.rand.nextFloat() * 0.1f + 0.95f, 12);
		}

		Color hitColor = sword.getEnchantmentColor();
		boolean fullBright = sword.getDamageType() != DamageType.PHYSICAL;

		if(fullBright) {
			// make a light at this location
			DynamicLight l = new DynamicLight(xLoc, yLoc, zLoc, new Vector3(hitColor.r * 0.85f, hitColor.g * 0.85f, hitColor.b * 0.85f));
			l.startLerp(new Vector3(0,0,0), 20, true);
			lvl.non_collidable_entities.add(l);
		}

		Random r = Game.rand;
		for(int ii = 0; ii < r.nextInt(5) + 3; ii++)
		{
			lvl.SpawnNonCollidingEntity(CachePools.getParticle(xLoc, yLoc, zLoc + 0.6f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.03f - 0.015f, 420 + r.nextInt(500), 1f, 0f, 0, hitColor, fullBright)) ;
		}

		Particle part = CachePools.getParticle(xLoc, yLoc, zLoc + 0.2f, "dust_puffs", 5);
		part.floating = true;
		part.checkCollision = false;
		part.shader = "spark";
		part.spriteAtlas = "dust_puffs";
		part.lifetime = 15;
		part.scale = 0.7f;
		part.color.set(Color.WHITE);
		part.color.a = 0.32f;
		part.endColor = new Color(part.color);
		part.endColor.a = 1;
		part.fullbrite = true;
		lvl.SpawnNonCollidingEntity(part);

		Game.instance.player.shake(0.8f);
	}

    @Override
    public void steppedOn(Entity e) {
        if (this.breaksWhenSteppedOn) {
            hit(0, 0, hp, 0, DamageType.PHYSICAL, e);
        }
    }
}
