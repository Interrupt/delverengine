package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Door.DoorState;
import com.interrupt.dungeoneer.entities.Door.DoorType;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.entities.spells.Spell;
import com.interrupt.dungeoneer.entities.triggers.Trigger;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.animation.AnimationAction;
import com.interrupt.dungeoneer.gfx.animation.LightAnimationAction;
import com.interrupt.dungeoneer.gfx.animation.SpellCastAction;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.interfaces.Directional;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;

import java.util.Random;

public class Monster extends Actor implements Directional {

	public enum AmbushMode {
		None,
		WaitToSee
	}

	public Integer origtex = null;
	private float tickcount = 0;

	/** Is monster hostile towards the player? */
	@EditorProperty
	public boolean hostile = true;

	/** Will the monster be quiet and still until the player is near? **/
	@EditorProperty
	public AmbushMode ambushMode = AmbushMode.None;

	/** Distance which monster can hit. */
	@EditorProperty
	public float reach = 0.6f;

	/** Distance which monster begins attacking. */
	@EditorProperty
	public float attackStartDistance = 0.6f;

	/** Minimum level monster is allowed to be. */
	public int baseLevel = 0;

	/** Move speed. */
	@EditorProperty
	public float speed;

	public float targetx, targety, targetz;
	public float last_targetx, last_targety;
	public float last_picked_targetx, last_picked_targety;

	/** Does monster wander around? */
	@EditorProperty
	public boolean wanders = true;

	public transient boolean canSafelyNavigateToTarget = true;

	private transient float nextTargetf = 0f;

	/** Time interval between monster attacks. */
	@EditorProperty
	private float attackTime = 60;

	/** Time to wait before starting to move again after an attack. */
	@EditorProperty
	private float postAttackMoveWaitTime = 0.01f;

	/** Time interval between monster projectile attacks. */
	@EditorProperty
	private float projectileAttackTime = 100;

	/** Maximum distance monster can perform a projectile attack. */
	@EditorProperty
	public float projectileAttackMaxDistance = 30f;

	/** Minimum distance monster can perform a projectile attack. */
	@EditorProperty
	public float projectileAttackMinDistance = 0f;

	/** Vertical offset for projectile. */
	@EditorProperty
	public float projectileOffset = 0f;

	private float attacktimer = 0;
	private float rangedAttackTimer = 0;
	private float regenManaTimer = 0;

	private float stuntime = 0;
	private float postAttackMoveWaitTimer = 0;

	/** Is monster alerted to player's presence? */
	public boolean alerted = false;

	/** Is monster fleeing from the player? */
	public boolean fleeing = false;

	/** Does monster attempt to keep distance between themselves and the player? */
	public boolean keepDistance = false;

	private int lastWander = 0;
	private boolean waiting = false;

	private float bleedTimer = 0;

	/** Monster name. */
	public String name = "";

	/** Does monster chase after it's target? */
	@EditorProperty
	public Boolean chasetarget = true;

	public boolean ranged = false;

	/** Does monster have an attack animation? */
	@EditorProperty
	public boolean hasAttackAnim = true;

	/** Type of damage monster deals. */
	@EditorProperty
	private DamageType damageType = DamageType.PHYSICAL;

	/** Can monster open doors? */
	@EditorProperty
	private boolean canOpenDoors = true;

	/** Sound played when monster attacks. */
	@EditorProperty
	private String attackSound = null;

	/** Sound played when monster attacks. */
	@EditorProperty
	private String attackSwingSound = "enemy_swipes_01.mp3,enemy_swipes_02.mp3,enemy_swipes_03.mp3,enemy_swipes_04.mp3";

	/** Sound played when monster is hit. */
	@EditorProperty
	private String hitSound = "hit.mp3,hit_02.mp3,hit_03.mp3,hit_04.mp3";

	/** Sound played when monster is hurt. */
	@EditorProperty
	private String hurtSound = null;

	/** Sound played when monster first sees player. */
	@EditorProperty
	private String alertSound = null;

	/** Sound played when monster dies. */
	@EditorProperty
	private String dieSound = null;

	/** Sound played when monster starts fleeing player. */
	@EditorProperty
	private String fleeSound = null;

	/** Sound played while monster is idle. */
	@EditorProperty
	private String idleSound = null;

	/** Sound played while monster is walking. */
	@EditorProperty
	private String walkSound = null;

	/** Does monster have a chance to spawn random loot when it dies? */
	@EditorProperty(group = "Loot")
	private boolean spawnsLoot = true;

	/** Can dropped loot potentially be gold? */
	@EditorProperty(group = "Loot")
	private boolean lootCanBeGold = true;

	/** Percent chance to play pain animation when monster takes damage. */
	@EditorProperty
	private float painChance = 0.75f;

	/** Will flee the player when health below this percent. */
	@EditorProperty
	private float fleeThreshold = 0.25f;

	/** Entity to send trigger event when monster dies. */
	@EditorProperty(group = "Triggers")
	private String triggersOnDeath = null;

	/** Entity to send trigger event when monster takes damage. */
	@EditorProperty(group = "Triggers")
	private String triggersWhenHurt = null;

	/** Monster rotation. */
	public Vector3 rotation = new Vector3(Vector3.X);

	private float soundVolume = 0.45f;

	private float deathDelay = 22f;

	private float alertValue = 0f;

	private float stuckTime = 0f;
	private float stuckWanderTimer = 0f;

	/** Monster walk animation. */
	private SpriteAnimation walkAnimation = null;

	/** Monster attack animation. */
	private SpriteAnimation attackAnimation = null;

	/** Monster ranged attack animation. */
	private SpriteAnimation rangedAttackAnimation = null;

