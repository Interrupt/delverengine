package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Door.DoorType;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.entities.projectiles.Projectile;
import com.interrupt.dungeoneer.entities.spells.SplashExplosion;
import com.interrupt.dungeoneer.entities.spells.Teleport;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.statuseffects.PoisonEffect;

public class Trap extends SpriteDecal {
	public enum TrapType { explosion, teleport, poison };
	private transient float ticks = 0;
	private transient final float checkSpeed = 8;
	public TrapType trapType = TrapType.explosion;
	private float resetTimer = 0;
	
	private boolean triggered = false;
	private float triggerTimer = 0;
	private float triggerDelay = 3;
	
	private transient boolean isStandingOn = false;
	private transient boolean playerTriggered = false;
	
	
	private boolean set = true;
	
	public Trap() {
		artType = ArtType.sprite;
	}
	
	public Trap(boolean pickRandomType) {
		this();
		if(pickRandomType) {
			trapType = TrapType.values()[Game.rand.nextInt(TrapType.values().length)];
		}
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		super.tick(level, delta);
		ticks += delta;
		
		if(resetTimer > 0) resetTimer -= delta;
		else { triggered = false; set = true; }
		
		color = set ? Colors.MUNDANE : Colors.MAGIC;
		fullbrite = !set;
		
		if(ticks > checkSpeed) {
			ticks = 0;
			
			isStandingOn = false;
			playerTriggered = false;
			
			for(Entity e : level.entities) {
				if((e instanceof Item || e instanceof Actor || e instanceof Projectile) && Math.abs(e.getX() - x) < 0.5f + e.collision.x && Math.abs(e.getY() - y) < 0.5f + e.collision.y && z + 1f > e.z) {
					isStandingOn = true;
				}
			}
			
			Player p = Game.instance.player;
			if(!isStandingOn && Math.abs(p.getX() - x) < 0.5f + p.collision.x && Math.abs(p.getY() - y) < 0.5f + p.collision.y && z + 1f > p.z) {
				isStandingOn = true;
				playerTriggered = true;
			}
			
			if(isStandingOn && !triggered) triggered = true;
			if(isStandingOn) resetTimer = 200;
			
			if(triggered && set) {
				if(triggerTimer == 0) Audio.playPositionedSound("trap_activate.mp3", new Vector3(x,y,z), 0.15f, 12);
				triggerTimer += delta;
			}
			
			if(triggered && set && triggerTimer > triggerDelay) {
				set = false;
				triggerTimer = 0;
				triggered = false;
				resetTimer = 200;
				
				if(trapType == TrapType.explosion) {
					SplashExplosion trap = new SplashExplosion(DamageType.FIRE, 6 + (int)(level.dungeonLevel * 0.5));
					trap.physicsForce = 0.1f;
					trap.doCast(new Vector3((float)x,(float)y,(float)z), Vector3.Zero);
				}
				else if(trapType == TrapType.teleport) {
					Teleport trap = new Teleport();
					trap.doCast(new Vector3((float)x,(float)y,(float)z), Vector3.Zero);
				}
				else if(trapType == TrapType.poison) {
					SplashExplosion trap = new SplashExplosion(DamageType.POISON, 2);
					trap.physicsForce = 0.1f;
					trap.doCast(new Vector3((float)x,(float)y,(float)z), Vector3.Zero);
				}
				
				if(playerTriggered) {
					p.history.activatedTrap(this);
				}
			}
		}
	}
	
	public void updateDrawable() {
		if(drawable != null) {
			drawable.update(this);
			DrawableSprite drbls = (DrawableSprite)drawable;
		}
		else if(artType != ArtType.hidden) {
			DrawableSprite drbls = new DrawableSprite(tex, artType);
			drbls.billboard = false;
			
			drbls.dir.set(Vector3.X).scl(-1f);
			drbls.dir.rotate(Vector3.Z, -90f);
			
			drawable = drbls;
			drawable.update(this);
		}
	}
}
