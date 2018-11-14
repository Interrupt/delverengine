package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.MonsterManager;

import java.util.Random;

public class Corpse extends Entity {

	protected SpriteAnimation animation = null;

	public String monsterName = null;

	public Actor.BloodType bloodType = Actor.BloodType.Red;

	protected ProjectedDecal bloodPoolDecal = null;

	int hp = 10;

	public Corpse() { }

	protected short reviveAttempts = 0;

	public Corpse(Monster monster) {

		collidesWith = CollidesWith.staticOnly;
		bloodType = monster.bloodType;
		isSolid = true;

		monsterName = monster.name;
		bloodPoolDecal = monster.bloodPoolDecal;

		// Play the death animation
		if(monster.dieAnimation != null) {
			tex = monster.dieAnimation.start;
			artType = monster.artType;
			spriteAtlas = monster.spriteAtlas;
			yOffset = monster.yOffset;

			playAnimation(monster.dieAnimation);

			x = monster.x;
			y = monster.y;
			z = monster.z;

			xa = monster.xa;
			ya = monster.ya;
			za = monster.za;

			scale = monster.scale;

			collision.set(monster.collision);
			collision.z = 0.1f;

			shader = monster.shader;
			blendMode = monster.blendMode;

			// keep on burning!
			Entity fire = monster.getAttached(Fire.class);
			if(fire != null) {
				fire.x = 0;
				fire.y = 0;
				fire.z = 0;
				attach(fire);
			}
		}
		else {
			isActive = false;
		}
	}

	@Override
	public void init(Level level, Level.Source source) {
		if(source == Level.Source.LEVEL_LOAD && isActive) {
			Player p = Game.instance.player;

			if(p != null && p.isHoldingOrb && reviveAttempts == 0) {
				// maybe revive this monster
				if(Game.rand.nextBoolean()) {
					revive(level, p, Game.rand.nextBoolean());
				}
			}
		}
	}

	public void revive(Level level, Player p, boolean makeUndead) {
		if(p == null) return;
		if(!isActive) return;
		MonsterManager mm = Game.GetMonsterManager();

		if(mm == null)
			return;

		reviveAttempts++;

		// Don't revive too close to the player
		float xDist = Math.abs(p.x - x);
		float yDist = Math.abs(p.y - y);
		if(xDist < 8 && yDist < 8) {
			return;
		}

		// Don't spawn in water / lava
		Tile t = level.getTileOrNull((int)x, (int)y);
		if(t != null && t.data != null) {
			if(t.data.isWater) {
				return;
			}
		}

		if(makeUndead) {
			Monster m = mm.GetRandomMonster(Level.DungeonTheme.UNDEAD.toString());
			if(m != null) {
				level.SpawnEntity(m);
				isActive = false;
			}
		}
		else {
			Monster m = mm.GetMonster(level.theme, monsterName);
			if(m != null) {
				m.x = x;
				m.y = y;
				m.z = z;
				m.Init(level, 15);
				m.shader = "invisible";
				m.blendMode = BlendMode.ADD;
				level.SpawnEntity(m);
				isActive = false;
			}
		}
	}

	@Override
	public void hit(float projx, float projy, int damage, float knockback, Weapon.DamageType damageType, Entity instigator) {
		super.hit(projx, projy, damage, knockback, damageType, instigator);
		float force = Math.min(knockback, 0.075f);
		this.applyPhysicsImpulse(new Vector3(projx * force, projy * force, 0));

		Random r = Game.rand;
		int particleCount = 8;
		particleCount *= Options.instance.gfxQuality;

		for(int i = 0; i < particleCount; i++)
		{
			Game.instance.level.SpawnNonCollidingEntity( CachePools.getParticle(x, y, z + 0.12f, r.nextFloat() * 0.02f - 0.01f + xa, r.nextFloat() * 0.02f - 0.01f + ya, r.nextFloat() * 0.02f - 0.01f + za, 460 + r.nextInt(800), 1f, 0f, Actor.getBloodTexture(bloodType), Actor.getBloodColor(bloodType), false)) ;
		}

		if(isActive) {
			Audio.playPositionedSound(dropSound, new Vector3(x,y,z), Math.max(0.11f, knockback * 3f), 1f, 10f);
		}

		if(hp > 0) {
			hp -= damage;
			if (hp <= 0) {
				gib();
			}
		}
	}

	@Override
	public void tick(Level level, float delta)
	{
		super.tick(level, delta);

		if(animation != null && animation.playing) {
			animation.animate(delta, this);
		}
	}

	public void playAnimation(SpriteAnimation animation) {
		this.animation = animation;
		this.animation.play();
	}

	public void gib() {
		hidden = true;
		isDynamic = false;
		isSolid = false;

		Random r = Game.rand;
		int particleCount = 30;
		particleCount *= Options.instance.gfxQuality;

		for(int i = 0; i < particleCount; i++)
		{
			Game.instance.level.SpawnNonCollidingEntity( CachePools.getParticle(x, y, z + 0.12f, r.nextFloat() * 0.04f - 0.02f + (xa * 0.5f), r.nextFloat() * 0.04f - 0.02f + (ya * 0.5f), r.nextFloat() * 0.04f - 0.01f + (za * 0.5f), 600 + r.nextInt(800), 2f, 0f, Actor.getBloodTexture(bloodType), Actor.getBloodColor(bloodType), false)) ;
		}

		if(bloodPoolDecal != null && bloodPoolDecal.isActive) {
			ProjectedDecal proj = new ProjectedDecal(bloodPoolDecal);
			proj.decalHeight -= Game.rand.nextFloat() * 0.2f;
			proj.decalWidth = proj.decalHeight;
			proj.x = x;
			proj.y = y;
			proj.z = z + 0.2f;
			proj.direction = new Vector3(0.05f,0,-0.95f).nor();
			proj.roll = Game.rand.nextFloat() * 360f;
			proj.end = 1f;
			proj.start = 0.01f;
			proj.isOrtho = true;

			Game.instance.level.entities.add(proj);
		}
	}
}
