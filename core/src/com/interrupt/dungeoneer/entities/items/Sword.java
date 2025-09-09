package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.triggers.BasicTrigger;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

import java.util.Random;

public class Sword extends Weapon {
	
	public Sword() { isSolid = true; attackAnimation = "swordAttack"; chargeAnimation = "swordCharge"; attackStrongAnimation = "swordAttackStrong"; equipSound = "/ui/ui_equip_item.mp3"; }
	
	public ProjectedDecal hitDecal = new ProjectedDecal(ArtType.sprite, 18, 1.0f);

	public Sword(float x, float y) {
		super(x, y, 8, ItemType.sword, "SWORD");
	}

	/** Sound played when Sword hits a wall */
	@EditorProperty
	public String wallHitSound = "clang.mp3,clang_02.mp3,clang_03.mp3,clang_04.mp3";

	/** Sound played when Sword is swung */
	@EditorProperty
	public String swingSound = "whoosh1.mp3,whoosh1_02.mp3,whoosh1_03.mp3,whoosh1_04.mp3";
	
	private float attackTimer = 0;
	private float lastTickTime = 0;
	private float attackPower = 0;
	
	private float hitTime = 10f;
	
	@Override
	public void doAttack(Player p, Level lvl, float attackPower) {
		this.attackPower = attackPower;

		if(p == null || p.handAnimation == null) {
			return;
		}
		
		p.setAttackSpeed(getSpeed());
		p.handAnimateTimer = (p.handAnimation.length() / p.handAnimation.speed) * 0.75f;
		
		attackTimer = 0;
		lastTickTime = 0;
		
		hitTime = (p.handAnimation.actionTime / p.handAnimation.speed) * 0.5f;
		
		Audio.playSound(swingSound, 0.25f, Game.rand.nextFloat() * 0.1f + 0.95f);
	}
	
	public void tickAttack(Player p, Level lvl, float time) {
		attackTimer += time;
		
		if(attackTimer >= hitTime && lastTickTime < hitTime) {
			float usedist = reach;
			Entity near = null;
			
			Vector3 attackDir = new Vector3(Game.camera.direction);
			
			float hitX = 0f;
			float hitY = 0f;
			float hitZ = 0f;
			
			// sweep the collision
			for(int i = 1; i < 10 && near == null; i++)
			{
				float dstep = (i / 6.0f) * usedist;
				float projx = attackDir.x * dstep;
				float projy = attackDir.z * dstep;
				float projz = attackDir.y * dstep;
				
				hitX = p.x + projx;
				hitY = p.y + projy;
				hitZ = p.z + projz - 0.14f;
				
				near = lvl.checkEntityCollision(p.x + projx, p.y + projy, p.z + projz + 0.4f, 0.2f, 0.2f, 0.2f, null, p);
			}
			
			if(near != null)
			{
				float projx = ( 0 * (float)Math.cos(p.rot) + usedist * (float)Math.sin(p.rot)) * 1;
				float projy = (usedist * (float)Math.cos(p.rot) - 0 * (float)Math.sin(p.rot)) * 1;
				
				int attackroll = doAttackRoll(attackPower, p);
				near.hit(projx, projy, attackroll, attackPower * (knockback + p.getKnockbackStatBoost()), getDamageType(), Game.instance.player);
				
				if (near instanceof Breakable) {
					((Breakable)near).doHitEffect(hitX, hitY, hitZ, this, lvl);
				}
				else if (near instanceof Door) {
					((Door)near).doHitEffect(hitX, hitY, hitZ, this, lvl);
				}
				else if(near instanceof BasicTrigger) {
					doHitEffect(hitX, hitY, hitZ, lvl);
				}
				else if(!near.isDynamic) {
					doHitEffect(hitX, hitY, hitZ, lvl);
				}

				magicHitVfx(hitX, hitY, hitZ, lvl);
			}
			else
			{	
				for(int i = 1; i < 10 && near == null; i++)
				{
					float dstep = (i / 6.0f) * usedist;
					float projx = attackDir.x * dstep;
					float projy = attackDir.z * dstep;
					float projz = attackDir.y * dstep;
					if(!lvl.isFree(p.x + projx, p.y + projy, p.z + projz + 0.26f, new Vector3(0.15f, 0.15f, 0.25f), 0, false, null))
					{
						doHitEffect(p.x + projx, p.y + projy, p.z + projz - 0.14f, lvl);
						magicHitVfx(p.x + projx, p.y + projy, p.z + projz - 0.14f, lvl);
						wasUsed();
						break;
					}
				}
			}
		}
		
		lastTickTime = attackTimer;
	}
	