	/** Monster cast animation. */
	private SpriteAnimation castAnimation = null;

	/** Monster hurt animation. */
	private SpriteAnimation hurtAnimation = null;

	/** Monster death animation. */
	protected SpriteAnimation dieAnimation = null;

	/** Monster dodge animation. */
	private SpriteAnimation dodgeAnimation = null;

	private transient AmbientSound walkAmbientSound = null;

	/** Decal to place when monster dies. */
	protected ProjectedDecal bloodPoolDecal = new ProjectedDecal(ArtType.sprite, 16, 0.8f);

	/** Decal to place when monster is hurt. */
	protected ProjectedDecal bloodSplatterDecal = new ProjectedDecal(ArtType.sprite, 17, 0.5f);

	private transient float targetdist;
	private transient float pxdir;
	private transient float pydir;
	private transient float playerdist;
	private transient boolean foundPath;
	private transient Random random = new Random();
	private transient float txa;
	private transient float tya;
	private transient float tza;
	private transient float length;

	/** List of spells monster can cast. */
	public Array<Spell> spells;

	/** List of items monster will always drop when they die. */
	public Array<Item> loot;

	/** Entity that monster will throw/fire at player. */
	public Entity projectile = null;

	/** Scales how much arc the monster gives to projectile. */
	protected float projectileBallisticsMod = 0.1f;

	/** Initial velocity of projectile. */
	protected float projectileSpeed = 0.15f;

	private transient Float idleSoundTimer = null;

	/** Does monster award experience points when slain? */
	public boolean givesExp = true;

	/** List of random Entities to spawn when monster dies. */
	Array<Entity> spawns = new Array<Entity>();

	/** Number of spawns to create. */
	int spawnsCount = 1;

	/** Spawn initial velocity. */
	public Vector3 spawnVelocity = new Vector3(0.0f, 0.0f, 0.0625f);

	/** Spawn initial random velocity. */
	public Vector3 spawnRandomVelocity = new Vector3(0.125f, 0.125f, 0.0625f);

	/** Size of volume where spawns will be created. */
	public Vector3 spawnSpread = new Vector3(0.125f, 0.125f, 0.0f);

	/** Percent of parent speed to inherit. */
	public float spawnMomentumTransfer = 1.0f;

	private transient Entity attackTarget = null;

	public Monster() {
		// for ranged monsters
		maxMp = 10;
		mp = maxMp;

		stepHeight = 0.4f;

		attacktimer = 30;
		rangedAttackTimer = 30;

		mass = 2f;

		collision.x = 0.3f;
		collision.y = 0.3f;
		collision.z = 0.6f;

		artType = ArtType.entity;
		isSolid = true;
		bounces = false;

		shadowType = ShadowType.BLOB;
	}

	public Monster(float x, float y, int tex) {
		super(x, y, tex);
		artType = ArtType.entity;
		origtex = tex;
		isSolid = true;

		collision.x = 0.3f;
		collision.y = 0.3f;
		collision.z = 0.6f;

		bounces = false;

		Random random = new Random();
		tickcount = random.nextInt(1000);

		speed = 0.004f;

		targetx = x;
		targety = y;

		stepHeight = 0.4f;
	}

	public void Init(Level level, int playerLevel)
	{
		// If the player is scaling faster than the level difficulty, bump up a little bit
		int levelDifficulty = (int)(level.dungeonLevel * 1.5f);
		int calcedDifficulty = levelDifficulty;

		if(playerLevel > levelDifficulty) {
			int difference = playerLevel - levelDifficulty;
			calcedDifficulty += difference * 0.3f;
		}

		// Adjust randomly a tad
		if(calcedDifficulty > 4) {
			calcedDifficulty += Game.rand.nextInt(3) - 1;
		}

		// Put a lower cap on things
		if(calcedDifficulty <= 0)
			calcedDifficulty = 1;

		this.level = Math.max(calcedDifficulty, baseLevel);
		initLevel( this.level );

		this.init(level, Source.LEVEL_START);

		ranged = true;

		placeMonster(level);
	}

	public void placeMonster(Level level) {
		// Don't spawn if we are spawning *inside* something else
		boolean levelFree = level.isFree(x, y, z, collision, stepHeight, floating, null);
		boolean entityFree = level.checkEntityCollision(x, y, z, collision, this) == null;
		isActive = levelFree && entityFree;
	}

	@Override
	public void initLevel(int newLevel)
	{
		super.initLevel(newLevel);

		// If we've won the game already, monsters should be harder!
		int newGamePlusMod = Game.getNumberOfWins() * 2;

		atk += newGamePlusMod;
		STR += newGamePlusMod;
		DEF += newGamePlusMod;
		DEX += newGamePlusMod;
		INT += newGamePlusMod;

		stats.ATK += newGamePlusMod;
		stats.DEF += newGamePlusMod;
		stats.DEX += newGamePlusMod;
		stats.MAG += newGamePlusMod;
		stats.SPD += newGamePlusMod;
	}

	@Override
	public int takeDamage(int damage, DamageType damageType, Entity instigator) {
		int tookDamage = super.takeDamage(damage, damageType, instigator);
		if(doPainRoll(tookDamage)) {
			if (attackAnimation != null) attackAnimation.playing = false;
			if (rangedAttackAnimation != null) rangedAttackAnimation.playing = false;
			if (hurtAnimation != null) hurtAnimation.play();
		}
		return tookDamage;
	}

	public boolean doPainRoll(int damage) {
		if(hp <= 0) return true;
		if(damage < 0) return false;

		float damageMod = (float)damage / (float)maxHp;
		return Game.rand.nextFloat() <= (painChance + (damageMod * 0.5f));
	}

