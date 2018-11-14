package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.serializers.hacks.BoundingBoxOld;

public class ParticleEmitter extends Entity {
	public ParticleEmitter() { hidden = true; spriteAtlas = "editor"; tex = 14; isSolid = false; }
	
	public enum StartMode { IMMEDIATELY, DELAYED }
 	
	@EditorProperty
	public StartMode startMode = StartMode.IMMEDIATELY;
	
	@EditorProperty( group="Particle Appearance" )
	public int particleStartTex = 0;
	
	@EditorProperty( group="Particle Appearance" )
	public int particleEndTex = 0;

	public ArtType particleArtType = ArtType.particle;

	@EditorProperty( group="Particle Appearance" )
	public String particleAtlas = "particle";

	@EditorProperty( group="Particle Appearance" )
	public boolean particleShadows = false;

	@Deprecated
	public boolean particleHalos = false;

	@EditorProperty( group="Particle Appearance" )
	public HaloMode particleHalo = HaloMode.NONE;
	
	@EditorProperty( group="Particle Spawning" )
	public float particleLifetime = 20f;
	
	@EditorProperty( group="Particle Physics" )
	public boolean particlesCollide = true;
	
	@EditorProperty( group="Particle Spawning" )
	public Vector3 particleSpread = new Vector3(0.1f, 0.1f, 0.1f);
	
	@EditorProperty( group="Particle Physics" )
	public Vector3 particleCollisionSize = new Vector3(0.05f, 0.05f, 0.05f);
	
	@EditorProperty( group="Particle Spawning" )
	public Vector3 particleVelocity = new Vector3();

	@EditorProperty( group="Particle Spawning" )
	public float particleMaxVelocity = 10f;

	@EditorProperty( group="Particle Spawning" )
	public float particleDampenAmount = 0.9f;
	
	@EditorProperty( group="Particle Variance" )
	public Vector3 particleRandomVelocity = new Vector3(0.1f, 0.1f, 0.1f);

    @EditorProperty( group="Particle Variance" )
    public int particleRandomSpawnCount = 0;

	@EditorProperty( group="Particle Rotation" )
	public float particlesRotateByVariance = 0;

	@EditorProperty( group="Particle Rotation" )
	public float particlesMovementRotateAmount = 0;
	
	@EditorProperty( group="Particle Physics" )
	public boolean particlesFloat = false;

	@EditorProperty( group="Particle Physics" )
	public float turbulenceAmount = 0;

	@EditorProperty( group="Particle Physics" )
	public float turbulenceMoveModifier = 0;
	
	@EditorProperty( group="Particle Appearance" )
	public float particleStartScale = 1;
	
	@EditorProperty( group="Particle Appearance" )
	public float particleEndScale = 1;
	
	@EditorProperty( group="Particle Appearance" )
	public boolean particlesFullbrite = false;

	@EditorProperty( group="Particle Rotation" )
	public float particlesRotateBy = 0f;
	
	@EditorProperty( group="Particle Spawning" )
	public int particleSpawnCount = 2;
	
	@EditorProperty( group="Particle Spawning" )
	public float particleSpawnInterval = 15f;
	
	@EditorProperty( group="Particle Variance" )
	public float particleRandomSpawnInterval = 0f;
	
	@EditorProperty( group="Particle Variance" )
	public float particleRandomLifetime = 0f;
	
	@EditorProperty( group="Particle Spawning" )
	public boolean particlesRepeat = true;

	@EditorProperty( group="Particle Spawning" )
	public boolean particlesMoveRelativeToParent = true;
	
	@EditorProperty( group="Particle Appearance" )
	public Color particleColor = new Color(1, 1, 1, 1);

	@EditorProperty( group="Particle Appearance" )
	public Color particleEndColor = null;
	
	@EditorProperty( group="Particle Spawning" )
	public boolean particlesPersist = false;
	
	@EditorProperty( group="Particle Spawning" )
	public float spawnDistance = 40f;

	@EditorProperty( group="Particle Spawning" )
	public boolean pickRandomSprite = false;
	
	private transient float spawntimer = 0f;
	
	private transient Vector3 position = new Vector3();
	private transient Vector3 playerPosition = new Vector3();
	private transient float playerDistance = 0f;
	
	private BoundingBoxOld visibleArea = null;
	private float maxVisibleRadius = 1f;
	
