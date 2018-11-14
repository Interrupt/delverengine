package com.interrupt.dungeoneer.entities.projectiles;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Explosion;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.drawables.Drawable;
import com.interrupt.dungeoneer.gfx.drawables.DrawableBeam;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class BeamProjectile extends Projectile {
	
	private float trailTimer = 0;
	
	public float length = 10;
	
	public Vector3 startPos = new Vector3();
	
	public DamageType damageType;

	public int startTex = 14;
	public int endTex = 15;
	public float animateTime = 3f;
	public float animateTimer = 0f;

	public Explosion explosion = null;
	
	public BeamProjectile() { collision.set(0.05f, 0.05f, 0.1f); canStepUpOn = false; dropSound = ""; }

	private transient DynamicLight light = null;
	
	public BeamProjectile(float x, float y, float z, float xa, float ya, float za, int damage, DamageType damageType, Color color, Entity owner) {
		super(x, y, z, 10, xa, ya, damage, damageType, owner);
		this.z = z + 0.1f;
		this.za = za;

		length = 10;
		
		floating = true;
		fullbrite = true;
		this.color = color;
		
		this.damageType = damageType;
		
		startPos.set(x, y, z);
		
		drawable = new DrawableBeam(15, ArtType.sprite);
		((DrawableBeam)drawable).beamRenderMode = DrawableBeam.BeamRenderModes.LINE;
		
		// set rotation
		if(drawable instanceof DrawableBeam) {
			DrawableBeam drblb = (DrawableBeam)drawable;
			drblb.size = 1f;
		}
		drawable.dir.set(xa, za, ya).nor();
		drawable.color = this.color;
		
		this.tex = startTex;
		
		yOffset = 0.03f;
		
		destroyDelay = 30f;
		destroyOnEntityHit = false;
		
		collision.set(0.05f, 0.05f, 0.1f);

		canStepUpOn = false;
	}
	
	@Override
	public void onTick(float delta) {

		if(light == null) {
			light = new DynamicLight();
			light.z = -0.2f;
			light.haloMode = HaloMode.BOTH;
			attach(light);
		}

		if(light != null) {
			light.lightColor.set(color.r,color.g,color.b);
		}

		animateTimer -= delta;
		if(animateTimer <= 0) {
			animateTimer = animateTime;
			if(endTex - startTex != 0)
				tex = Game.rand.nextInt(endTex - startTex + 1) + startTex;
			else
				tex = startTex;
		}
		
		if(drawable instanceof DrawableBeam) {
			DrawableBeam drblb = (DrawableBeam)drawable;
			drblb.size = startPos.cpy().add( -x, -y, -z).len();
			drblb.size = Math.min(drblb.size, length);
			drblb.rot += ((Game.rand.nextFloat() * 200) - 100) * delta;
		}
		
		if(destroyTimer == null) {
			if(trailTimer > 0) trailTimer -= delta;
			else {
				trailTimer = 0.2f;
				float driftSpeed = 0.001f;
				float driftSpeedHalf = driftSpeed / 2f;
				Particle p = CachePools.getParticle(x - xa, y - ya, z + 0.5f - za + yOffset, (Game.rand.nextFloat() * driftSpeed) - driftSpeedHalf, (Game.rand.nextFloat() * driftSpeed) - driftSpeedHalf, (Game.rand.nextFloat() * driftSpeed) - driftSpeedHalf, 0, color, fullbrite);
				p.floating = true;
				p.lifetime = (int)(150 * Game.rand.nextFloat()) + 250;
				p.startScale = 1f;
				p.endScale = 0f;
				Game.GetLevel().SpawnNonCollidingEntity( p ) ;
			}
		} else {
			float fadeScale = destroyTimer / destroyDelay;
			
			if(light != null) {
				light.lightColor.x = 1.5f * color.r * fadeScale;
				light.lightColor.y = 1.5f * color.g * fadeScale;
				light.lightColor.z = 1.5f * color.b * fadeScale;
			}
			
			if(drawable instanceof DrawableBeam) {
				DrawableBeam drblb = (DrawableBeam)drawable;
				drblb.size *= fadeScale;
			}
		}
			
		Vector3 dir2 = new Vector3(xa, za, ya).nor();
		drawable.dir = dir2;
	}
	
	@Override
	public void hitEffect()	{
		if(!isActive || destroyTimer != null) return;

		makeHitDecal();

		if(explosion != null) {
			explosion.x = x;
			explosion.y = y;
			explosion.z = z + yOffset;

			if (this.explosion.color == null) {
				explosion.color = color;
			}
			explosion.color.a = 1f;

			explosion.explodeSound = hitSound;
			explosion.explode(Game.GetLevel(), 1f);
			return;
		}
		
		Level level = Game.GetLevel();
		Random r = new Random();
		int particleCount = 12;
		particleCount *= Options.instance.gfxQuality;

		int detailLevel = Options.instance.graphicsDetailLevel;
		
		for(int i = 0; i < particleCount; i++)
		{
			Particle p = CachePools.getParticle(x, y, z + 0.5f + yOffset, r.nextFloat() * 0.02f - 0.01f, r.nextFloat() * 0.02f - 0.01f, r.nextFloat() * 0.03f - 0.01f, 260 + r.nextInt(500), 1f, 0f, 0, color, fullbrite);

			// add a glow on high graphics
			if(detailLevel > 3) p.haloMode = HaloMode.CORONA_ONLY;

			level.SpawnNonCollidingEntity(p) ;
		}

		// dust effects!
		for(int i = 0; i < particleCount / 2; i++) {
			Particle p = CachePools.getParticle(x, y, z + yOffset, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.01f - 0.005f, 40 + r.nextInt(20), 1f, 0.2f, Game.rand.nextInt(3), color, fullbrite);
			p.floating = true;
			p.checkCollision = false;
			p.spriteAtlas = "dust_puffs";
			p.shader = fullbrite ? "dust-fullbrite" : "dust";
			p.endColor = new Color(p.color);
			p.endColor.a = 0;
			level.SpawnNonCollidingEntity(p) ;
		}
	}
	
	@Override
	public void hit(float xa, float ya, int damage, float force, DamageType damageType, Entity instigator) {
		if(damageType != DamageType.PHYSICAL) {
			Particle ring = new Particle(x, y, z, 0, 0, 0, 0, color, true);
			((DrawableSprite)ring.drawable).billboard = false;
			((DrawableSprite)ring.drawable).dir.set(xa, za, ya).nor();
			ring.xa = 0;
			ring.ya = 0;
			ring.za = 0;
			ring.artType = ArtType.particle;
			ring.tex = 16;
			ring.x = x;
			ring.y = y;
			ring.z = z;
			ring.lifetime = 20;
			ring.startScale = 0.1f;
			ring.fullbrite = true;
			ring.endScale = 8f;
			ring.scale = 0f;
			ring.floating = true;
			ring.yOffset = yOffset;
			ring.checkCollision = false;
			ring.color.set(color);
			ring.isActive = true;
			ring.initialized = false;
			ring.isDynamic = true;
			Game.instance.level.non_collidable_entities.add(ring);
		}
		
		super.hit(xa, ya, damage, force, damageType, instigator);
	}
	
	@Override
	public void onDestroy() { }
}