    protected boolean shouldFlee() {
        return hp <= maxHp * fleeThreshold;
    }

	@Override
	public void tick(Level level, float delta)
	{
		if(!isActive)
			return;

		// Time speed could be different for just us
		delta *= actorTimeScale;

		if(origtex == null) origtex = tex;
		tickStatusEffects(delta);
		stepUpTick(delta);

		Player player = GameManager.getGame().player;
		if(Math.abs(player.x - x) > 17 || Math.abs(player.y - y) > 17) {
			if(walkAmbientSound != null) walkAmbientSound.pause();
			return;
		}

		// sorry mob, you be dead
		if(!isAlive())
		{
			if(deathDelay > 0) {
				floating = false;
				deathDelay -= delta;
				if(hurtAnimation != null && hurtAnimation.playing) hurtAnimation.animate(delta, this);
			}
			else {
				die(level);
			}

			super.tick(level, delta);
			return;
		}

		if(hp <= maxHp / 2.0)
		{
			bleed(level);
		}

		// run away when health is low
		if(!fleeing && shouldFlee()) {
			Audio.playPositionedSound(fleeSound, new Vector3(x, y, z), soundVolume, 1f, 12f);
			fleeing = true;
		}

		// tick some timers (TODO: MAKE A TIMER HELPER)
		if(attacktimer > 0) attacktimer -= delta;
		if(rangedAttackTimer > 0) rangedAttackTimer -= delta;
		regenManaTimer += delta;

		if(regenManaTimer > 60) {
			regenManaTimer = Game.rand.nextInt(5);
			if(mp < maxMp) mp++;
		}

		targetdist = Math.min(Math.abs(targetx - x), Math.abs(targety - y));

		if(alerted && last_targetx == targetx && last_targety == targety && stuckWanderTimer == 0) {

			// are we stuck?
			if(Math.abs(xa) + Math.abs(ya) < 0.05f)
				stuckTime += delta;

			float speedStuckMod = 1.5f / getSpeed();
			if(stuckTime > speedStuckMod) {
				stuckWanderTimer = speedStuckMod * 0.75f;
				stuckTime = 0f;
			}
		}
		else {
			if(stuckTime > 0)
				stuckTime -= delta;
			if(stuckWanderTimer > 0)
				stuckWanderTimer -= delta;
		}

		last_targetx = targetx;
		last_targety = targety;

		boolean canSeePlayer = false;

		if(hostile) {
			canSeePlayer = level.canSeeIncludingDoors(x, y, player.x, player.y, 17);

			// Reset attack timers if we lose track of the player
			if(!canSeePlayer && attacktimer < 30) attacktimer = 30;
			if(!canSeePlayer && rangedAttackTimer < 30) rangedAttackTimer = 30;

			pxdir = player.x - x;
			pydir = player.y - y;
			playerdist = GlRenderer.FastSqrt(pxdir * pxdir + pydir * pydir);

			if(canSeePlayer && player.invisible) {
				if(alerted && playerdist > 1.5f) {
					canSeePlayer = false;
				}
				else if(playerdist > 0.75f) {
					canSeePlayer = false;
				}
			}
		}

		foundPath = false;

		if(keepDistance && !shouldFlee()) {
			if(playerdist < 3 && alerted) fleeing = true;
			else fleeing = false;
		}

		// Turn alerted if the player is visible
		if(hostile && (!alerted && canSeePlayer) && !fleeing)
		{
			if(playerdist < (player.visiblityMod * 15) + 3  || alertValue >= 1f) {
				alerted = true;

				// bark!
				Audio.playPositionedSound(alertSound, new Vector3(x, y, z), soundVolume, 1f, 12f);

				attacktimer = 40 + Game.rand.nextInt(20);
				rangedAttackTimer = 40 + Game.rand.nextInt(20);
			}
		}

		// Never reset back to ambush mode after being alerted
		if(alerted && ambushMode != AmbushMode.None) {
			ambushMode = AmbushMode.None;
		}

		if(alerted && playerdist > 0.7f && chasetarget && stuckWanderTimer <= 0) // When alerted, try to find a path to the player
		{
			nextTargetf -= delta;
			if(nextTargetf < 0 || targetdist < 0.12)
			{
				if(targetdist < 0.12) {
					// not stuck!
					stuckTime = 0f;
				}

				nextTargetf = 50f;

				// try to find a path to the player, or stop being alerted if the player ran away
				foundPath = findPathToPlayer(level);
				if(!foundPath && !canSeePlayer) alerted = false;
				else if(!foundPath && !fleeing)
				{
					if(level.canSafelySee(x, y, player.x, player.y)) {
						targetx = player.x;
						targety = player.y;
					}
					else {
						canSafelyNavigateToTarget = false;
					}
				}

				if(foundPath && floating) {
					targetz = level.getTile((int)targetx, (int)targety).getFloorHeight() + 0.2f;
				}
			}
		}
		else if(alerted && playerdist <= 1f && !fleeing) // If close enough to the player, skip pathfindinga
		{
			targetx = player.x;
			targety = player.y;
			stuckWanderTimer = 0f;
		}

		// Wander when not alerted
		boolean canWander = wanders && !alerted && ambushMode == AmbushMode.None;

		// Wander when there is no other path to the target
		boolean stuckWander = !canSafelyNavigateToTarget || !chasetarget || stuckWanderTimer > 0;

		if(canWander || stuckWander)
		{
			nextTargetf -= delta;

			if(waiting && nextTargetf < 0) waiting = false;

			if(!waiting && (targetdist < 0.05 || nextTargetf < 0))
			{
				if(nextTargetf < 0) lastWander = -1;
				nextTargetf = 100;

				int xPos = (int)x;
				int yPos = (int)y;

				Tile north = level.getTile(xPos, yPos - 1);
				Tile south = level.getTile(xPos, yPos + 1);
				Tile east = level.getTile(xPos - 1, yPos);
				Tile west = level.getTile(xPos + 1, yPos);

				int tryAt = random.nextInt(5);

				// handle dead ends
				if(lastWander == 0 && !canMoveTo(north) && !canMoveTo(east) && !canMoveTo(west))
				{
					lastWander = -1;
				}
				else if(lastWander == 1 && !canMoveTo(south) && !canMoveTo(east) && !canMoveTo(west))
				{
					lastWander = -1;
				}
				else if(lastWander == 2 && !canMoveTo(east) && !canMoveTo(north) && !canMoveTo(south))
				{
					lastWander = -1;
				}
				else if(lastWander == 3 && !canMoveTo(west) && !canMoveTo(north) && !canMoveTo(south))
				{
					lastWander = -1;
				}

				// wander at random, avoid reversing direction
				if(tryAt == 0 && lastWander != 1 && canDrylyMoveTo(north))
				{
					lastWander = 0;
					targetx = xPos;
					targety = yPos - 1;
					targetx += 0.5f;
					targety += 0.5f;
				}
				else if(tryAt == 1 && lastWander != 0 && canDrylyMoveTo(south))
				{
					lastWander = 1;
					targetx = xPos;
					targety = yPos + 1;
					targetx += 0.5f;
					targety += 0.5f;
				}
				else if(tryAt == 2 && lastWander != 3 && canDrylyMoveTo(east))
				{
					lastWander = 2;
					targetx = xPos - 1;
					targety = yPos;
					targetx += 0.5f;
					targety += 0.5f;
				}
				else if(tryAt == 3 && lastWander != 2 && canDrylyMoveTo(west))
				{
					lastWander = 3;
					targetx = xPos + 1;
					targety = yPos;
					targetx += 0.5f;
					targety += 0.5f;
				}
				else if(tryAt == 4)
				{
					if(random.nextInt(20) < 3)
					{
						//just wait a bit
						waiting = true;
						nextTargetf = 220f;
					}
				}

				Tile targetTile = level.getTile((int)targetx, (int)targety);
				targetz = targetTile.floorHeight;

				if(floating) {
					targetz = targetTile.floorHeight;
					targetz += Game.rand.nextFloat() * (targetTile.ceilHeight - targetTile.floorHeight - collision.z);
				}
			}
		}

		txa = targetx - x;
		tza = targety - y;
		tya = targetz - z;

		length = GlRenderer.FastSqrt(txa * txa + tza * tza);

		if(length > 0.25f)
		{
			txa /= length;
			tza /= length;
			tya /= length;
		}

		if(stuntime > 0) {
			stuntime -= 1;

			txa = 0;
			tza = 0;
		}

		if(hurtAnimation != null && hurtAnimation.playing) {
			txa = 0;
			tza = 0;
		}

		// stop moving after an attack
		if(postAttackMoveWaitTimer > 0) {
			boolean isPlayingAttackAnimation = false;

			if(attackAnimation != null)
				isPlayingAttackAnimation |= attackAnimation.playing;
			if(rangedAttackAnimation != null)
				isPlayingAttackAnimation |= rangedAttackAnimation.playing;
			if(castAnimation != null)
				isPlayingAttackAnimation |= castAnimation.playing;

			if(!isPlayingAttackAnimation)
				postAttackMoveWaitTimer -= delta;

			txa = 0;
			tza = 0;
		}

		// Attack the player once we're mad and alerted
		if(hostile && alerted) {
			if((playerdist < collision.x + reach || playerdist < collision.x + attackStartDistance)) {
				float zDiff = Math.abs((player.z + 0.3f) - (z + 0.3f));
				if (zDiff < reach || zDiff < attackStartDistance) {
					attack(player);
				}
			} else if(playerdist > projectileAttackMinDistance && playerdist < projectileAttackMaxDistance && (projectile != null || rangedAttackAnimation != null)) {
				rangedAttack(player);
			}
		}

		if(walkSound != null) {
			try {
				if(walkAmbientSound == null) {
					walkAmbientSound = new AmbientSound(x,y,z,walkSound,0.6f,1f,6f);
				}
				else {
					if(isOnFloor || isOnEntity) {
					    walkAmbientSound.resume();
						walkAmbientSound.volume = Math.min(1f, Math.max(Math.abs(txa), Math.abs(tza)));
					}
					else {
						walkAmbientSound.volume = 0f;
					}

					walkAmbientSound.x = x;
					walkAmbientSound.y = y;
					walkAmbientSound.z = z;

					walkAmbientSound.tick(level, delta);
				}
			}
			catch(Exception ex) { }
		}

		float calcSpeed = getSpeed();

		if(isOnFloor || isOnEntity || floating) {
			xa += (txa * calcSpeed) * delta;
			ya += (tza * calcSpeed) * delta;

			// set direction towards walk
			t_dirWork.set(txa, tza, 0).nor();
			setDirection(t_dirWork);
		}

		if(floating) {
			float flyingSpeed = calcSpeed * 4f;
			za += (tya * calcSpeed * 2f) * delta;
			if(za > flyingSpeed) za = flyingSpeed;
			if(za < -(flyingSpeed)) za = -(flyingSpeed);
		}

		z += za;

		super.tick(level, delta);

		tickcount += delta;

		if(stuntime <= 0) {
			tex = (int)(tickcount / 16) % 2 + origtex;
		}

		if(spells != null && spells.size > 0 && hostile && canSeePlayer && alerted && stuntime <= 0 && attacktimer <= 0 && !isParalyzed()) {

			int pickedSpell = Game.rand.nextInt(spells.size);
			Spell picked = spells.get(pickedSpell);

			setDirectionTowards(player);

			if(mp >= picked.mpCost && playerdist < picked.maxDistanceToTarget && playerdist > picked.minDistanceToTarget) {
				stuntime = 30;
				attacktimer = projectileAttackTime + Game.rand.nextInt(30);

				if(castAnimation != null && castAnimation.actions != null) {
					// TODO: has to be a better way to do this
					for(Array<AnimationAction> aa : castAnimation.actions.values()) {
						for(AnimationAction a : aa) {
							if(a instanceof SpellCastAction) {
								((SpellCastAction)a).setSpell(picked);
							}
							if(a instanceof LightAnimationAction) {
								LightAnimationAction action = (LightAnimationAction)a;
								if(action.useSpellColor)
									action.setEndColor(picked.spellColor);
							}
						}
					}
					castAnimation.play();
				}
				else {
					Vector3 dir = new Vector3(player.x, player.z, player.y).sub(x, z + projectileOffset, y).nor();
					picked.cast(this, dir);

					if(attackAnimation != null) {
						attackAnimation.play();
					}
				}
			}

			postAttackMoveWaitTimer = postAttackMoveWaitTime;
		}

		// idle sounds!
		if(ambushMode == AmbushMode.None && (!hostile || !chasetarget)) {
			if(idleSoundTimer == null) idleSoundTimer = 400f + Game.rand.nextFloat() * 400;
			idleSoundTimer -= delta;
			if(idleSoundTimer <= 0) {
				Audio.playPositionedSound(idleSound, new Vector3(x, y, z), soundVolume, 1f, 12f);
				idleSoundTimer = null;
			}
		}

		if(walkAnimation != null && !walkAnimation.playing)
			walkAnimation.loop();

		if(hurtAnimation != null && hurtAnimation.playing) hurtAnimation.animate(delta, this);
		else if(castAnimation != null && castAnimation.playing) castAnimation.animate(delta, this);
		else if(attackAnimation != null && attackAnimation.playing) attackAnimation.animate(delta, this);
		else if(rangedAttackAnimation != null && rangedAttackAnimation.playing) rangedAttackAnimation.animate(delta, this);
		else if(dodgeAnimation != null && dodgeAnimation.playing) dodgeAnimation.animate(delta, this);
		else if(walkAnimation != null) walkAnimation.animate(delta, this);
	}

