package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.collision.Collision;
import com.interrupt.dungeoneer.entities.items.*;
import com.interrupt.dungeoneer.entities.items.Potion.PotionType;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.entities.projectiles.BeamProjectile;
import com.interrupt.dungeoneer.entities.triggers.ButtonModel;
import com.interrupt.dungeoneer.entities.triggers.Trigger;
import com.interrupt.dungeoneer.entities.triggers.Trigger.TriggerType;
import com.interrupt.dungeoneer.game.*;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.animation.lerp3d.LerpFrame;
import com.interrupt.dungeoneer.gfx.animation.lerp3d.LerpedAnimation;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.Actions.Action;
import com.interrupt.dungeoneer.input.ControllerState;
import com.interrupt.dungeoneer.input.ReadableKeys;
import com.interrupt.dungeoneer.overlays.*;
import com.interrupt.dungeoneer.rpg.Stats;
import com.interrupt.dungeoneer.screens.GameScreen;
import com.interrupt.dungeoneer.statuseffects.*;
import com.interrupt.dungeoneer.tiles.ExitTile;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.PlayerHistory;
import com.interrupt.managers.HUDManager;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Random;

public class Player extends Actor {
	/** Player gold amount. */
	public int gold = 0;

	/** Player z-axis rotation. */
	public float rot = 0;

	/** Player y-axis rotation. */
	public float yrot = 0;

	public float rota = 0;
	public float rotya = 0;

	public float rot2 = 0;

	/** Player jump height. */
	public float jumpHeight = 0.05f;

	/** Player eye height. */
	public float eyeHeight = 0.12f;

	/** Head bob speed. */
	public float headBobSpeed = 0.319f;

	/** Head bob height. */
	public float headBobHeight = 0.3f;

	/** Hand lag strength. */
	public float handLagStrength = 0.6f;

    /** Offhand lag strength. */
	public float offhandLagStrength = 0.8f;

	/** Hand offset */
	public Vector3 handOffset = new Vector3(-0.12f, -0.07f, 0.28f);

	/** Offhand offset */
	public Vector3 offhandOffset = new Vector3(0.12f, -0.14f, 0.24f);

	public boolean hasAttacked;

	private float attackSpeed = 1;
	private float attackChargeSpeed = 1;

	public float handAnimateTimer = 0;
	private float tickcount = 0;

	private float playtime = -1;

	public boolean ignoreStairs = false;
	public float spawnX, spawnY;

	/** Player key count. */
	public int keys = 0;

	public float attackChargeTime = 40;
	public float attackCharge = 0;

	@Deprecated
	public float attackDelay = 0.0f;

	public float headbob = 0;
	public transient Float stepUpLerp = null;
	public transient Float stepUpTimer = null;

	public boolean doingHeldItemTransition = false;
	public float heldItemTransitionEnd = 0;
	public float heldItemTransition = 0;

	private boolean wasOnFloorLast = false;
	private float lastSplashTime = 0;

	private float stepHeight = 0.35f;
	private float lastZ;

	public boolean isHoldingOrb = false;

	public int levelNum = 0;

	/** Player inventory. */
	public Array<Item> inventory = new Array<Item>();
	public Integer selectedBarItem = null;
	public Integer heldItem = null;

	/** New game player inventory. */
	public Array<Entity> startingInventory = new Array<Entity>();

	public HashMap<String,Item> equippedItems = new HashMap<String,Item>();

	// audio stuff
	private Long torchSoundInstance = null;
	private Long stepsSoundInstance = null;

	private Integer tapLength = null;

	public int hotbarSize = 5;
	public int inventorySize = 23;

	public Item hovering = null;

	private boolean attackButtonWasPressed = false;

	float walkVel = 0.05f;
	float walkSpeed = 0.15f;
	float minWalkSpeed = 0.01f;
	float rotSpeed = 0.009f;
	float maxRot = 0.06f;

	private transient float xm = 0;
	private transient float zm = 0;
	private transient float deltaX = 0;
	private transient float deltaY = 0;

	public transient float friction = 1f;

	public float randomSeed = 1;

	private transient Vector2 walkVelVector = new Vector2();
	private transient Vector2 lastDelta = new Vector2();
	private transient Collision hitLoc = new Collision();
	private transient float nextx;
	private transient float nexty;

	private transient boolean touchingItem = false;

	Vector3 tempVec1 = new Vector3();
	Vector3 tempVec2 = new Vector3();
	Vector3 tempVec3 = new Vector3();
	Vector3 tempVec4 = new Vector3();

	/** Player light color. */
	public Color torchColor = new Color(1f, 0.8f, 0.4f, 1f);

	/** Player light range. */
	public float torchRange = 3.0f;

	private Color originalTorchColor = null;

	public boolean inEditor = false;

	public static transient ControllerState controllerState = new ControllerState();

	private transient Array<Entity> pickList = new Array<Entity>();

	private HashMap<String, Float> messageViews = new HashMap<String, Float>();

	public transient LerpedAnimation handAnimation = null;

	public PlayerHistory history = new PlayerHistory();

	public transient boolean wasGamepadDragging = false;

	public float screenshakeAmount = 0;
	public transient Vector2 screenshake = new Vector2();

	public Array<Potion> shuffledPotions = new Array<Potion>();
	public Array<PotionType> discoveredPotions = new Array<PotionType>();

	private transient float footstepsTimer = 30;

	private transient boolean holdingTwoHanded = false;

	public HashMap<String,String> seenMessages = new HashMap<String, String>();

	public Stats calculatedStats = new Stats();

    public transient boolean isOnLadder = false;

    public LerpedAnimation dyingAnimation = null;
    public transient boolean isDead = false;

    public transient float strafeCameraAngleMod = 0f;

    /** Does player level up? */
    private boolean canLevelUp = true;

    private Array<TravelInfo> travelPath = new Array<TravelInfo>();
    public String levelName = "UNKNOWN";

    protected float tossPower = 0f;

	private transient Color t_vislightColor = new Color();
    public transient float visiblityMod = 0f;

    public boolean makeEscapeEffects = true;

    public boolean godMode = false;

    // Used to act on breaking changes between save versions
    public int saveVersion = -1;

	public Player() {
		isSolid = true;
		collision.set(0.2f,0.2f,0.65f);
		dropSound = "drops/drop_soft.mp3";
		hidden = true;
		mass = 2f;
		canStepUpOn = false;
	}

	public Player(Game game) {
		z = 0;
		rot = (float)Math.PI * -0.5f;

		maxHp = 8;
		hp = maxHp;

		collision.set(0.2f,0.2f,0.65f);

		isSolid = true;

		dropSound = "drops/drop_soft.mp3";

		mass = 2f;

		game.player = this;

		canStepUpOn = false;
	}

	public boolean canAddInventorySlot() {
		return inventorySize - hotbarSize < 36;
	}

	public void addInventorySlot() {
		if(canAddInventorySlot()) {
			inventorySize++;
			inventory.add(null);
			Game.hudManager.backpack.refresh();
			Game.hudManager.quickSlots.refresh();

			if(inventorySize - hotbarSize >= 35) {
				SteamApi.api.achieve("SQUID3");
			}
		}
	}

	public boolean canAddHotbarSlot() {
		return hotbarSize < 10;
	}

	public void addHotbarSlot() {
		if(canAddHotbarSlot()) {
			hotbarSize++;
			inventorySize++;
			inventory.add(null);
			Game.hudManager.backpack.refresh();
			Game.hudManager.quickSlots.refresh();

			if(hotbarSize >= 9) {
				SteamApi.api.achieve("SQUID4");
			}
		}
	}

	public void makeStartingInventory() {

		// initialize the inventory
		if(inventory.size < inventorySize) {
			for (int i = 0; i < inventorySize; i++) inventory.add(null);
		}

		if(startingInventory != null && startingInventory.size > 0) {
			boolean equippedWeapon = false;
			for(Entity e : startingInventory) {

				Entity toAdd = null;

				// Get the item, or spawn one!
				if(e instanceof Item) {
					toAdd = e;
				}
				else if(e instanceof ItemSpawner) {
					toAdd = ((ItemSpawner) e).getItem();
				}

				if(toAdd != null) {
					if(toAdd instanceof Weapon) {
						addToInventory((Item)toAdd, false);
						if(!equippedWeapon) equip((Item)toAdd, false);
						equippedWeapon = true;
					}
					else if(toAdd instanceof Armor) {
						equipArmor((Item)toAdd);
					}
					else if(toAdd instanceof Item) {
						addToInventory((Item)toAdd);
					}
				}
			}
			startingInventory.clear();
		}
	}

	public void init() {
		makeStartingInventory();
		setupController();
	}

	@Override
	public void checkAngles(Level level, float delta)
	{
		if(level.collidesWithAngles(x + xa * delta, y, collision, this)) xa = 0;
		if(level.collidesWithAngles(x, y + ya * delta, collision, this)) ya = 0;
	}