	public void doHitEffect(float xLoc, float yLoc, float zLoc, Level lvl) {
		Audio.playSound(wallHitSound, 0.25f, Game.rand.nextFloat() * 0.1f + 0.95f);
		
		Color hitColor = getEnchantmentColor();
		boolean fullBright = getDamageType() != DamageType.PHYSICAL;
		
		if(fullBright) {
			// make a light at this location
			DynamicLight l = new DynamicLight(xLoc, yLoc, zLoc, new Vector3(hitColor.r * 0.85f, hitColor.g * 0.85f, hitColor.b * 0.85f));
			l.startLerp(new Vector3(0,0,0), 20, true);
			lvl.non_collidable_entities.add(l);
		}
		
		Random r = Game.rand;
		for(int ii = 0; ii < r.nextInt(5) + 3; ii++)
		{
			Particle p = CachePools.getParticle(xLoc, yLoc, zLoc + 0.6f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.03f - 0.015f, 420 + r.nextInt(500), 1f, 0f, 0, hitColor, fullBright);
			p.movementRotateAmount = 10f;
			lvl.SpawnNonCollidingEntity(p);
		}

		// Make dust particle!
		for(int i = 0; i <= 1; i++) {
			Particle p = CachePools.getParticle(xLoc, yLoc, zLoc + 0.15f, 0, 0, 0, Game.rand.nextInt(3), Color.WHITE, false);

			// Randomize location a tad
			p.x += (0.15f * Game.rand.nextFloat()) - 0.075f;
			p.y += (0.15f * Game.rand.nextFloat()) - 0.075f;

			p.spriteAtlas = "dust_puffs";
			p.shader = "dust";
			p.checkCollision = false;
			p.floating = true;
			p.lifetime = (30 + 60 * i);
			p.startScale = 0.5f + (0.5f * Game.rand.nextFloat() - 0.25f);
			p.endScale = p.startScale;
			p.scale = 0.5f;
			p.endColor = new Color(1f, 1f, 1f, 0f);
			p.fullbrite = false;

			if(fullBright) {
				p.color.mul(hitColor);
				p.endColor.mul(hitColor);
				p.fullbrite = true;
				p.shader = "dust-fullbrite";
			}

			p.xa = (0.00125f * Game.rand.nextFloat());
			p.ya = (0.00125f * Game.rand.nextFloat());
			p.za = 0.004f;
			p.maxVelocity = 0.005f;

			Game.GetLevel().SpawnNonCollidingEntity(p);
		}
		
		makeHitDecal(xLoc, yLoc, zLoc + 0.18f, new Vector3(Game.camera.direction.x, Game.camera.direction.z, Game.camera.direction.y));

		wallHitSpark(xLoc, yLoc, zLoc, lvl);
		
		Game.instance.player.shake(1.5f);
	}
	
	public void makeHitDecal(float hitx, float hity, float hitz, Vector3 direction) {
		if(hitDecal != null) {
			ProjectedDecal proj = new ProjectedDecal(hitDecal.artType, hitDecal.tex, hitDecal.decalWidth);
			proj.x = hitx;
			proj.y = hity;
			proj.z = hitz;
			proj.direction = new Vector3(Game.camera.direction.x, Game.camera.direction.z, Game.camera.direction.y);
			proj.roll = Game.rand.nextFloat() * 360f;
			
			proj.end = 0.6f;
			proj.start = 0.01f;
			proj.isOrtho = true;
			
			Game.instance.level.entities.add(proj);
		}
	}

	@Override
	public void bonkEntity(Entity hit, float speed) {
		if(hit == null) return;

		hit.hit(0, 0, doAttackRoll(1f, Game.instance.player), 0, getDamageType(), Game.instance.player);
		if(hit.isDynamic) {
			xa = 0;
			ya = 0;
		}

		if (hit instanceof Breakable) {
			((Breakable)hit).doHitEffect(x - 0.5f, y - 0.5f, z - 0.5f, this, Game.instance.level);
		}
		else if (hit instanceof Door) {
			((Door)hit).doHitEffect(x - 0.5f, y - 0.5f, z - 0.5f, this, Game.instance.level);
		}

		if(hit instanceof Player) {
			ignorePlayerCollision = true;
			Audio.playSound("hit.mp3,hit_02.mp3,hit_03.mp3,hit_04.mp3", speed * 6f);
		}
	}

	public void wallHitSpark(float xLoc, float yLoc, float zLoc, Level lvl) {
		if(getDamageType() == DamageType.PHYSICAL) {
			Color hitColor = Color.GRAY;
			Particle vfx = CachePools.getParticle(xLoc, yLoc, zLoc + 0.15f, 0, 0, 0, 4, Color.WHITE, true);
			vfx.spriteAtlas = "dust_puffs";
			vfx.shader = "spark";
			vfx.checkCollision = false;
			vfx.floating = true;
			vfx.lifetime = (8);
			vfx.startScale = 0.25f + (0.25f * Game.rand.nextFloat() - 0.25f);
			vfx.endScale = vfx.startScale;
			vfx.scale = 0.5f;
			vfx.color.set(1f, 1f, 1f, 0.25f);
			vfx.endColor = new Color(1f, 1f, 1f, 1.2f);
			vfx.fullbrite = false;
			vfx.color.mul(hitColor);
			vfx.endColor.mul(hitColor);
			vfx.fullbrite = true;
			lvl.SpawnNonCollidingEntity(vfx);
		}
	}

	public void magicHitVfx(float xLoc, float yLoc, float zLoc, Level lvl) {
		if(getDamageType() != DamageType.PHYSICAL) {
			Color hitColor = new Color(getEnchantmentColor());
			hitColor.mul(0.3f, 0.3f, 0.3f, 1f);
			Particle vfx = CachePools.getParticle(xLoc, yLoc, zLoc + 0.15f, 0, 0, 0.01f, 4, Color.WHITE, true);
			vfx.spriteAtlas = "vfx";
			vfx.shader = "fire";
			vfx.checkCollision = false;
			vfx.floating = true;
			vfx.lifetime = (30);
			vfx.startScale = 0.5f;
			vfx.endScale = 0.2f;
			vfx.scale = 0.5f;
			vfx.color.set(1f, 1f, 1f, 0.0325f);
			vfx.endColor = new Color(1f, 1f, 1f, 1f);
			vfx.fullbrite = false;
			vfx.color.mul(hitColor);
			vfx.endColor.mul(hitColor);
			vfx.fullbrite = true;
			vfx.playAnimation(0, 4, 30);
			lvl.SpawnNonCollidingEntity(vfx);
		}
	}
}