	private float getSpeed() {
		float baseSpeed = speed;

		// don't run at full speed unless agitated
		if(chasetarget && !alerted) {
			baseSpeed *= 0.6f;
		}

		if(statusEffects == null || statusEffects.size <= 0) return baseSpeed;

		for(StatusEffect s : statusEffects) {
			if(s.active) baseSpeed *= s.speedMod;
		}

		return baseSpeed;
	}

	private boolean isGoodPathLocation(Level level, float checkX, float checkY, float checkZ) {
		Entity collidesWith = level.checkEntityCollision(checkX, checkY, checkZ, collision, this);
		if(collidesWith != null && collidesWith != attackTarget) {
			return false;
		}

		boolean checkAreaIsFree = level.isFree(checkX, checkY, checkZ, collision, stepHeight, floating, null);
		if(!checkAreaIsFree) {
			return false;
		}

		return true;
	}

	private boolean tryPathAdjust(Level level, float tryLocX, float tryLocY) {
		float checkX = Game.rand.nextBoolean() ? -0.6f : 0.6f;
		float checkY = Game.rand.nextBoolean() ? -0.6f : 0.6f;

		if(!isGoodPathLocation(level, tryLocX + checkX, tryLocY + checkY, z))
		{
			return false;
		}

		targetx = tryLocX + checkX;
		targety = tryLocY + checkY;
		stuckTime = -100f;
		return true;
	}

