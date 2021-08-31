package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.noise.PerlinNoise;

import java.util.Random;

public class Particle extends Sprite {
	public float lifetime = 82;
	public float starttime = 0;
	public boolean initialized = true;
	
	public boolean checkCollision = true;
	
	public float startScale = 1;
	public float endScale = 1;
	public transient float timeModifier = 0;

	public Color startColor;
	public Color endColor;
	
	public transient Interpolation interpolation = Interpolation.circleIn;

	public float rotateAmount = 0f;
	public float movementRotateAmount = 0f;

	public float maxVelocity = 10.0f;
	public float dampenAmount = 0.9f;

	private static transient Random r = new Random();

	private static transient PerlinNoise perlinNoise = new PerlinNoise(1, 1f, 2f, 1f, 1);

	public float turbulenceAmount = 0f;
	public float turbulenceMoveModifier = 0f;

	private transient Vector2 randX = new Vector2(Game.rand.nextFloat() * 50f, Game.rand.nextFloat() * 50);
	private transient Vector2 randY = new Vector2(Game.rand.nextFloat() * 50f, Game.rand.nextFloat() * 50);

	@Deprecated
	public boolean hasHalo = false;

	public HaloMode haloMode = HaloMode.NONE;

	public Particle() {
		color = new Color(Color.WHITE);
		artType = ArtType.particle;
		collidesWith = CollidesWith.staticOnly;

		// Create a drawable sprite that scales from the center
		drawable = new DrawableSprite(tex, artType, false);
	}
	