	@Override
	public void tick(Level level, float delta) {
        setMusicVolume();
		stepUpTick(delta);
		calculatedStats.Recalculate(this);

		// refresh the UI if it's being shown
		if(calculatedStats.statsChanged()) {
			Game.instance.refreshMenu();
		}

		// adjust the rendering field of view based on status effects
		if(GameManager.renderer != null) {
			GameManager.renderer.setFieldOfViewMod(getFieldOfViewModifier());
		}

		if(hp > getMaxHp()) hp = getMaxHp();

		nextx = x + xa * delta;
		nexty = y + ya * delta;

		// check for an item collision first
		if(!inEditor) {
			Entity item = level.checkItemCollision(nextx, y, collision.x);
			if(item == null) item = level.checkItemCollision(x, nexty, collision.x);
			if(item != null) item.encroached(this);
		}

		Vector3 floorSlope = level.getSlope(x, y, z, collision.x);

		float slopeXMod = (floorSlope.x * Math.abs(floorSlope.x)) * 0.01f;
		float slopeYMod = (floorSlope.y * Math.abs(floorSlope.y)) * 0.01f;

		if(Math.abs(slopeXMod) < 0.003f) slopeXMod = 0;
		if(Math.abs(slopeYMod) < 0.003f) slopeYMod = 0;

		if(isOnFloor) {
            xa += slopeXMod * delta;
            ya += slopeYMod * delta;
        }

		// room to move in X?
		if (!isSolid || level.isFree(nextx, y, z, collision, stepHeight, false, hitLoc)) {
			runPushCheck(level, delta, CollisionAxis.X);

			Entity encroaching = null;
			if(isSolid) {
				encroaching = level.getHighestEntityCollision(nextx, y, z, collision, this);
				if(encroaching == null) {
					encroaching = level.checkStandingRoomWithEntities(nextx, y, z, collision, this);
				}
			}

			if(encroaching == null || z > encroaching.z + encroaching.collision.z - stepHeight) {
				// are we touching an entity?
				if(encroaching != null) {
					// maybe we can climb on it
					if( z > encroaching.z + encroaching.collision.z - stepHeight &&
							level.collidesWorldOrEntities(nextx, y, encroaching.z + encroaching.collision.z, collision, this) && encroaching.canStepUpOn) {
						x += xa * delta;
						stepUp((encroaching.z + encroaching.collision.z) - z);
						z = encroaching.z + encroaching.collision.z;
						encroaching.steppedOn(this);
					}
					else {
						if(!inEditor) {
							encroaching.encroached(this);
						}
						xa = 0;
					}
				}
				else {
					x += xa * delta;
				}
			}
			else {
				if(!inEditor) {
					encroaching.encroached(this);
				}
				xa = 0;
			}
		}
		else {
			xa = 0;
		}

		// room to move in Y?
		if (!isSolid || level.isFree(x, nexty, z, collision, stepHeight, false, hitLoc)) {
			runPushCheck(level, delta, CollisionAxis.Y);

			Entity encroaching = null;
			if(isSolid) {
				encroaching = level.getHighestEntityCollision(x, nexty, z, collision, this);
				if(encroaching == null) {
					encroaching = level.checkStandingRoomWithEntities(x, nexty, z, collision, this);
				}
			}

			if(encroaching == null || z > encroaching.z + encroaching.collision.z - stepHeight) {
				// are we touching an entity?
				if(encroaching != null) {
					// maybe we can climb on it
					if( z > encroaching.z + encroaching.collision.z - stepHeight &&
							level.collidesWorldOrEntities(x, nexty, encroaching.z + encroaching.collision.z, collision, this) && encroaching.canStepUpOn) {
						y += ya * delta;
						stepUp((encroaching.z + encroaching.collision.z) - z);
						z = encroaching.z + encroaching.collision.z;
						encroaching.steppedOn(this);
					}
					else {
						if(!inEditor) {
							encroaching.encroached(this);
						}
						ya = 0;
					}
				}
				else {
					y += ya * delta;
				}
			}
			else {
				if(!inEditor) {
					encroaching.encroached(this);
				}
				ya = 0;
			}
		}
		else {
			ya = 0;
		}

		// Falling and stepping physics
		isOnEntity = false;
		Array<Entity> allStandingOn = level.getEntitiesColliding(x, y, (z + za * delta) - 0.02f, this);
		Entity standingOn = null;

		// check which entity standing on is the highest
		for(Entity on : allStandingOn) {
			if(isSolid) {
				if(standingOn == null || on.z + on.collision.z > standingOn.z + standingOn.collision.z) {
					standingOn = on;
				}

				if(standingOn != null) {
					standingOn.encroached(this);
					if(standingOn instanceof Actor || standingOn instanceof Trigger) {
						za = 0.02f; // bounce off of actors!
						xa += (Game.rand.nextFloat() * 0.01f - 0.005f) * delta;
						ya += (Game.rand.nextFloat() * 0.01f - 0.005f) * delta;
					}
				}
			}
		}

        // ceiling collision?
        if (isSolid && (za > 0 && !level.isFree(x, y, z + za, collision, stepHeight, false, null))) {
            za = 0;
        }

		if(standingOn == null) {
			z += za * delta;
		}
		else if(za <= 0) {
			isOnEntity = true;
		}

		boolean wasOnFloor = isOnFloor;
		float lastZa = za;

		float floorHeight = level.maxFloorHeight(x, y, z, collision.x);

		float floorClampMod = 0.035f;
		if(floating || za > 0) floorClampMod = 0f;

		isOnFloor = z <= (floorHeight + 0.5f) + floorClampMod;

		if(!isOnFloor && !isOnEntity) {
            if(!isOnLadder && !floating)
                za -= 0.0035f * delta; // falling; add gravity
        }
		else if(!isOnLadder)
		{
			float stepUpToHeight = floorHeight + 0.5f;
			if(standingOn != null && standingOn.z + standingOn.collision.z - z < stepHeight && standingOn.z + standingOn.collision.z > stepUpToHeight) stepUpToHeight = standingOn.z + standingOn.collision.z;

			if(isSolid) {
                if (level.collidesWorldOrEntities(x, y, stepUpToHeight, collision, this)) {
                    if (stepUpToHeight > z) {
                        stepUp(stepUpToHeight - z);
                        z = stepUpToHeight;
                    } else if (isOnFloor && !isOnEntity) {
                        z = floorHeight + 0.5f;
                    } else if (isOnEntity) {
                        z = stepUpToHeight;
                    }
                }
            }

            if(!floating) {
				if(isOnEntity && standingOn != null) {
					za = Math.max(za - (0.0035f * delta), standingOn.za);
				}
				else if (za < 0) {
					za = 0;
				}
			}
		}

		// headbob
		headbob = (float)Math.sin(tickcount * headBobSpeed) * Math.min((Math.abs(xa) + Math.abs(ya)), (headBobHeight * 0.5f)) * headBobHeight;
        if(floating) headbob = 0;

		// water movement
		inwater = false;
		Tile waterTile = level.findWaterTile(x, y, z, collision);
		if(waterTile != null)
		{
			if(lastZ >= waterTile.floorHeight + 0.5f) {
				splash(level, waterTile.floorHeight + 0.5f, waterTile);
				if(waterTile.data.hurts > 0)
					lavaHurtTimer = 0;
			}

			if(waterTile.data.hurts > 0) {
				lavaHurtTimer -= delta;
				if(lavaHurtTimer <= 0) {
					lavaHurtTimer = 30;
					this.hit(0, 0, waterTile.data.hurts, 0, waterTile.data.damageType, null);
				}
			}

			// walk sound
			if(isOnFloor && waterTile.data.walkSound != null) {
				footstepsTimer -= delta;
				if(footstepsTimer < 0) {
					footstepsTimer = 25;
					float stepvol = Math.max(Math.abs(xa), Math.abs(ya) );
					stepvol = Math.min(stepvol, 1f);
					Audio.playSound(waterTile.data.walkSound, stepvol * 4f, 0.9f + Game.rand.nextFloat() * 0.2f);
				}
			}

			// water friction!
			friction = 0.08f;
			xa -= ((xa - (xa * 0.5f)) * friction) * delta;
			ya -= ((ya - (ya * 0.5f)) * friction) * delta;

			// let the player climb up out of the water
			stepHeight = 0.3499f + (waterTile.floorHeight + 0.5f - z);

			headbob = (float)Math.sin(tickcount / 24.0f) * (headBobHeight * 0.067f);
			inwater = true;
		}
		else {
			stepHeight = 0.35f;

			// check if this tile hurts the player
			Tile current = level.getTileOrNull((int)(x), (int)(y));
			if(current == null) current = Tile.solidWall;

			if(current != null && current.data.hurts > 0 && z <= current.getMaxFloorHeight() + 0.5f && isOnFloor) {
				lavaHurtTimer -= delta;
				if(lavaHurtTimer <= 0) {
					lavaHurtTimer = 30;
					this.hit(0, 0, current.data.hurts, 0, current.data.damageType, null);
				}
			}

			// walk sound
			if(isOnFloor && current.data.walkSound != null) {
				footstepsTimer -= delta;
				if(footstepsTimer < 0) {
					footstepsTimer = 20;
					float stepvol = Math.max(Math.abs(xa), Math.abs(ya) );
					stepvol = Math.min(stepvol, 1f);
					Audio.playSound(current.data.walkSound, stepvol * 2f, 0.9f + Game.rand.nextFloat() * 0.2f);
				}
			}
			else if(isOnEntity) {
				footstepsTimer -= delta;
				if(footstepsTimer < 0) {
					footstepsTimer = 20;
					float stepvol = Math.max(Math.abs(xa), Math.abs(ya) );
					stepvol = Math.min(stepvol, 1f);
					Audio.playSound(Tile.emptyWall.data.walkSound, stepvol * 2f, 0.9f + Game.rand.nextFloat() * 0.2f);
				}
			}

			// friction!
			if(isOnFloor) friction = current.data.friction;
			else if(isOnEntity || isOnLadder) friction = 1f;
			else friction = 0.1f;

			xa -= ((xa - (xa * 0.8f)) * friction) * delta;
			ya -= ((ya - (ya * 0.8f)) * friction) * delta;

			if(isOnFloor && current.data.applyStatusEffect != null) {
				current.data.applyStatusEffect(this);
			}
		}

		// floor drop effects
		if(wasOnFloor == false && isOnFloor == true && !inwater) {
			if(Math.abs(lastZa) > 0.03f) {
				Audio.playSound(dropSound, Math.abs(lastZa) * 1f, 1f);
				if(Math.abs(lastZa) > 0.06f) makeFallingDustEffect();
			}
		}

		// decay screenshake
		screenshakeAmount = Math.max(screenshakeAmount -= delta * 0.1f, 0);

		if(screenshakeAmount > 0 && Game.instance != null) {
			float shakeMod = Math.min(screenshakeAmount, 4f);
			screenshake.x = (float)Math.sin(Game.instance.time * 0.88f) * shakeMod * 0.36f;
			screenshake.y = (float)Math.cos(Game.instance.time * 0.9f) * shakeMod * 0.36f;
		}

        // ladder movement
        if(isOnLadder) {
            if(walkVelVector.y > 0f || this.zm > 0f) {
                float ladderMul = yrot;
                if (ladderMul > 0.2f) ladderMul = 0.2f;
                else if (ladderMul < -0.2f) ladderMul = -0.2f;
                za = ladderMul * 0.1f;
            }
            else za = 0f;

            if (isOnFloor && za < 0f) za = 0f;

            isOnLadder = false;
        }

        // don't get sick
		if(drunkMod > 0) {
			if(drunkMod > 6) drunkMod = 6;
			drunkMod -= delta * 0.02;
		}
		else {
			drunkMod = 0;
		}
	}