	@Override
	public void tick(Level level, float delta)
	{	
		if(!checkDetailLevel()) return;
		if(startMode != StartMode.IMMEDIATELY) return;

		if(particleAtlas == null) {
			particleAtlas = particleArtType.toString();
		}
		
		if(Game.instance != null) {
			PerspectiveCamera camera = GameManager.renderer.camera;
			playerPosition.set(camera.position.x, camera.position.z, camera.position.y);
			position.set(x, y, z);
			
			playerDistance = position.sub(playerPosition).len();
			
			if(playerDistance > spawnDistance || !Game.camera.frustum.sphereInFrustum(position.set(x,z,y), maxVisibleRadius)) {
				return;
			}
		}
		else {
			playerDistance = 1f;
		}
		
		spawntimer += delta * (1f - Math.min(playerDistance / spawnDistance, 1f));
		
		if(spawntimer >= particleSpawnInterval) {
			spawntimer = Game.rand.nextFloat() * particleRandomSpawnInterval;

            int rand = 0;
            if(particleRandomSpawnCount > 0) rand = Game.rand.nextInt(particleRandomSpawnCount + 1);
			
			for(int i = 0; i < particleSpawnCount + rand; i++) {
				Particle p = CachePools.getParticle();
				p.artType = particleArtType;
				p.checkCollision = particlesCollide;
				p.floating = particlesFloat;
				p.fullbrite = particlesFullbrite;
				p.color.set(particleColor);
				p.lifetime = particleLifetime + (Game.rand.nextFloat() * particleRandomLifetime);
				p.tex = particleStartTex;
				p.rotateAmount = particlesRotateBy + (Game.rand.nextFloat() - 0.5f) * particlesRotateByVariance;
				p.movementRotateAmount = particlesMovementRotateAmount * (Game.rand.nextFloat() - 0.5f);
				p.maxVelocity = particleMaxVelocity;
				p.dampenAmount = particleDampenAmount;
				p.haloMode = particleHalo;
				p.shader = shader;
				p.spriteAtlas = particleAtlas;
				p.blendMode = blendMode;

				if(particleShadows) p.shadowType = ShadowType.BLOB;

				if(particleEndColor != null) {
					p.endColor = particleEndColor;
				}
				
				p.collision.set(particleCollisionSize);
				
				p.x = x + Game.rand.nextFloat() * particleSpread.x - particleSpread.x * 0.5f;
				p.y = y + Game.rand.nextFloat() * particleSpread.y - particleSpread.y * 0.5f;
				p.z = z + Game.rand.nextFloat() * particleSpread.z - particleSpread.z * 0.5f;
				
				p.xa = particleVelocity.x + Game.rand.nextFloat() * particleRandomVelocity.x - particleRandomVelocity.x * 0.5f;
				p.ya = particleVelocity.y + Game.rand.nextFloat() * particleRandomVelocity.y - particleRandomVelocity.y * 0.5f;
				p.za = particleVelocity.z + Game.rand.nextFloat() * particleRandomVelocity.z - particleRandomVelocity.z * 0.5f;
				
				if(owner != null && this.particlesMoveRelativeToParent) {
					p.xa += owner.xa;
					p.ya += owner.ya;
					p.za += owner.za;
				}
				
				p.persists = particlesPersist;
				
				p.scale = particleStartScale;
				p.endScale = particleEndScale;

				p.turbulenceAmount = turbulenceAmount;
				p.turbulenceMoveModifier = turbulenceMoveModifier;
				p.drawDistance = drawDistance;

				if(pickRandomSprite && particleEndTex > particleStartTex) {
					p.tex = particleStartTex + Game.rand.nextInt(Math.abs(particleEndTex - particleStartTex) + 1);
				}
				else if(particleStartTex != particleEndTex) {
					p.playAnimation(particleStartTex, particleEndTex, particleLifetime - 0.0001f);
				}
				
				level.SpawnNonCollidingEntity(p);
			}
			
			if(!particlesRepeat) startMode = StartMode.DELAYED;
		}
	}
	
	@Override
	public void editorTick(Level level, float delta) {
		// let the emitter make particles in the editor, just don't save them ever
		Boolean persistValue = particlesPersist;
		
		particlesPersist = false;
		tick(level, delta);
		
		particlesPersist = persistValue;
	}
	
	@Override
	public void init(Level level, Source source) {
		maxVisibleRadius = Math.max(particleSpread.x,Math.max(particleSpread.y,particleSpread.z));
		if(maxVisibleRadius < 0.4f) maxVisibleRadius = 0.4f;
		spawntimer = Game.rand.nextFloat() * particleSpawnInterval;
	}
	
	@Override
	public void rotate90() {
		if(particleVelocity != null) {
			float swap = particleVelocity.x;
			particleVelocity.x = particleVelocity.y;
			particleVelocity.y = swap;
		}
		
		if(particleRandomVelocity != null) {
			float swap = particleRandomVelocity.x;
			particleRandomVelocity.x = particleRandomVelocity.y;
			particleRandomVelocity.y = swap;
		}
		
		if(particleSpread != null) {
			float swap = particleSpread.x;
			particleSpread.x = particleSpread.y;
			particleSpread.y = swap;
		}
	}
	
	@Override
	public void onTrigger(Entity instigator, String value) {
		if(startMode == StartMode.IMMEDIATELY) {
			startMode = StartMode.DELAYED;
		}
		else startMode = StartMode.IMMEDIATELY;
	}
}