	public Particle(float x, float y, float z, float xv, float yv, float zv, int tex, Color c, boolean fullbrite)
	{
		this.x = x;
		this.y = y;
		this.z = z;

		this.tex = tex;
		artType = ArtType.particle;
		
		this.xa = xv * 2;
		this.ya = yv * 2;
		this.za = zv * 2;

		lifetime = 20 + r.nextInt(160);
		
		color = new Color(c);
		
		isSolid = false;
		this.fullbrite = fullbrite;

		// Create a drawable sprite that scales from the center
		drawable = new DrawableSprite(tex, artType, false);
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		if(!initialized) {
			starttime = lifetime;
			startScale = scale;

			if(endColor != null)
				startColor = new Color(color);

			initialized = true;
		}
		
		if(scale != endScale) {
			if(drawable != null) {
				timeModifier = 1 - (lifetime / starttime);
				timeModifier = interpolation.apply(timeModifier);
				scale =  endScale * timeModifier + startScale * (1 - timeModifier);
			}
		}

		if(startColor != null && endColor != null) {
			timeModifier = 1 - (lifetime / starttime);
			timeModifier = Interpolation.pow2.apply(timeModifier);
			color.set(startColor);
			color.lerp(endColor, timeModifier);
		}
		
		lifetime -= delta;
		if(lifetime < 0)
		{
			isActive = false;
            this.attached = null;
			return;
		}

		roll += rotateAmount * delta;

		Vector3 speed = CachePools.getVector3(xa, za, ya);
		if (speed.len() > this.maxVelocity) {

			// scale friction based on delta between frames
			speed.scl((float)Math.pow(dampenAmount, delta));

			this.xa = speed.x;
			this.ya = speed.z;
			this.za = speed.y;
		}
		CachePools.freeVector3(speed);
		
		if(checkCollision) {
			if(!physicsSleeping) {
				float nextx = x + (xa * delta) * 2f;
				float nexty = y + (ya * delta) * 2f;
				
				if (level.isFree(nextx, y, z, collision, 0.01f, floating, null)) {
					Entity colliding = level.checkEntityCollision(nextx, y, z, collision.x, collision.y, collision.z, this);
					if(colliding == null)
						x += (xa * delta) * 2f;
					else
					{
						xa *= -0.12;
						ya *= 0.9;
					}
				}
				else
				{
					xa *= -0.12;
					ya *= 0.9;
				}
				
				if (level.isFree(x, nexty, z, collision, 0.01f, floating, null)) {
					Entity colliding = level.checkEntityCollision(x, nexty, z, collision.x, collision.y, collision.z, this);
					if(colliding == null)
						y += (ya * delta) * 2f;
					else {
						xa *= 0.9;
						ya *= -0.12;
					}
				}
				else
				{
					xa *= 0.9;
					ya *= -0.12;
				}
				
				float floorHeight = level.maxFloorHeight(x, y, z, 0);
				float ceilHeight = level.minCeilHeight(x, y, z, 0) + 0.485f;
				boolean onFloor = z <= floorHeight + 0.5f;
				
				Entity onEntity = level.checkEntityCollision(x, y, (z + za), collision.x, collision.y, collision.z, this);
				if(!onFloor) onFloor = onEntity != null && za <= 0;
				
				// ceiling collision
				if(z > ceilHeight)
				{
					z = ceilHeight;
					za = 0;
				}
				else if((za > 0 && onEntity != null))
				{
					za = 0;
				}
				
				if(onFloor)
				{
					// floor friction
					xa -= (xa - (xa * 0.8)) * delta;
					ya -= (ya - (ya * 0.8)) * delta;
				}
				else
				{
					xa -= (xa - (xa * 0.98)) * delta;
					ya -= (ya - (ya * 0.98)) * delta;
				}
				
				if(!onFloor && !floating) za -= 0.0035f * delta; // falling; add gravity
				else if(onFloor)
				{
					if(bounces)
					{
						if(Math.abs(za) < 0.01) za = 0;
						else za = -za * 0.2f;
					} else za = 0;
					
					if(onEntity == null) {
						if(z > floorHeight + 0.5f - 0.08f)
						z = floorHeight + 0.5f;
					}
					else if (onEntity != null && z - onEntity.z + onEntity.collision.z < stepHeight)
						z = onEntity.z + onEntity.collision.z;
					
					// sleep to save collision cycles if not moving very fast
					if(onFloor && (onEntity == null || !onEntity.isDynamic) && Math.abs(xa) < 0.0001f && Math.abs(ya) < 0.0001f && Math.abs(za) < 0.0001f) physicsSleeping = true;
				}
				
				isOnFloor = onFloor;
				
				//x += xa * delta;
				//y += ya * delta;
				z += za * delta;
			}
			else {
				if(Math.abs(xa) > 0.0001f || Math.abs(ya) > 0.0001f || Math.abs(za) > 0.0001f) physicsSleeping = false;
			}
			
		} else {
			if(!floating)
				za -= 0.0035f;
			
			x += xa * delta;
			y += ya * delta;
			z += za * delta;

			if(floating && turbulenceAmount > 0 && turbulenceMoveModifier > 0) {
				float turbulenceDelta = delta * 1.5f;
				x += (perlinNoise.getHeight(randX.x, randX.y + lifetime * turbulenceAmount)) * turbulenceMoveModifier * turbulenceDelta;
				y += (perlinNoise.getHeight(randX.x + lifetime * turbulenceAmount, randY.y)) * turbulenceMoveModifier * turbulenceDelta;
			}
		}

		if(movementRotateAmount != 0) {
			roll += (movementRotateAmount * getMovementRollSpeed()) * delta;
		}
		
		if(animation != null && animation.playing) {
			animation.animate(delta, this);
		}

		tickAttached(level, delta);
	}

	public float getMovementRollSpeed() {
		return ((xa + ya + za) / 3f) * 100f;
	}
	
	public void editorTick(Level level, float delta) {
		super.editorTick(level, delta);
		tick(level, delta);
	}
	
	public void playAnimation(int startTex, int endTex, float speed) {
		this.playAnimation(startTex, endTex, speed, false);
	}

	public void playAnimation(int startTex, int endTex, float speed, boolean looping) {
		SpriteAnimation animation = new SpriteAnimation(startTex, endTex, speed, null);
		this.playAnimation(animation, looping);

		if (!looping) {
			lifetime = speed;
		}
	}
	
	@Override
	public void init(Level level, Source source) {
		if(attached != null) {
			for(int i = 0; i < attached.size; i++) {
				attached.get(i).init(level, source);
			}
		}
	}

	@Override
	public HaloMode getHaloMode() {
		return haloMode;
	}
}