	private void runPushCheck(Level level, float delta, CollisionAxis collisionAxis) {

		float checkX = x;
		float checkY = y;

		if(collisionAxis == CollisionAxis.X) {
			checkX = nextx;
		}
		else if(collisionAxis == CollisionAxis.Y) {
			checkY = nexty;
		}

		// This is all the same collision check from the tick function copy pasted here, probably should generalize this
		Entity encroaching = null;
		if(isSolid) {
			encroaching = level.getHighestEntityCollision(checkX, checkY, z, collision, this);
			if(encroaching == null) {
				encroaching = level.checkStandingRoomWithEntities(checkX, checkY, z, collision, this);
			}
		}

		if(encroaching == null || z > encroaching.z + encroaching.collision.z - stepHeight) {
			// are we touching an entity?
			if(encroaching != null) {
				// maybe we can climb on it
				if( z > encroaching.z + encroaching.collision.z - stepHeight &&
						level.collidesWorldOrEntities(checkX, checkY, encroaching.z + encroaching.collision.z, collision, this) && encroaching.canStepUpOn) {
					// can climb, no push
				}
				else {
					encroaching.push(this, level, delta, collisionAxis);
				}
			}
		}
		else {
			encroaching.push(this, level, delta, collisionAxis);
		}
	}

    private void setMusicVolume() {
        float musicLerp = Math.max(hp == 0 ? 0 : (float) hp / (float) maxHp, 0f);
        if(isDead) musicLerp = 0f;
        Audio.setMusicTargetVolume(musicLerp);
    }

    public void die() {
        Gdx.app.log("DelverGame", "Oh noes :( Player is dying!");
        Array<LerpFrame> deathFrames = new Array<LerpFrame>();

        floating = false;

        if(!inwater) {
            deathFrames.add(new LerpFrame(new Vector3(), new Vector3(), 275f));
            deathFrames.add(new LerpFrame(new Vector3(0, -0.3f, 0.1f), new Vector3(0, 0, 70), 1000f));
            deathFrames.add(new LerpFrame(new Vector3(0, -0.3f, 0.1f), new Vector3(0, 0, 70), 10f));
        }
        else {
            deathFrames.add(new LerpFrame(new Vector3(), new Vector3(), 275f));
            deathFrames.add(new LerpFrame(new Vector3(0, 0f, 0.1f), new Vector3(70, 0, 0), 1000f));
            deathFrames.add(new LerpFrame(new Vector3(0, 0f, 0.1f), new Vector3(70, 0, 0), 10f));
        }

        dyingAnimation = new LerpedAnimation(deathFrames);
        dyingAnimation.play(6f, Interpolation.pow4In);

        Audio.playSound("sfx_death.mp3", 1f);

        dropItem(selectedBarItem, Game.instance.level, 0.075f);

        isDead = true;
    }

	public void editorTick(Level level, float delta) {
		walkVel = 0.05f;
		walkSpeed = 0.15f;
		rotSpeed = 0.009f;
		maxRot = 0.06f;

		isSolid = true;

		tick(level, delta);
	}