	private boolean findPathToPlayer(Level level)
	{
		PathNode node = Game.pathfinding.GetNodeAt(x + xa, y + ya, z + za);

		if(node != null && node.playerSmell != Short.MAX_VALUE) {
			PathNode picked = node;

			Array<PathNode> adjacent = node.getConnections();
			for(int i = 0; i < adjacent.size; i++) {
				PathNode a = adjacent.get(i);
				if(fleeing) {
					if (a.playerSmell > picked.playerSmell) {
						picked = a;
					}
				}
				else {
					if (a.playerSmell < picked.playerSmell) {
						picked = a;
					}
				}
			}

			adjacent = node.getJumps();
			for(int i = 0; i < adjacent.size; i++) {
				PathNode a = adjacent.get(i);
				if(fleeing) {
					if (a.playerSmell > picked.playerSmell) {
						picked = a;
					}
				}
				else {
					if (a.playerSmell < picked.playerSmell) {
						picked = a;
					}
				}
			}

			targetx = picked.loc.x;
			targety = picked.loc.y;
			return true;
		}

		return false;
	}

	@Override
	public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator)
	{
		if(!hostile) {
			hostile = true;	// u mad?
			attacktimer = 30;
		}

		alertValue = 1f;

		knockback = Math.min(knockback * 1.1f, 0.6f);
		xa = projx * (knockback);
		ya = projy * (knockback);

		stuntime = knockback * 5;

		Audio.playPositionedSound(hitSound, new Vector3(x, y, z), 0.8f, 1f, 12f);

		if (damage > 0) {
			takeDamage(damage, damageType, instigator);

			Level level = Game.GetLevel();
			hitEffect(level, damageType);

			if(hp > 0) {
				if(Game.rand.nextFloat() > 0.7f)
					Audio.playPositionedSound(hurtSound, new Vector3(x, y, z), soundVolume, 1f, 12f);
			}
			else {
				Audio.playPositionedSound(dieSound, new Vector3(x, y, z), soundVolume, 1f, 12f);
			}

			if(hp > 0) {
				if(bloodSplatterDecal != null && bloodSplatterDecal.isActive) {
					ProjectedDecal proj = new ProjectedDecal(bloodSplatterDecal);
					proj.decalHeight -= Game.rand.nextFloat() * 0.2f;
					proj.decalWidth = proj.decalHeight;
					proj.x = x;
					proj.y = y;
					proj.z = z + 0.2f;

					proj.direction = new Vector3(Game.rand.nextFloat() - 0.5f, Game.rand.nextFloat() - 0.5f, Game.rand.nextFloat() - 1f).nor();
					proj.roll = Game.rand.nextFloat() * 360f;

					proj.end = 1f;
					proj.start = 0.01f;
					proj.isOrtho = true;

					Game.instance.level.entities.add(proj);
				}

				if(triggersWhenHurt != null && !triggersWhenHurt.isEmpty()) {
					Game.GetLevel().trigger(this, triggersWhenHurt, name);
				}
			}
		}
	}

	public void spawnLoot(Level level) {
		Array<Item> toSpawn = new Array<Item>();

		// Random loot, 50% chance of spawning something
        if(spawnsLoot && random.nextBoolean()) {
            Item loot = Game.GetItemManager().GetMonsterLoot(this.level + 1, lootCanBeGold);
            if (loot != null) {
                toSpawn.add(loot);
            }
        }

		// Predefined loot
		if (this.loot != null) {
			for(Item i : this.loot) {
				toSpawn.add(i);
			}
		}

		// drop items around the monster
		for(Item itm : toSpawn) {
			// pick a random direction to drop the item in
			float rot = Game.rand.nextFloat() * 15f;
			float throwPower = 0.03f;
			float projx = 0;
			float projy = 0;

			// if spawning more than one, spawn in a ring
			if(toSpawn.size > 1) {
				projx = (0 * (float) Math.cos(rot) + 1 * (float) Math.sin(rot)) * 1;
				projy = (1 * (float) Math.cos(rot) - 0 * (float) Math.sin(rot)) * 1;
			}

			itm.isActive = true;
			itm.isDynamic = true;
			itm.z = z + 0.3f;
			itm.xa = projx * (throwPower * 0.4f);
			itm.ya = projy * (throwPower * 0.4f);
			itm.za = throwPower * 0.05f;
			itm.ignorePlayerCollision = true;
			itm.isSolid = false;

			level.SpawnEntity(itm);

			itm.x = (x + projx * 0.1f);
			itm.y = (y + projy * 0.1f);
		}
	}

	public void spawnEntities(Level level) {
		if (this.spawns != null && this.spawns.size > 0) {
			for (int i = 0; i < this.spawnsCount; i++) {
				// Grab a random spawn element to create
				int idx = Game.rand.nextInt(this.spawns.size);
				Entity e = EntityManager.instance.Copy(this.spawns.get(idx));

				// Honor entities' spawnChance property
				if (Game.rand.nextFloat() >= e.spawnChance) {
					e.isActive = false;
					continue;
				}

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

	public void die(Level level)
	{
		boolean splattered = hp < -500f;
		isActive = false;
		this.clearStatusEffects();

		Game.instance.player.history.addMonsterKill(this);
		if (this.givesExp) {
			Game.instance.player.addExperience(3 + this.level);
		}

		dieEffect(level);

		if(!splattered) {
			spawnLoot(level);
			spawnEntities(level);

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
		}

		if(walkAmbientSound != null) {
			walkAmbientSound.stop();
            walkAmbientSound = null;
		}

		// spawn a corpse if there is an animation for it
		if(dieAnimation != null && !splattered) {
			Corpse corpse = new Corpse(this);
			level.entities.add(corpse);
		}

		Audio.playPositionedSound("sfx_death_enemy_01.mp3,sfx_death_enemy_02.mp3,sfx_death_enemy_03.mp3,sfx_death_enemy_04.mp3", new Vector3(x, y, z), soundVolume, 1f, 12f);


		if(triggersOnDeath != null && !triggersOnDeath.isEmpty()) {
			level.trigger(this, triggersOnDeath, name);
		}
	}

	public void bleed(Level level)
	{
		if(tickcount > bleedTimer)
		{
			Random r = new Random();
			bleedTimer = tickcount + 10 + r.nextInt(80);

			float xPos = x + r.nextFloat() * 0.4f - 0.2f;
			float yPos = y + r.nextFloat() * 0.4f - 0.2f;
			float zPos = z + r.nextFloat() * 0.4f - 0.2f;

			if(bloodType != BloodType.Bone) {
				Game.GetLevel().SpawnNonCollidingEntity( CachePools.getParticle(xPos, yPos, zPos + 0.2f, (xPos - x) * 0.01f, (yPos - y) * 0.01f, 0, 420 + r.nextInt(600), 1f, 0f, Actor.getBloodTexture(bloodType), Actor.getBloodColor(bloodType), false));
			}
		}
	}

	public void attack(Entity target)
	{
		if(target == null || !target.isActive) return;
		if(!hostile) return;
		if(stuntime > 0) return;
		if(isParalyzed()) return;
		alerted = true;

		if(attacktimer > 0)
			return;

		if(dodgeAnimation != null) {
			dodgeAnimation.play();
			attacktimer = attackTime * 2;
			return;
		}

		attackTarget = target;
		attacktimer = attackTime + Game.rand.nextInt(10);
		Audio.playPositionedSound(attackSwingSound, new Vector3(x, y, z), 0.75f, 1f, 12f);

		setDirectionTowards(attackTarget);

		if(Game.rand.nextFloat() > 0.7f) Audio.playPositionedSound(attackSound, new Vector3(x, y, z), soundVolume, 1f, 12f);

		if(attackAnimation != null) {
			attackAnimation.play();

			// if no actions are set, just do the damage hit now
			// otherwise assume that there's a set damage frame
			if(attackAnimation.actions == null) {
				tryDamageHit(attackTarget, 0f, 0.05f);
			}
		}
		else {
			tryDamageHit(attackTarget, 0f, 0.05f);
		}

		postAttackMoveWaitTimer = postAttackMoveWaitTime;
	}

	private void rangedAttack(Entity target) {
		if(target == null || !target.isActive) return;
		if(!hostile) return;
		if(stuntime > 0) return;
		if(isParalyzed()) return;
		alerted = true;

		if(rangedAttackTimer > 0)
			return;

		attackTarget = target;
		rangedAttackTimer = projectileAttackTime + Game.rand.nextInt(15);
		setDirectionTowards(attackTarget);

		if(rangedAttackAnimation != null) {
			rangedAttackAnimation.play();

			// If no actions are set, try to spawn the basic projectile.
			// Otherwise, assume there is a ProjectileAttackAction in there.
			if(rangedAttackAnimation.actions == null) {
				spawnBasicProjectile(target);
			}
		}
		else {
			spawnBasicProjectile(target);
		}

		postAttackMoveWaitTimer = postAttackMoveWaitTime;
	}

	private void spawnBasicProjectile(Entity target) {
		// face fire direction!
		setDirectionTowards(target);

		rangedAttackTimer = projectileAttackTime;
		if(attackAnimation != null) {
			attackAnimation.play();
		}

		try {
			Entity pCopy = null;
			if(projectile instanceof Prefab) {
				Prefab p = (Prefab)projectile;
				pCopy = EntityManager.instance.getEntity(p.category, p.name);
			}
			else {
				pCopy = (Entity) KryoSerializer.copyObject(projectile);
			}

			if(pCopy != null) {
				pCopy.owner = this;
				pCopy.ignorePlayerCollision = false;

				// spawns from this entity
				pCopy.x = x;
				pCopy.y = y;
				pCopy.z = projectileOffset + z + (collision.z * 0.6f);

				Vector3 dirToTarget = new Vector3(target.x, target.y, target.z + (target.collision.z * 0.5f));
				if (!pCopy.floating) {
					// Go ballistics
					dirToTarget.z += (playerdist * playerdist) * projectileBallisticsMod;
				}

				dirToTarget.sub(pCopy.x, pCopy.y, pCopy.z).nor();

				// offset out of collision
				pCopy.x += dirToTarget.x * collision.x * 0.5f;
				pCopy.y += dirToTarget.y * collision.x * 0.5f;
				pCopy.z += dirToTarget.z * collision.x * 0.5f;

				// initial speed
				dirToTarget.scl(projectileSpeed);

				pCopy.xa = dirToTarget.x;
				pCopy.ya = dirToTarget.y;
				pCopy.za = dirToTarget.z;

				// Some items trigger effects when thrown
				if(pCopy instanceof Item)
					((Item)pCopy).tossItem(Game.instance.level, 1.0f);

				Game.instance.level.SpawnEntity(pCopy);
			}
		}
		catch(Exception ex) {
			Gdx.app.log("DelverGame", "Error spawning projectile: " + ex.getMessage());
		}
	}

	public void tryDamageHit(Entity target, float rangeBoost, float knockback) {
		// Assume we're attacking the player
		if(target == null || !target.isActive) {
			target = Game.instance.player;
		}

		if(target == null)
			return;

		float zoffset = 0.3f;

		Vector3 dir = new Vector3(target.x, target.y, target.z + zoffset).sub(new Vector3(x, y, z + zoffset));
		if(dir.len() > reach + rangeBoost + collision.x) return;

		if(target instanceof Actor) {
			Actor t = (Actor) target;
			int dealt = t.damageRoll(atk + level, damageType, this);

			if (!Game.isMobile && target instanceof Player)
				Game.flash(Colors.HURT_FLASH, 20);

			if (dealt > 0) {
				dir.z = 0f;
				dir.nor();

				t.xa = dir.x * knockback;
				t.ya = dir.y * knockback;

				t.hitEffect(Game.GetLevel(), DamageType.PHYSICAL);
			}
		}
		else {
			int dealt = Game.rand.nextInt(atk) + 1;
			target.hit(dir.x, dir.y, dealt, knockback, damageType, this);
		}
	}

	public void encroached(Player player)
	{
		attack(player);
	}

	public void encroached(Entity hit)
	{
		float speedStuckMod = 0.9f / getSpeed();

		if(hit instanceof Monster) {
			// push other monsters out of the way
			Vector3 myVec = CachePools.getVector3().set(x,y,z);
			myVec = myVec.sub(hit.x,hit.y,hit.z).nor();
			hit.xa -= myVec.x * 0.2f * Math.abs(xa);
			hit.ya -= myVec.y * 0.2f * Math.abs(ya);
		}
		else if(hit instanceof Door && canOpenDoors) {
			// open doors!
			Door d = (Door)hit;
			if(d.doorType == DoorType.NORMAL && !d.isLocked && (d.doorState == DoorState.CLOSED || d.doorState == DoorState.CLOSING)) {
				d.doOpen(true);
			}
		}
		else if(hit instanceof Breakable) {
			// Attack breakables that are in the way
			if(((canOpenDoors && alerted) || stuckTime > speedStuckMod * 0.5f) && Game.rand.nextFloat() > 0.95f) {
				Breakable b = (Breakable) hit;
				attack(b);
			}
		}
		else if(!hit.isDynamic && stuckTime > speedStuckMod * 0.25f) {
			// Try to move out of the way
			boolean foundPath = false;
			for(int i = 0; i < 4 && !foundPath; i++) {
				foundPath = tryPathAdjust(Game.instance.level, targetx, targety);
			}
		}
	}

	@Override
	public void use(Player player, float projx, float projy)
	{
		if(!hostile) {
			super.use(player, projx, projy);
		}
	}

	@Override
	public Trigger getUseTrigger() {
		if(hostile) return null;
		return useTrigger;
	}

	private boolean canMoveTo(Tile check)
	{
		if(check.blockMotion || check.data.hurts > 0 || !check.hasRoomFor(0.65f)) return false;
		if(floating) return true;

		float checkZ = z;
		if(fleeing && check.getFloorHeight() < checkZ) return true;
		if(Math.abs(check.getFloorHeight() + 0.5f - checkZ) > 0f + stepHeight) return false;
		return true;
	}

	private boolean canMoveTo(Tile check, Tile from)
	{
		if(check.blockMotion || check.data.hurts > 0 || !check.hasRoomFor(0.65f)) return false;
		if(floating) return true;

		float checkZ = Math.min(z, from.getFloorHeight() + 0.5f);
		if(Math.abs(check.getFloorHeight() + 0.5f - checkZ) > 0f + stepHeight) return false;
		return true;
	}

	private boolean canDrylyMoveTo(Tile check)
	{
		if(check.data.isWater && !floating) return false;
		return canMoveTo(check);
	}

	@Override
	public void init(Level level, Source source) {
		if(source != Source.LEVEL_LOAD) {
			// Set some default properties if being first created
			origtex = tex;

			tickcount = Game.rand.nextInt(1000);

			targetx = x;
			targety = y;

			hp = maxHp;
		}

		super.init(level, source);

		// add some base animations if none were defined
		if(walkAnimation == null) walkAnimation = new SpriteAnimation(tex, tex + 1, 32f, null);
		if(attackAnimation == null && hasAttackAnim) {
			attackAnimation = new SpriteAnimation(tex + 2, tex + 2, 24f, null);
		}

		// preload some sounds
		/*if(!Game.isMobile) {
			Audio.preload(dieSound);
			Audio.preload(attackSwingSound);
			Audio.preload(hurtSound);
			Audio.preload(idleSound);
			Audio.preload(alertSound);
		}*/

		// Let the lightmap color fade
		if(drawable instanceof DrawableSprite)
		{
			DrawableSprite s = (DrawableSprite)drawable;
			if(s.colorLastFrame == null) {
				Color lightmap = GameManager.renderer.GetLightmapAt(level, x + drawable.drawOffset.x, z, y + drawable.drawOffset.y);
				s.colorLastFrame = new Color(lightmap.r, lightmap.g, lightmap.b, 1f);
			}
		}
	}

	public void stun(float stunTime) {
		this.stuntime = stunTime;
	}

	public void delayAttack(float attackDelayTime) {
		this.attacktimer = attackDelayTime;
	}

	public void onDispose() {
		super.onDispose();
		if(walkAmbientSound != null) walkAmbientSound.onDispose();
	}

	@Override
	public void updateDrawable() {
		super.updateDrawable();
		if(drawable != null) {
			drawable.drawOffset.z = getStepUpValue() + yOffset;
		}
	}

	public Entity getAttackTarget() {
		if(attackTarget == null || !attackTarget.isActive) {
			return Game.instance.player;
		}
		return attackTarget;
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		triggersOnDeath = makeUniqueIdentifier(triggersOnDeath, idPrefix);
		triggersWhenHurt = makeUniqueIdentifier(triggersWhenHurt, idPrefix);
	}

	@Override
	public void rotate90() {
		super.rotate90();
		rotation.z -= 90f;
	}

	@Override
	public void rotate90Reversed() {
		super.rotate90Reversed();
		rotation.z += 90f;
	}

	@Override
	public void setRotation(float rotX, float rotY, float rotZ) {
		rotation.x = rotX;
		rotation.y = rotY;
		rotation.z = rotZ;
	}

	@Override
	public void rotate(float rotX, float rotY, float rotZ) {
		rotation.x += rotX;
		rotation.y += rotY;
		rotation.z += rotZ;
	}

	@Override
	public Vector3 getRotation() {
		return rotation;
	}

	public void setDirection(Vector3 dir) {

		if(attacktimer > 0 || stuntime > 0)
			return;

		float yaw = (float)Math.atan2(dir.x, dir.y);
		setRotation(0, 0, yaw * 57.2957795f - 90f);
	}

	private void setDirectionTowards(Entity attackTarget) {
		t_dirWork.set(attackTarget.x, attackTarget.y, 0).sub(x, y, 0).nor();
		setDirection(t_dirWork);
	}

	private transient Vector3 t_dirWork = new Vector3();
	@Override
	public Vector3 getDirection() {
		Vector3 dir = t_dirWork.set(1,0,0);
		dir.rotate(Vector3.Y, -rotation.y);
		dir.rotate(Vector3.X, -rotation.x);
		dir.rotate(Vector3.Z, -rotation.z);
		return dir;
	}

	@Override
	public void editorTick(Level level, float delta) {
		super.editorTick(level, delta);

		if(walkAnimation == null)
			return;

		if(!walkAnimation.playing)
			walkAnimation.loop();

		walkAnimation.animate(delta, this);
	}
}