	public void tick(Level level, float delta, GameInput input) {

		boolean isInOverlay = OverlayManager.instance.current() != null && OverlayManager.instance.current().catchInput;

        // don't do anything if the player is dead.
        if(isDead) {
            dyingAnimation.animate(delta);
        }
		else {
            tickStatusEffects(delta);
        }

		// reset movement speed
		walkVel = 0.05f;
		walkSpeed = getWalkSpeed();
		rotSpeed = 0.009f;
		maxRot = 0.06f;

		// keep rotation in bounds
		if(Math.abs(rot) > 6.28318531) {
			if(rot > 0) rot = rot % 6.28318531f;
			else if(rot < 0) rot = rot % 6.28318531f;
		}

		boolean up = false, down = false, left = false, right = false, turnLeft = false, turnRight = false, turnUp = false, turnDown = false, attack = false, jump = false;

        if(!isDead && !isInOverlay) {
            up = input.isMoveForwardPressed();
            down = input.isMoveBackwardsPressed();
            left = input.isStrafeLeftPressed();
            right = input.isStrafeRightPressed();
            turnLeft = input.isTurnLeftPressed();
            turnRight = input.isTurnRightPressed();
            turnUp = input.isLookUpPressed();
            turnDown = input.isLookDownPressed();
            attack = input.isAttackPressed() || controllerState.attack;
            jump = input.isJumpPressed();
        }

		// Update player visibility
		Color lightColor = level.getLightColorAt(x, y, z, null, t_vislightColor);
		visiblityMod = Math.max(Math.max(lightColor.r, lightColor.g), lightColor.b);
		visiblityMod *= visiblityMod;

		if(heldItem != null) {
			if(handAnimation != null && handAnimation.playing) handAnimation.animate(delta);
		}
		else if(heldItem == null) handAnimation = null;

		// Check for mobile attack press
		if(Game.isMobile && !isInOverlay)
		{
			attack = input.isAttackPressed() || Game.hud.isAttackPressed() || controllerState.attack;

			if(!attack && (input.isLeftTouched()) )
			{
				if(tapLength == null) tapLength = 0;
				else tapLength++;
			}
			else if(tapLength != null)
			{
				if(tapLength < 10) {
					Use(level, Gdx.input.getX(),Gdx.input.getY());
				}

				tapLength = null;
			}
		}

		lastZ = z;

		// check to see if the player has moved far enough away from the stairs
		if(ignoreStairs)
		{
			float distance = Math.max(Math.abs(x - spawnX), Math.abs(y - spawnY));
			if(distance > 1) ignoreStairs = false;
		}

		// charging an attack makes walking slower
		if(attackCharge > 0)
		{
			float walkMod = attackCharge / attackChargeTime;
			walkMod = Math.min(walkMod, 1);

			walkVel *= ( 1 - ( 0.5f * walkMod ) * (1.2 - stats.DEX * 0.06f) );

			// if not holding anything, cancel the attack
			if(GetHeldItem() == null) attackCharge = 0;
		}

		if(GetHeldItem() instanceof Weapon) attackChargeSpeed = ((Weapon)GetHeldItem()).getChargeSpeed() + getAttackSpeedStatBoost();
		else attackChargeSpeed = 1;

		xm = 0;
		zm = 0;

		walkVelVector.set(0,0);
		if(up) walkVelVector.y += 1;
		if(down) walkVelVector.y -= 1;
		if(left) walkVelVector.x += 1;
		if(right) walkVelVector.x -= 1;

		walkVelVector = walkVelVector.nor();

		// walking backwards is slower
		if(walkVelVector.y < 0) {
			walkVelVector.x *= 0.8f;
			walkVelVector.y *= 0.5f;
		}

		zm += walkVelVector.y * walkVel;
		xm += walkVelVector.x * walkVel;

		// angle the camera a tad when strafing
        strafeCameraAngleMod *= (0.825f);
        strafeCameraAngleMod += (xm * delta);

		//controllers!
		if(input.usingGamepad && !isDead && !isInOverlay) {
			if(input.isCursorCatched()) {
				float controllerXMod = 0.02f * (Options.instance.mouseXSensitivity * 0.5f);
				float controllerYMod = (!Options.instance.mouseInvert ? 0.02f : -0.02f) * (Options.instance.mouseYSensitivity * 0.5f);
				rota += controllerState.controllerLook.x * controllerXMod * delta;
				rotya += controllerState.controllerLook.y * controllerYMod * delta;

				zm += -controllerState.controllerMove.y * walkVel;
				xm += -controllerState.controllerMove.x * walkVel;
			}
			else if(input.showingGamepadCursor) {
				if (controllerState.controllerMove.len2() > 0.001f) {
					input.gamepadCursorPosition.add(controllerState.controllerMove.x * 14f * Options.instance.mouseXSensitivity, -controllerState.controllerMove.y * 14f * Options.instance.mouseYSensitivity);
				}
				else {
					input.gamepadCursorPosition.add(-controllerState.controllerLook.x * 14f * Options.instance.mouseXSensitivity, controllerState.controllerLook.y * 14f * Options.instance.mouseYSensitivity);
				}

				input.gamepadCursorPosition.x = Math.min(input.gamepadCursorPosition.x, Gdx.graphics.getWidth());
				input.gamepadCursorPosition.y = Math.min(input.gamepadCursorPosition.y, Gdx.graphics.getHeight());
				input.gamepadCursorPosition.x = Math.max(input.gamepadCursorPosition.x, 0);
				input.gamepadCursorPosition.y = Math.max(input.gamepadCursorPosition.y, 0);

				Game.ui.mouseMoved((int)input.gamepadCursorPosition.x, Gdx.graphics.getHeight() - (int)input.gamepadCursorPosition.y);

				if(controllerState.use) {
					wasGamepadDragging = true;
					input.touchDown((int)input.gamepadCursorPosition.x, Gdx.graphics.getHeight() - (int)input.gamepadCursorPosition.y, input.gamepadPointerNum, 0);
				}
				else if(wasGamepadDragging) {
					wasGamepadDragging = false;
					input.touchUp((int)input.gamepadCursorPosition.x, Gdx.graphics.getHeight() - (int)input.gamepadCursorPosition.y, input.gamepadPointerNum, 0);
				}
			}
		}

		touchingItem = false;
		boolean inOverlay = OverlayManager.instance.current() != null && OverlayManager.instance.current().catchInput;

		if(!inOverlay && !isDead && ((Game.isMobile || !input.isCursorCatched()) && Gdx.input.isButtonPressed(Input.Buttons.LEFT))) {
			if(Game.hud.dragging != null) {
				touchingItem = true;
			}
			else if(Gdx.input.justTouched() && !attack && input.uiTouchPointer == null && input.lastTouchedPointer != null) {
				Entity touching = pickEntity(level, Gdx.input.getX(input.lastTouchedPointer), Gdx.input.getY(input.lastTouchedPointer), 0.9f);
				if(touching != null) input.uiTouchPointer = input.lastTouchedPointer;

				if(touching != null && touching instanceof Item) {
					touchingItem = true;

					// drag item
					if(touching.isActive) {
						if(touching instanceof Key || touching instanceof Gold) {
							touching.use(this, 0, 0);
						}
						else {
							touching.isActive = false;
							Game.hud.dragging = (Item)touching;
							Game.dragging = Game.hud.dragging;
							Game.hud.refresh();
						}
					}
				}
				else if (touching != null && !(touching instanceof Stairs) && !(touching instanceof Door)) {
					touching.use(this, Game.camera.direction.x, Game.camera.direction.z);
				}
			}
		}

		hovering = null;
		if(!touchingItem && !Game.isMobile && !input.isCursorCatched()) {
			hovering = pickItem(level, input.getPointerX(), input.getPointerY(), 0.9f);
		}

		if(!isDead && (!Game.isMobile || input.isCursorCatched()) && !OverlayManager.instance.shouldPauseGame()) {
			String useText = ReadableKeys.keyNames.get(Actions.keyBindings.get(Action.USE));
			if(Game.isMobile) useText = StringManager.get("entities.Player.mobileUseText");

			Entity centered = pickEntity(level, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0.7f);
			if(centered != null && centered != this) {
				if(centered instanceof Trigger) {
					Trigger t = (Trigger)centered;
					if(t.getTriggerStatus() == Trigger.TriggerStatus.WAITING || t.getTriggerStatus() == Trigger.TriggerStatus.TRIGGERED) {
						Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Player.useText"), useText, t.getUseVerb()));
					}
				}
				else if(centered instanceof Item && (Math.abs(centered.xa) < 0.01f && Math.abs(centered.ya) < 0.01f && Math.abs(centered.za) < 0.01f)) {
					Item i = (Item)centered;

					if (!i.isPickup) {
						Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Player.getItemText"), useText, ((Item) (centered)).GetName() + "\n" + ((Item) (centered)).GetInfoText()), ((Item) (centered)).GetTextColor());
					}
				}
				else if(centered instanceof Stairs) Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Player.useText"), useText, ((Stairs) (centered)).getUseText()));
				else if(centered instanceof Door) Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Player.useText"), useText, ((Door)centered).getUseText()));
				else if(centered instanceof ButtonModel) Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Player.useText"), useText, ((ButtonModel)centered).useVerb));
				else if(centered instanceof Actor && ((Actor)centered).getUseTrigger() != null) Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Player.useText"), useText, ((Actor)centered).getUseTrigger().useVerb));
			}
			else {
				// check for a wall hit
				Ray ray = Game.camera.getPickRay(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);

				float projx = ray.direction.x * 0.7f;
				float projy = ray.direction.z * 0.7f;

				int checkx = (int)(Math.floor(ray.origin.x + projx));
				int checky = (int)(Math.floor(ray.origin.z + projy));

				Tile hit = level.getTile(checkx, checky);
				if(hit instanceof ExitTile) Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Player.exitDungeonText"),useText));
			}
		}

		// keyboard input
		if(turnLeft) {
			rota += rotSpeed * delta;
			if(rota > maxRot) rota = maxRot;
		}
		else if(turnRight) {
			rota -= rotSpeed * delta;
			if(rota < -maxRot) rota = -maxRot;
		}

		rot += rota;
		rota *= 0.8;

		if(turnUp) {
			rotya += (rotSpeed * 0.6f) * delta;
			if(rotya > maxRot) rotya = maxRot;
		}
		else if(turnDown) {
			rotya -= (rotSpeed * 0.6f) * delta;
			if(rotya < -maxRot) rotya = -maxRot;
		}

		yrot += rotya;
		rotya *= 0.8;

		if(jump && (isOnFloor || isOnEntity) && !isOnLadder) {
			za += jumpHeight;
		}

		// touch movement
		if(Game.isMobile && !isDead && !isInOverlay) {
			float max = 60;

			if(input.isLeftTouched() && !(touchingItem && input.uiTouchPointer == input.leftPointer)) {
				deltaX = input.getLeftTouchPosition().x - Gdx.input.getX(input.leftPointer);
				deltaY = input.getLeftTouchPosition().y - Gdx.input.getY(input.leftPointer);

				deltaX *= Math.abs(deltaX);
				deltaY *= Math.abs(deltaY);

				deltaX *= GameScreen.cDelta;
				deltaY *= GameScreen.cDelta;

				if(deltaY > max) deltaY = max;
				else if(deltaY < -max) deltaY = -max;

				if(deltaX > max) deltaX = max;
				else if(deltaX < -max) deltaX = -max;

				deltaY /= max;
				deltaX /= max;

				if(Math.abs(deltaX) < 0.1f ) deltaX = 0;
				if(Math.abs(deltaY) < 0.1f ) deltaY = 0;

				if(!Game.ignoreTouch) {
					zm += (deltaY) * walkVel;
					xm += (deltaX) * walkVel;
				}
			}

			if((Game.hud.isAttackPressed()) && !(touchingItem && input.uiTouchPointer == input.rightPointer)) {

				deltaX = 0;
				deltaY = 0;

				Integer thisX = 0;
				Integer thisY = 0;

				if(input.isRightTouched()) {
					thisX = Gdx.input.getX(input.rightPointer);
					thisY = Gdx.input.getY(input.rightPointer);
				}
				else if(Game.hud.isAttackPressed() && input.uiTouchPointer != null) {
					thisX = Gdx.input.getX(input.uiTouchPointer);
					thisY = Gdx.input.getY(input.uiTouchPointer);
				}

				if(lastDelta == null) {
					lastDelta = new Vector2(thisX,thisY);
				}

				deltaX = (int)lastDelta.x - thisX;
				deltaY = (int)lastDelta.y - thisY;

				if(Options.instance != null && Options.instance.mouseInvert) deltaY *= -1;

				deltaX *= Options.instance.mouseXSensitivity;
				deltaY *= Options.instance.mouseYSensitivity;

				if(!Game.ignoreTouch) {
					rotya += (deltaY / 800f) * Game.GetUiSize() / 85f;
					rota += (deltaX / 400f) * Game.GetUiSize() / 85f;
				}

				lastDelta.set(thisX, thisY);
			}

			if(!(Game.hud.isAttackPressed())) {
				lastDelta = null;
			}
		}

		// reset the ignore touch flag
		if(Game.ignoreTouch) {
			Game.ignoreTouch = false;
		}

		tickcount += delta;
		if(hasAttacked && !attack) hasAttacked = false;
		if(doingHeldItemTransition) heldItemTransition += delta;

		// vertical look clamp
		if(yrot > 1.3) yrot = 1.3f;
		if(yrot < -1.3) yrot = -1.3f;

		// walk!
		float xMod = (float)(xm * Math.cos(rot) + zm * Math.sin(rot)) * walkSpeed * delta;
		float yMod = (float)(zm * Math.cos(rot) - xm * Math.sin(rot)) * walkSpeed * delta;

        // flight controls!
        if(floating) {

            float flySpeed = stats.SPD * 0.1f;

            if(!isOnFloor && !isOnEntity) {
                xMod = GameManager.renderer.camera.direction.x * zm * flySpeed;
                yMod = GameManager.renderer.camera.direction.z * zm * flySpeed;

                xMod += (float) (xm * Math.cos(rot)) * flySpeed * delta;
                yMod += (float) (-xm * Math.sin(rot)) * flySpeed * delta;
            }

            za += (GameManager.renderer.camera.direction.y) * 0.008f * walkVelVector.y * flySpeed;

            float flightFriction = 0.4f;
            za -= ((za - (za * 0.8f)) * flightFriction) * delta;
            xa -= ((xa - (xa * 0.8f)) * flightFriction) * delta;
            ya -= ((ya - (ya * 0.8f)) * flightFriction) * delta;
        }

        if(isOnLadder) {
            xMod *= 0.5f;
            yMod *= 0.5f;
        }

		xa += xMod * Math.min(friction * 1.4f, 1f);
		ya += yMod * Math.min(friction * 1.4f, 1f);

		tick(level, delta);

		if(handAnimateTimer > 0) {
			handAnimateTimer -= delta;

			Item held = GetHeldItem();
			if(held != null && held instanceof Weapon) {
				((Weapon)held).tickAttack(this, level, delta);
			}
		}
		else {
			Item held = GetHeldItem();
			if(handAnimation == null) playIdleAnimation(held);

			if(held instanceof Weapon || held instanceof Decoration || held instanceof FusedBomb) {
				// Automatic weapons should not do the attack when released
				boolean attackOnRelease = true;

				if(held instanceof Weapon) attackOnRelease = ((Weapon)held).chargesAttack;
				if(held instanceof Wand && ((Wand)held).autoFire) attackOnRelease = false;
				if(held instanceof Gun) {
				    attackOnRelease = false;
                    if(!attack) ((Gun)held).resetTrigger();
                }

				if((!attack && attackCharge > 0 && !hasAttacked)) {
					if(attackOnRelease) {
						Attack(level);
					}
					else {
						hasAttacked = true;
						attackCharge = 0;
					}
				}
				else if(attack && held instanceof Weapon && !((Weapon)held).chargesAttack)
				{
					if(held instanceof Gun) {
						Gun g = (Gun)held;
						if(g.canFire()) {
							g.doAttack(this, level, 1);
						}
					}
					else {
						attackCharge = attackChargeTime;
						Attack(level);
					}
				}
				else if(attack && attackCharge < attackChargeTime) {
					if(held instanceof Wand) {
						Wand w = (Wand) held;
						if (w.autoFire) {
							if(handAnimation != null) handAnimation.stop();
							Attack(level);
						}
						else {
							if(attackCharge <= 0) playChargeAnimation(attackChargeSpeed);
						}
					}
					else {
						if(attackCharge <= 0) playChargeAnimation(attackChargeSpeed);
					}

					attackCharge += attackChargeSpeed * delta;
				}
			}
			else if(held instanceof Potion)
			{
				if(attack)
				{
					((Potion)held).Drink(this);
					removeFromInventory(held);
				}
			}
			else if(held instanceof Food)
			{
				if(attack)
				{
					((Food)held).Eat(this);
					removeFromInventory(held);
				}
			}
			else if(held instanceof Note) {
				if (attack) {
					((Note)held).Read(this);
				}
			}
			else if(held instanceof Scroll) {
				if (attack) {
					((Scroll)held).Read(this);
				}
			}
			else if(held instanceof BagUpgrade) {
				if (attack) {
					((BagUpgrade)held).inventoryUse(this);
				}
			}
			else if(held instanceof Armor)
			{
				if(attack)
				{
					Armor a = (Armor)held;
					ChangeHeldItem(null, true);
					equip(a);
				}
			}
		}

        if(!isDead && !isInOverlay) {
            if(input.doUseAction() ||
                    controllerState.buttonEvents.contains(Action.USE, true)) Use(level);

            // inventory actions
            if(input.keyEvents.contains(Input.Keys.NUM_1)) DoHotbarAction(1);
            if(input.keyEvents.contains(Input.Keys.NUM_2)) DoHotbarAction(2);
            if(input.keyEvents.contains(Input.Keys.NUM_3)) DoHotbarAction(3);
            if(input.keyEvents.contains(Input.Keys.NUM_4)) DoHotbarAction(4);
            if(input.keyEvents.contains(Input.Keys.NUM_5)) DoHotbarAction(5);
            if(input.keyEvents.contains(Input.Keys.NUM_6)) DoHotbarAction(6);
			if(input.keyEvents.contains(Input.Keys.NUM_7)) DoHotbarAction(7);
			if(input.keyEvents.contains(Input.Keys.NUM_8)) DoHotbarAction(8);
			if(input.keyEvents.contains(Input.Keys.NUM_9)) DoHotbarAction(9);
			if(input.keyEvents.contains(Input.Keys.NUM_0)) DoHotbarAction(10);

            if(input.isDropPressed() && Game.instance.menuMode == Game.MenuMode.Hidden) {
                chargeDrop(delta);
            }
            else {
            	if(tossPower > 0 && selectedBarItem != null) {
					Audio.playSound("inventory/drop_item.mp3", 0.7f, Game.rand.nextFloat() * 0.1f + 0.95f);
					tossHeldItem(level, tossPower);
					tossPower = 0f;
				}
			}

            // Debug stuff!
            if(Game.isDebugMode) {
            	try {
					if (input.keyEvents.contains(Keys.K))
						OverlayManager.instance.push(new DebugOverlay(this));
					else if (input.keyEvents.contains(Keys.L))
						Game.instance.level.down.changeLevel(level);
					else if (input.keyEvents.contains(Keys.J))
						Game.instance.level.up.changeLevel(level);
				}
				catch(Exception ex) {
            		Gdx.app.error("DelverDebug", ex.getMessage());
				}
            }

            // gamepad menu input
			/*
            if(controllerState.buttonEvents.contains(ControllerState.Buttons.HOTBAR_RIGHT, true)) {
                if(Game.hotbar.gamepadPosition == null) Game.hotbar.gamepadPosition = 0;
                else Game.hotbar.gamepadPosition = (Game.hotbar.gamepadPosition + 1) % 6;
            }
            else if(controllerState.buttonEvents.contains(ControllerState.Buttons.HOTBAR_LEFT, true)) {
                if(Game.hotbar.gamepadPosition == null) Game.hotbar.gamepadPosition = 5;
                else {
                    Game.hotbar.gamepadPosition -= 1;
                    if(Game.hotbar.gamepadPosition < 0) Game.hotbar.gamepadPosition = 6 - Game.hotbar.gamepadPosition - 2;
                }
            }
            */

            if(input.doInventoryAction()) {
                Game.instance.toggleInventory();
            }
            else if (Game.instance.menuMode != Game.MenuMode.Hidden && Game.gamepadManager.controllerState.menuButtonEvents.contains(ControllerState.MenuButtons.CANCEL, true)) {
            	if (Game.instance.menuMode == Game.MenuMode.Inventory) {
            		Game.instance.toggleInventory();
				}
				else {
            		Game.instance.toggleCharacterScreen();
				}

            	Game.gamepadManager.controllerState.clearEvents();
            	Game.gamepadManager.controllerState.resetState();
			}

			if (input.keyEvents.contains(Keys.C)) {
				Game.instance.toggleCharacterScreen();
			}

            if (input.doNextItemAction()) {
                wieldNextHotbarItem();
            }

            if (input.doPreviousItemAction()) {
                wieldPreviousHotbarItem();
            }

            if (input.doMapAction()) {

            	// toggle map!
            	if(OverlayManager.instance.current() == null)
                	OverlayManager.instance.push(new MapOverlay());
				else
					OverlayManager.instance.clear();

                if (GameManager.renderer.showMap && Game.instance.getShowingMenu()) {
                    Game.instance.toggleInventory();
                }

                if (GameManager.renderer.showMap) Audio.playSound("/ui/ui_map_open.mp3", 0.3f);
                else Audio.playSound("/ui/ui_map_close.mp3", 0.3f);
            }

            if (input.doBackAction()) {
                if (Game.instance.getShowingMenu())
                    Game.instance.toggleInventory();
                else if (Game.instance.getInteractMode())
                    Game.instance.toggleInteractMode();
            }
        }

		if(isHoldingOrb && makeEscapeEffects) {
			tickEscapeEffects(level, delta);
		}

        updatePlayerLight(level, delta);
        tickAttached(level, delta);
    }

	private void chargeDrop(float delta) {
		if(heldItem == null) return;
		if(attackCharge != 0) return;

		float tossChargeSpeed = 1;
		if(GetHeldItem() instanceof Weapon) tossChargeSpeed = ((Weapon)GetHeldItem()).getChargeSpeed();

		if(tossPower == 0) {
			playTossChargeAnimation(tossChargeSpeed);
		}

		tossPower += tossChargeSpeed * 0.03f * delta;

		if (tossPower > 1f) {
			tossPower = 1f;
		}
	}

	private void updatePlayerLight(Level level, float delta) {
    	if (this.originalTorchColor == null) {
    		this.originalTorchColor = new Color(this.torchColor);
		}

		torchColor.set(this.originalTorchColor);

    	// tick held items
        Item primaryHeld = GetHeldItem();
        if(primaryHeld != null) primaryHeld.tickEquipped(this, level, delta, "PRIMARY");

        Item offhandItem = equippedItems.get("OFFHAND");
        if(offhandItem != null && !isHoldingTwoHanded()) offhandItem.tickEquipped(this, level, delta, "OFFHAND");

        // update player light
        if(!torchColor.equals(Color.BLACK)) {
            com.interrupt.dungeoneer.gfx.DynamicLight light = GlRenderer.getLight();
            if (light != null) {
                light.color.set(torchColor.r, torchColor.g, torchColor.b);
                light.position.set(x, z + getStepUpValue(), y);
                light.range = this.torchRange;

                GlRenderer.playerLightColor.set(torchColor.r, torchColor.g, torchColor.b);
            }
        }

        // update some flags
        holdingTwoHanded = (primaryHeld != null && primaryHeld instanceof Weapon && ((Weapon)primaryHeld).twoHanded);

        // clamp visibility mod value
        visiblityMod = Math.min(1f, visiblityMod);
    }

    private void playIdleAnimation(Item held) {
        if(handAnimation == null && held instanceof Weapon) {
            Weapon w = (Weapon)held;
            handAnimation = Game.animationManager.getAnimation(w.chargeAnimation);
            if(handAnimation == null) handAnimation = Game.animationManager.getAnimation(w.attackAnimation);
            if(handAnimation != null) {
                handAnimation.play(0f);
                handAnimation.stop();
            }
        }
    }

    private void playTossChargeAnimation(float animationSpeed) {
		LerpedAnimation previousAnimation = handAnimation;
		handAnimation = Game.animationManager.decorationCharge;
		if(handAnimation != null) handAnimation.play(animationSpeed * 0.03f, previousAnimation);
	}

	private void playChargeAnimation(float animationSpeed) {
		LerpedAnimation previousAnimation = handAnimation;

		Item w = GetHeldItem();
		w.onChargeStart();

		if(w instanceof Weapon) {
			Weapon weapon = (Weapon)w;
			handAnimation = Game.animationManager.getAnimation(weapon.chargeAnimation);
			if(handAnimation != null) handAnimation.play(animationSpeed * 0.03f, previousAnimation);
		}
		else if(w instanceof Decoration || w instanceof Potion || w instanceof FusedBomb) {
			handAnimation = Game.animationManager.decorationCharge;
			if(handAnimation != null) handAnimation.play(animationSpeed * 0.03f, previousAnimation);
		}

		if(handAnimation == null) playIdleAnimation(w);
	}

	private Item pickItem(Level level, int pickX, int pickY, float maxDistance) {
		Entity picked = pickEntity(level,pickX,pickY,maxDistance);
		if(picked != null && picked instanceof Item)
			return (Item)picked;
		return null;
	}

	private static Vector3 pickEntityTemp1 = new Vector3();
    private static Vector3 pickEntityTemp2 = new Vector3();
	private Entity pickEntity(Level level, int pickX, int pickY, float maxDistance) {
		if(Game.camera == null) return null;

		Vector3 levelIntersection = tempVec1.set(Vector3.Zero);
		Vector3 intersection = tempVec2.set(Vector3.Zero);
		Vector3 testPos = tempVec3.set(Vector3.Zero);

		Ray ray = Game.camera.getPickRay(pickX, pickY);
		boolean hitLevel = Collidor.intersectRayTriangles(ray, GameManager.renderer.GetCollisionTrianglesAlong(ray,maxDistance), levelIntersection, null);
		float worldHitDistance = tempVec4.set(ray.origin).sub(levelIntersection).len();

		Array<Entity> toCheck = level.spatialhash.getEntitiesAt(x, y, maxDistance);
		toCheck.removeValue(this, true);

		for(int i = 0; i < toCheck.size; i++) {
			Entity e = toCheck.get(i);
			if(!(e instanceof Sprite || e instanceof Light) && Math.abs(e.x - x) < maxDistance * 1.5 && Math.abs(e.y - y) < maxDistance * 1.5) {
				testPos.x = e.x;
				testPos.z = e.y;
				testPos.y = e.z;

				float colSizeMod = 0;
				if(e instanceof Item) {
					testPos.y = e.z - 0.34f;
					colSizeMod = 0.2f;
				}
				else if(e instanceof Stairs) {
				    colSizeMod = 0.6f;
                }

				if(e instanceof Item || e instanceof Stairs) {
					if(Intersector.intersectRaySphere(ray, testPos, e.collision.x / 1.5f + colSizeMod, intersection)) {
						Vector3 start = pickEntityTemp1.set(ray.origin);
						Vector3 end = pickEntityTemp2.set(intersection);
						float distance = start.sub(end).len();
						if(distance < maxDistance && (!hitLevel || (distance < worldHitDistance))) {
							pickList.add(e);
						}
					}
				}
				else {
                    BoundingBox b = CachePools.getAABB(e);
                    if(Intersector.intersectRayBounds(ray, b, intersection)) {
                        Vector3 start = pickEntityTemp1.set(ray.origin);
                        Vector3 end = pickEntityTemp2.set(intersection);
                        float distance = start.sub(end).len();

                        if(distance < (maxDistance + colSizeMod) && (!hitLevel || (distance < worldHitDistance * 1.1f))) {
                            pickList.add(e);
                        }
                    }
                }
			}
		}

		Entity found = null;

		// get the first item in the list, or anything else
		for(int i = 0; i < pickList.size; i++) {
			Entity e = pickList.get(i);
			if(found == null || e instanceof Item)
				found = e;
		}

		// clean up and return!
		pickList.clear();
		return found;
	}

	public void Use(Level level)
	{
		// try simple using first
		if(Game.isMobile) {
			Array<Entity> entities = Game.instance.level.entities;
			for(int i = 0; i < entities.size; i++) {
				Entity e = entities.get(i);
				if(e instanceof Item || e instanceof Stairs) {
					if(Math.abs(x - e.x) < 0.5f && Math.abs(y - e.y) < 0.5f && Math.abs(z - e.z) < 1f) {
						e.use(this, 0, 0);
						Game.ShowMessage("", 1);
						return;
					}
				}
				else if(e instanceof Door) {
					if(Math.abs(x - e.x) < 1f && Math.abs(y - e.y) < 1f && Math.abs(z - e.z) < 1f) {
						e.use(this, 0, 0);
						Game.ShowMessage("", 1);
						return;
					}
				}
				else if(e instanceof Trigger) {
					Trigger trigger = (Trigger)e;
					if(trigger.triggerType == TriggerType.USE && Math.abs(x - e.x) < 0.8f && Math.abs(y - e.y) < 0.8f) {
						e.use(this, 0, 0);
						Game.ShowMessage("", 1);
						return;
					}
				}
				else if(e instanceof ButtonModel) {
					ButtonModel trigger = (ButtonModel)e;
					if(Math.abs(x - e.x) < 0.8f && Math.abs(y - e.y) < 0.8f) {
						e.use(this, 0, 0);
						Game.ShowMessage("", 1);
						return;
					}
				}
				else if(e instanceof Actor) {
					if(Math.abs(x - e.x) < 0.8f && Math.abs(y - e.y) < 0.8f) {
						e.use(this, 0, 0);
						Game.ShowMessage("", 1);
						return;
					}
				}
			}
		}

		// use the entity / wall hit by the camera ray
		float usedist = 0.95f;
		Tile hit = null;

		Entity centered = pickEntity(level, Gdx.graphics.getWidth() / 2, (int)(Gdx.graphics.getHeight() / 2), 0.7f);

		if(centered != null)
		{
			float projx = ( 0f * (float)Math.cos(rot) + (float)Math.sin(rot)) * 1f;
			float projy = ((float)Math.cos(rot) - 0f * (float)Math.sin(rot)) * 1f;
			centered.use(this, projx, projy);
			return;
		}

		// check for a wall hit
		for(int i = 1; i < 10 && hit == null; i++)
		{
			float dstep = (i / 6.0f) * usedist;
			float projx = Game.camera.direction.x * dstep;
			float projy = Game.camera.direction.z * dstep;

			int checkx = (int)(Math.floor(x + projx));
			int checky = (int)(Math.floor(y + projy));
			hit = level.getTile(checkx, checky);
			if(hit != null && !hit.blockMotion) hit = null;
		}

		if(hit != null)
		{
			hit.use();
		}
	}

	private void Use(Level level, int touchX, int touchY) {
		// use the entity / wall hit by the camera ray
		float usedist = 0.95f;
		Entity near = null;
		Tile hit = null;

		Entity centered = pickEntity(level, touchX, touchY, 0.9f);

		if(centered != null && !(centered instanceof Stairs))
		{
			float projx = ( 0 * (float)Math.cos(rot) + (float)Math.sin(rot)) * 1;
			float projy = ((float)Math.cos(rot) - 0 * (float)Math.sin(rot)) * 1;
			centered.use(this, projx, projy);
			return;
		}

		Ray ray = Game.camera.getPickRay(touchX, touchY);

		// check for a wall hit
		for(int i = 1; i < 10 && near == null && hit == null; i++)
		{
			float dstep = (i / 6.0f) * usedist;
			float projx = ray.direction.x * dstep;
			float projy = ray.direction.z * dstep;

			int checkx = (int)(Math.floor(ray.origin.x + projx));
			int checky = (int)(Math.floor(ray.origin.z + projy));
			hit = level.getTile(checkx, checky);
			if(hit != null && !hit.blockMotion) hit = null;
		}

		if(hit != null)
		{
			hit.use();
		}
	}

	public void playAttackAnimation(Weapon w, float attackPower) {
        playAttackAnimation(w, attackPower, ((w.getSpeed() + getAttackSpeedStatBoost()) * 0.25f) + ((stats.DEX - 4) * 0.015f));
	}

    public void playAttackAnimation(Weapon w, float attackPower, float speed) {
        // may have to blend with the previous animation
        LerpedAnimation previousAnimation = null;
        if(handAnimation != null) {
            previousAnimation = handAnimation;
        }

        // play either the weak or strong attack animation
        if(attackPower < 0.5f || w.attackStrongAnimation == null )
            handAnimation = Game.animationManager.getAnimation(w.attackAnimation);
        else
            handAnimation = Game.animationManager.getAnimation(w.attackStrongAnimation);

        if(handAnimation != null) {
            if(previousAnimation != null) previousAnimation.stop();
            handAnimation.play(speed, previousAnimation);
        }
    }

	private void Attack(Level lvl)
	{
		hasAttacked = true;
		float attackPower = attackCharge / attackChargeTime;
		attackCharge = 0;

		Item held = GetHeldItem();
		if(held == null) return;
		else if(held instanceof Weapon)
		{
			Weapon w = (Weapon)held;

			// start the attack
			playAttackAnimation(w, attackPower);
			w.doAttack(this, lvl, attackPower);
		}
		else
		{
			dropItem(held, lvl, attackPower);
			heldItem = null;

			held.xa = (Game.camera.direction.x) * (attackPower * 0.3f);
			held.ya = (Game.camera.direction.z) * (attackPower * 0.3f);
			held.za = (Game.camera.direction.y) * (attackPower * 0.3f);

			held.za += attackPower * 0.05;

			if (held instanceof Potion) ((Potion)held).activateExplosion(false);
		}

		// alert nearby enemies!
		visiblityMod = 1f;
	}

	public void tossHeldItem(Level level, float attackPower) {
		Item held = GetHeldItem();
		if(held == null) return;

		dropItem(heldItem, level, 2f);
		heldItem = null;

		held.xa = (Game.camera.direction.x) * (attackPower * 0.3f);
		held.ya = (Game.camera.direction.z) * (attackPower * 0.3f);
		held.za = (Game.camera.direction.y) * (attackPower * 0.3f);

		held.za += attackPower * 0.05;

		held.tossItem(level, attackPower);
	}

	public Item dropItem(Integer invLocation, Level level, float throwPower) {
		if(invLocation == null || invLocation < 0 || invLocation >= inventory.size) return null;
		Item itm = inventory.get(invLocation);
		dropItem(itm, level, throwPower);
		if(invLocation == selectedBarItem) selectedBarItem = null;

		return itm;
	}

	public void throwItem(Item itm, Level level, float throwPower, float xOffset) {
		float projx = ( 0 * (float)Math.cos(rot) + 1 * (float)Math.sin(rot)) * 1;
		float projy = (1 * (float)Math.cos(rot) - 0 * (float)Math.sin(rot)) * 1;

		itm.isActive = true;
		itm.isDynamic = true;
		itm.z = z + 0.5f;
		itm.xa = projx * (throwPower * 0.3f);
		itm.ya = projy * (throwPower * 0.3f);
		itm.za = throwPower * 0.05f;
		itm.ignorePlayerCollision = true;

		level.SpawnEntity(itm);

		itm.x = (x + projx * 0.25f);
		itm.y = (y + projy * 0.25f);

		float x_projx = ( 0 * (float)Math.cos(rot + 1.8f) + 1 * (float)Math.sin(rot + 1.8f)) * 1;
		float y_projy = (1 * (float)Math.cos(rot + 1.8f) - 0 * (float)Math.sin(rot + 1.8f)) * 1;

		itm.xa -= x_projx * xOffset;
		itm.ya -= y_projy * xOffset;
	}

	public void dropItemFromInv(Integer invLocation, Level level, float throwPower, float xOffset) {
		Item itm = dropItem(invLocation, level, throwPower);
		if(itm == null) return;

		float projx = ( 0 * (float)Math.cos(rot) + 1 * (float)Math.sin(rot)) * 1;
		float projy = (1 * (float)Math.cos(rot) - 0 * (float)Math.sin(rot)) * 1;

		itm.x = (x + projx * 0f);
		itm.y = (y + projy * 0f);

		float x_projx = ( 0 * (float)Math.cos(rot + 1.8) + 1 * (float)Math.sin(rot + 1.8)) * 1;
		float y_projy = (1 * (float)Math.cos(rot + 1.8) - 0 * (float)Math.sin(rot + 1.8)) * 1;

		itm.xa -= x_projx * xOffset;
		itm.ya -= y_projy * xOffset;

		itm.ignorePlayerCollision = true;
	}

	public void dropItem(Item itm, Level level, float throwPower) {
        if(itm == null) return;

		float projx = (0 * (float)Math.cos(rot) + 1 * (float)Math.sin(rot)) * 1;
		float projy = (1 * (float)Math.cos(rot) - 0 * (float)Math.sin(rot)) * 1;

		itm.isActive = true;
		itm.x = (x + projx * 0);
		itm.y = (y + projy * 0);
		itm.z = z + 0.36f;
		itm.xa = projx * (throwPower * 0.3f);
		itm.ya = projy * (throwPower * 0.3f);
		itm.za = throwPower * 0.05f;
		itm.ignorePlayerCollision = true;
		itm.spawnChance = 1f;

		level.SpawnEntity(itm);
		itm.isDynamic = true;

		removeFromInventory(itm);
	}

	private void splash(Level level, float splashZ, Tile tile)
	{
		if(tickcount - lastSplashTime < 30) return;
		lastSplashTime = tickcount;

		Random r = new Random();
		int particleCount = 19;
		particleCount *= Options.instance.gfxQuality;

		for(int i = 0; i < particleCount; i++)
		{
            Particle p = CachePools.getParticle(x, y, splashZ, r.nextFloat() * 0.04f - 0.02f, r.nextFloat() * 0.04f - 0.02f, r.nextFloat() * 0.01f + 0.015f, tile.data.particleTex, tile.data.particleColor, tile.data.particleFullbrite);
            p.endScale = 0.1f;
			level.SpawnNonCollidingEntity(p);
		}

		int amt = 6;
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
			p.scale = 1f;

			p.xa = (0 * (float) Math.cos(rot) + 1 * (float) Math.sin(rot)) * 0.075f;
			p.ya = (1 * (float) Math.cos(rot) - 0 * (float) Math.sin(rot)) * 0.075f;

			p.xa += xa;
			p.ya += ya;

			p.x += p.xa * 0.2f;
			p.y += p.ya * 0.2f;

			p.maxVelocity = 0.0001f;
			p.dampenAmount = 0.85f;
			//p.airFriction = 0.94f;

			level.SpawnNonCollidingEntity(p);

			rot += rotAmount;
		}

		float volume = Math.min(Math.abs(za) * 2.25f, 0.35f);
		Audio.playSound("splash2.mp3", volume);
	}

	public void ChangeHeldItem(Integer invPos, boolean doTransition)
	{
		if(handAnimation == null || !handAnimation.playing) {
			selectedBarItem = invPos;

			if(doTransition) {
				doingHeldItemTransition = true;
				heldItemTransitionEnd = 16;
				heldItemTransition = 0;
			} else {
				doingHeldItemTransition = false;
			}

			handAnimation = null;
			heldItem = invPos;

			// switch sound!
			Item held = GetHeldItem();
			if(doTransition && held != null && held.equipSound != null) {
				Audio.playSound(held.equipSound, 0.1f, Game.rand.nextFloat() * 0.1f + 0.95f);
			}
		}

		Game.instance.refreshMenu();
		tossPower = 0f;
	}

	public Boolean isEquipped(Item item) {
		if(item == GetHeldItem()) return true;
		if(equippedItems.containsValue(item)) return true;

		return false;
	}

	public Boolean isHeld(Item item) {
		Item held = GetHeldItem();
		return item == held;
	}

	public boolean addToInventory(Item item) {
		return addToInventory(item, true);
	}

	public boolean addToInventory(Item item, boolean autoEquip)
	{
		if(item instanceof Key) {
			keys++;
			return true;
		}

		if(inventory.contains(item, true)) return false;

		// this might be a stack
		if(item instanceof ItemStack) {
			ItemStack stack = (ItemStack)item;
			for(int i = 0; i < inventory.size; i++) {
				Item check = inventory.get(i);
				if(check instanceof ItemStack && ((ItemStack)check).stackType.equalsIgnoreCase(stack.stackType)) {
					ItemStack otherStack = (ItemStack)check;
					otherStack.count += stack.count;
					stack.isActive = false;
					return true;
				}
			}
		}

		if(inventory.size < inventorySize) inventory.add(item);
		else
		{
			// find an open spot
			boolean foundSpot = false;
			for(int i = 0; i < inventorySize && !foundSpot; i++)
			{
				if(inventory.get(i) == null)
				{
					inventory.set(i, item);
					foundSpot = true;
				}
			}
			if(!foundSpot) return false;
		}

		if((item instanceof Weapon || item instanceof Decoration || item instanceof FusedBomb) && heldItem == null && autoEquip) {
			equip(item);
		}

		item.onPickup();

		Game.RefreshUI();
		return true;
	}

	public boolean addToBackpack(Item item) {

		if(inventory.contains(item, true)) return false;

		// Find a spot in the backpack
		for(int i = hotbarSize; i < inventorySize; i++) {
			if(inventory.get(i) == null) {
				inventory.set(i, item);
				item.onPickup();
				Game.RefreshUI();
				return true;
			}
		}

		return false;
	}

	public void removeFromInventory(Item item)
	{
		if(!inventory.contains(item, true)) return;
		dequip(item);

		// save all the equipped items, so we can update their locations when the array changes
		Item held = GetHeldItem();

		int pos = inventory.indexOf(item, true);
		inventory.set(pos, null);

		// reset the item locations
		if(held != null) heldItem = inventory.indexOf(held, true);

		Game.RefreshUI();
	}

	/** Returns true if player inventory has space for an item. */
	public boolean hasFreeInventorySpace() {
		return inventory.contains(null, true);
	}

	public void equip(Item item) {
		equip(item, true);
	}

	public void equip(Item item, boolean doTransition) {
		int itempos = inventory.indexOf(item, true);
		if(selectedBarItem != null && selectedBarItem == itempos) selectedBarItem = null;

		if(item instanceof Weapon || item instanceof Decoration || item instanceof Potion || item instanceof Food || item instanceof FusedBomb) {
			ChangeHeldItem(itempos, doTransition);
		}
		else if(item instanceof Armor) {
			equipArmor(item, true);
		}

		Game.instance.refreshMenu();
	}

	public void equipArmor(Item item, boolean playSound) {
		equipArmor(item);
		if(playSound) Audio.playSound("ui/ui_equip_armor.mp3", 0.4f);
	}

	public void equipArmor(Item item) {
		if(equippedItems.containsKey(item.equipLoc))
		{
			int swappos = inventory.indexOf(item, true);
			if(swappos >= 0) inventory.set(swappos, equippedItems.get(item.equipLoc));
		}

		equippedItems.put(item.equipLoc, item);

		int itempos = inventory.indexOf(item, true);
		if(itempos >= 0) inventory.set(inventory.indexOf(item, true), null);

		Game.hudManager.quickSlots.refresh();
		Game.hudManager.backpack.refresh();
		Game.hud.refreshEquipLocations();
	}

	public void wieldNextHotbarItem(){
		int num = Game.instance.player.hotbarSize;
		int i=selectedBarItem==null?0:(selectedBarItem+1) % num;
		ChangeHeldItem(i, true);
	}

	public void wieldPreviousHotbarItem(){
		int num = Game.instance.player.hotbarSize;
		int i=selectedBarItem==null ? (num - 1):(selectedBarItem + (num - 1)) % num;
		ChangeHeldItem(i, true);
	}

	public void dequip(Item item) {
		if(!equipped(item)) return;
		int itempos = inventory.indexOf(item, true);
		if(selectedBarItem != null && selectedBarItem == itempos) selectedBarItem = null;

		if(item instanceof Weapon || item instanceof Decoration || item instanceof Potion || item instanceof Food) {
			heldItem = null;
		}

		Game.instance.refreshMenu();
	}

	// Check if an item is equipped
	public boolean equipped(Item item) {
		if((item instanceof Weapon || item instanceof Decoration || item instanceof Potion || item instanceof Food) && GetHeldItem() == item) return true;
		else if(equippedItems.containsValue(item)) return true;

		return false;
	}

	public Item GetHeldItem() {
		if(heldItem == null || heldItem < 0 || heldItem >= inventory.size) return null;
		return inventory.get(heldItem);
	}

	public Item GetHeldOffhandItem() {
		return equippedItems.get("OFFHAND");
	}

	public Armor GetEquippedArmor() {
		Item itm = equippedItems.get("ARMOR");
		if(itm instanceof Armor) return (Armor)itm;
		return null;
	}

	public Item GetEquippedRing() {
		return equippedItems.get("RING");
	}

	public Item GetEquippedAmulet() {
		return equippedItems.get("AMULET");
	}

	public void UseInventoryItem(final int location) {
		if(location < 0 || location >= inventory.size) return;
		Item itm = inventory.get(location);

		if(itm != null) {
			if(selectedBarItem == null || selectedBarItem != location) {
				// nothing is selected, or the selected item is different
				if(equipped(itm)) {
					dequip(itm);
					return;
				}

				//try to use item
				if (itm.inventoryUse(this)) return;

				//if not usable then wield it
				ChangeHeldItem(location, true);
			} else {
				// same selected item as previous, so either dequip or unwield it
				if(equipped(itm) && (handAnimation == null || !handAnimation.playing)) {
					dequip(itm);
				}
				else {
					ChangeHeldItem(null, true);
				}
			}
		}
	}

	public void DoHotbarAction(final int hotbarSlot) {
		int location = hotbarSlot - 1;
		if(location < 0 || location >= inventory.size || location + 1 > Game.hudManager.quickSlots.columns) return;
		UseInventoryItem(location);
	}

	public String GetAttackText() {
		Item held = GetHeldItem();
		if(held != null) {
			if(held instanceof Weapon) {
				Weapon w = (Weapon)held;
				int randDamage = w.getRandDamage();
				int baseDamage = w.getBaseDamage() + w.getElementalDamage() + getDamageStatBoost();

				if(randDamage == 0) return Integer.toString(baseDamage);
				return MessageFormat.format(StringManager.get("entities.Player.weaponAttackText"), baseDamage, (randDamage + baseDamage));
			}
			else if(held instanceof Potion) {
				return String.format("%.0f",((Potion)held).getExplosionDamageAmount());
			}
			else if(held instanceof Decoration) {
				return "1";
			}
		}

		return "0";
	}

	@Override
	public void addExperience(int e)
	{
		exp += e;

		// ding?
		if(!isDead && exp >= getNextLevel() && canLevelUp)
		{
			level++; // ding!
			hp = getMaxHp();

			OverlayManager.instance.push(new LevelUpOverlay(this));
		}
	}

	@Override
	public void hitEffect(Level level, DamageType inDamageType) {

		Random r = new Random();
		int particleCount = 8;
		particleCount *= Options.instance.gfxQuality;

		for(int i = 0; i < particleCount; i++)
		{
			level.SpawnNonCollidingEntity( CachePools.getParticle(x, y, z + 0.6f, r.nextFloat() * 0.02f - 0.01f, r.nextFloat() * 0.02f - 0.01f, r.nextFloat() * 0.02f - 0.01f, 220 + r.nextInt(500), 1f, 0f, Actor.getBloodTexture(bloodType), Actor.getBloodColor(bloodType), false)) ;
		}

		shake(2f);
	}

	public void attackButtonTouched() {
		attackButtonWasPressed = true;
	}

	@Override
	public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator)
	{
		takeDamage(damage, damageType, instigator);
		shake(((float)damage / (float)getMaxHp()) * 5f);
	}

	@Override
	public int takeDamage(int damage, DamageType damageType, Entity instigator) {
        if(!isDead && !godMode) {
			int tookDamage = super.takeDamage(damage, damageType, instigator);

			if(tookDamage < 0)
				Game.flash(Colors.HEAL_FLASH, 20);
			else
				Game.flash(Colors.HURT_FLASH, 20);

			history.tookDamage(damage);

			return tookDamage;
		}

		return 0;
	}

	public float getAttackSpeed() {
		return attackSpeed + (stats.DEX * 0.016f);
	}

	public void setAttackSpeed(float attackSpeed) {
		this.attackSpeed = attackSpeed + getAttackSpeedStatBoost();
	}

	public void updateMouseInput(GameInput input) {
		/*if(!touchingItem && !Game.isMobile)
		{
			if(input.caughtCursor) {
				rot -= (((float)Gdx.input.getDeltaX()) / 230.0) * Options.instance.mouseXSensitivity;

				float deltaY = Gdx.input.getDeltaY();
				if(Options.instance.mouseInvert) deltaY *= -1;

				yrot -= (deltaY / 230.0) * Options.instance.mouseYSensitivity;

				if(yrot > 1.3) yrot = 1.3f;
				if(yrot < -1.3) yrot = -1.3f;
			}
		}*/
	}

	public float getWalkSpeed() {
		float baseSpeed = 0.10f + stats.SPD * 0.015f;
		if(statusEffects == null || statusEffects.size <= 0) return baseSpeed * GetEquippedSpeedMod();

		for(StatusEffect s : statusEffects) {
			if(s.active) baseSpeed *= s.speedMod;
		}

		return baseSpeed * GetEquippedSpeedMod();
	}

	public float getFieldOfViewModifier() {
		float adjustedFieldOfViewMod = 1.0f;
		if(statusEffects == null || statusEffects.size <= 0)
			return adjustedFieldOfViewMod;

		for(StatusEffect s : statusEffects) {
			if(s.active)
				adjustedFieldOfViewMod *= s.getFieldOfViewMod();
		}

		return adjustedFieldOfViewMod;
	}

	public void setupController() {
		try {
			if(Game.instance.input != null) {
				Game.instance.input.setGamepadManager(Game.gamepadManager);
				controllerState = Game.gamepadManager.controllerState;
			}
		}
		catch(Exception ex) { Gdx.app.log("Delver", ex.getMessage()); }
	}

	public void setMessageViews(String message, int views) {
		messageViews.put(message, (float)views);
	}

	public int getMessageViews(String message) {
		Float views = messageViews.get(message);
		if(views == null) return 0;
		return Math.round(views);
	}

	public int getDamageStatBoost() {
		return Math.max(0, stats.ATK - 4 + calculatedStats.ATK);
	}

	@Override
	public float getAttackSpeedStatBoost() {
		return stats.attackSpeedMod + calculatedStats.attackSpeedMod;
	}

	@Override
	public float getKnockbackStatBoost() {
		return stats.knockbackMod + calculatedStats.knockbackMod;
	}

	@Override
	public float getMagicResistModBoost() {
		return stats.magicResistMod + calculatedStats.magicResistMod;
	}

	public int getMagicStatBoost() {
		return Math.max(0, stats.MAG - 4 + calculatedStats.MAG);
	}

	public int getDefenseStatBoost() {
		return Math.max(0, stats.DEF - 4);
	}

	public void shake(float amount) {
        if(isDead) return;
        amount = Math.min(20, amount);
		screenshakeAmount = Math.max(screenshakeAmount, amount);
	}

	public void shake(float amount, float range, Vector3 position) {
        if(isDead) return;
		float distance = tempVec1.set(x,y,z).sub(position).len();
		float mod = 1 - Math.min(distance / range, 1f);

        float finalAmount = Math.min(20, amount * mod);
		screenshakeAmount = Math.max(screenshakeAmount, finalAmount);
	}

	public boolean isHoldingTwoHanded() {
		return holdingTwoHanded;
	}

	@Override
	public int getMaxHp() {
		return maxHp + calculatedStats.HP;
	}

	public int GetArmorClass() {
		return calculatedStats.DEF + getDefenseStatBoost();
	}

	public float GetEquippedSpeedMod() {
		float armorInfluence = 0.16f;
		float speedToWeightRatio = (stats.SPD + calculatedStats.SPD * armorInfluence) / (float)stats.SPD;

		return Math.max(speedToWeightRatio, minWalkSpeed);
	}

	public void makeFallingDustEffect() {

		for(int i = 0; i < 6; i++) {
			Particle p = CachePools.getParticle(x + xa, y + ya, z - za - 0.4f, 0, 0, 0, Game.rand.nextInt(3), Color.WHITE, false);

			// Randomize location a tad
			float offset = 0.25f;
			p.x += (offset * Game.rand.nextFloat()) - (offset * 0.5f);
			p.y += (offset * Game.rand.nextFloat()) - (offset * 0.5f);

			p.checkCollision = false;
			p.floating = true;
			p.lifetime = (int) (15 * Game.rand.nextFloat()) + 40;
			p.shader = "dust";
			p.spriteAtlas = "dust_puffs";
			p.startScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
			p.endScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
			p.endColor = new Color(1f, 1f, 1f, 0f);
			p.scale = 0.5f;

			offset = 0.01f;
			p.xa = (offset * Game.rand.nextFloat()) - (offset * 0.5f) + xa * 0.7f;
			p.ya = (offset * Game.rand.nextFloat()) - (offset * 0.5f) + ya * 0.7f;
			p.za = (0.00125f * Game.rand.nextFloat()) + 0.0025f;

			Game.GetLevel().SpawnNonCollidingEntity(p);
		}
	}

	@Override
	protected void UseArmor() {
		// reduce armor conditions
		for(Item e : equippedItems.values()) {
			if(e instanceof Armor && ((Armor)e).armor > 0) {
				((Armor)e).wasUsed();
			}
		}
	}

    public void addTravelPath(TravelInfo info) {
		travelPath.add(info);
    }

    public TravelInfo popTravelPath() {
		return travelPath.pop();
	}

	public TravelInfo getCurrentTravelPath() {
		if(travelPath.size == 0) return null;
		TravelInfo info = travelPath.peek();
		if(info == null) return null;
		return info;
	}

	public String getCurrentTravelKey() {
		if(travelPath.size == 0) return null;
		TravelInfo info = travelPath.peek();
		if(info == null) return null;
		return info.locationUuid;
	}

	public void rotateCamera(int deltaX, int deltaY, boolean caughtCursor) {
		if (Game.isDebugMode &&
				(Gdx.input.isKeyPressed(Keys.R) ||
				 Gdx.input.isKeyPressed(Keys.G) ||
				 Gdx.input.isKeyPressed(Keys.B) ||
				 Gdx.input.isKeyPressed(Keys.F) ||
				 Gdx.input.isKeyPressed(Keys.V))) {
			return;
		}

		if(!touchingItem && !Game.isMobile )
		{
			if(caughtCursor) {
				rot -= deltaX * 0.003 * Options.instance.mouseXSensitivity;
				if(Options.instance.mouseInvert) deltaY *= -1;
				yrot -= deltaY * 0.003 * Options.instance.mouseYSensitivity;
			}
		}
	}

    public float getHeadRoll() {
		if(drunkMod == 0) return 0;
		return (float)Math.sin(GlRenderer.time) * drunkMod;
    }

    private transient float t_timeSinceEscapeEffect = 0;
	private transient Color escapeFlashColor = new Color();
	private void tickEscapeEffects(Level level, float delta) {
		t_timeSinceEscapeEffect += delta;

		if(t_timeSinceEscapeEffect > 300) {
			t_timeSinceEscapeEffect = 0 - Game.rand.nextInt(150);

			float mod = Game.rand.nextFloat();

			boolean bigShake = Game.rand.nextBoolean();

			// flash
			if(bigShake) {
				mod += 0.25f;
				escapeFlashColor.set(Color.PURPLE);
				escapeFlashColor.a = 0.3f + mod * 0.5f;
				if(escapeFlashColor.a > 0.75f)
					escapeFlashColor.a = 0.75f;
				Game.flash(escapeFlashColor, 20 + (int) (50 * mod));
			}
			else {
				mod *= 0.5f;
				t_timeSinceEscapeEffect *= 0.5f;
			}

			// play a sound
			Audio.playSound("break/earthquake1.mp3,break/earthquake2.mp3", mod * 0.2f + 0.1f);

			// shakey shake
			shake(2 + 4 * mod);

			// make dust!
			int dustRadius = 9;
			for(int dustX = (int)x - dustRadius; dustX < (int)x + dustRadius; dustX++ ) {
				for(int dustY = (int)y - dustRadius; dustY < (int)y + dustRadius; dustY++ ) {
					float particleX = dustX + Game.rand.nextFloat();
					float particleY = dustY + Game.rand.nextFloat();

					Tile t = level.getTileOrNull((int)particleX, (int)particleY);
					if(t != null && !t.blockMotion) {
						float particleZ = t.getCeilHeight(particleX, particleY);

						// only make particles that are in places we can see
						if(bigShake || Game.rand.nextFloat() < mod) {
							if (GameManager.renderer.camera.frustum.pointInFrustum(particleX, particleZ - 0.8f, particleY)) {
								Particle p = CachePools.getParticle(particleX, particleY, particleZ, 0, 0, 0, Game.rand.nextInt(3), Color.WHITE, false);

								p.checkCollision = false;
								p.floating = true;
								p.lifetime = (int) (200 * Game.rand.nextFloat()) + 40;
								p.shader = "dust";
								p.spriteAtlas = "dust_puffs";
								p.startScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
								p.endScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
								p.endColor = new Color(1f, 1f, 1f, 0f);
								p.scale = 0.5f;

								p.xa = 0;
								p.ya = 0;
								p.za = -0.005f + Game.rand.nextFloat() * -0.02f;

								Game.GetLevel().SpawnNonCollidingEntity(p);
							}
						}

						if(Game.rand.nextFloat() < 0.035f * mod) {
							BeamProjectile beam = new BeamProjectile(particleX, particleY, particleZ, 0, 0.0001f, -0.2f - Game.rand.nextFloat() * 0.1f, 4, DamageType.MAGIC, Color.PURPLE, this);
							beam.hitSound = "magic/mg_fwoosh.mp3";

							if(Game.rand.nextBoolean()) {
								Explosion e = new Explosion();

								e.spawns = new Array<Entity>();
								Fire f = new Fire();
								f.color = new Color(Color.PURPLE);
								f.scaleMod = 0;
								f.particleEffect = "Magic Fire Effect";
								f.lifeTime = 100 + Game.rand.nextInt(400);
								f.z = 0.3f;

								e.spawns.add(f);
								e.color = null;

								e.explodeSound = "magic/mg_fwoosh.mp3";

								beam.explosion = e;
							}

							Game.GetLevel().SpawnEntity(beam);
						}
					}
				}
			}
		}
	}

	public void updatePlaytime(float delta) {
		if(playtime >= 0) {
			playtime += delta;
		}
	}

	public String getPlaytime() {
		if(playtime < 0)
			return "??:??";

		float seconds = playtime;
		int minutes = (int)Math.floor(seconds / 60);
		int hours = (int)Math.floor(minutes / 60);

		minutes %= 60;
		seconds %= 60;

		String playtime = "";
		if(hours != 0) {
			playtime += String.format("%02d", hours) + ":";
		}

		if(hours == 0) {
			// "0:45"
			playtime += String.format("%01d", minutes);
		}
		else {
			// "1:04:45"
			playtime += String.format("%02d", minutes);
		}

		playtime += ":" + String.format("%02d", (int)seconds);

		return playtime;
	}

	@Override
	public void addStatusEffect(StatusEffect newEffect) {
		if(newEffect != null) {
			newEffect.forPlayer(this);
		}
		super.addStatusEffect(newEffect);
	}

	public void startPlaytime() {
		playtime = 0;
	}

    public void resetInventoryDrawables() {
		// Reset inventory drawables after art assets have been disposed
		for(int i = 0; i < inventory.size; i++) {
			Item itm = inventory.get(i);
			if(itm != null && itm.drawable != null)
				itm.drawable.refresh();
		}
    }
}
