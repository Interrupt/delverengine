package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.GlRenderer;

public class DynamicLight extends Entity {
	
	public enum LightType { steady, fire, flicker_on, flicker_off, sin_slow, sin_fast, torch, sin_slight };
	
	@EditorProperty
	public Vector3 lightColor = new Vector3(1,1,1);
	
	@EditorProperty
	public float range = 3.2f;
	
	@EditorProperty
	public LightType lightType = LightType.steady;

	@Deprecated
	public boolean hasHalo = false;

	@EditorProperty
	public HaloMode haloMode = HaloMode.NONE;

	@EditorProperty
	public boolean on = true;

	@EditorProperty
	public float toggleAnimationTime = 0f;
	public transient float toggleLerpTime = 1f;

	public float haloOffset = 0.25f;
	public float haloSize = 0.5f;

	@EditorProperty
	public float haloSizeMod = 1f;
	
	private transient float time = 0f;
	public transient Vector3 workColor = new Vector3();
	private transient float workRange = range;
	private Vector3 colorLerpTarget = null;
	private float rangeLerpTarget = 3.2f;
	public float lerpTimer = 0;
	private float lerpTime = 0;
	private Boolean killAfterLerp = null;
	
	private transient Vector3 workVector3 = new Vector3();
	
	public DynamicLight() { hidden = true; spriteAtlas = "editor"; tex = 12; isSolid = false; }
	
	public DynamicLight(float x, float y, float z, Vector3 lightColor) {
		super(x, y, 0, false);
		this.z = z;
		artType = ArtType.hidden;
		this.lightColor = lightColor;
	}
	
	public DynamicLight(float x, float y, float z, float range, Vector3 lightColor) {
		super(x, y, 0, false);
		this.z = z;
		this.range = range;
		artType = ArtType.hidden;
		this.lightColor = lightColor;
	}
	
	public void updateLightColor(float delta) {
		time += delta;
		
		workColor.set(lightColor);
		workRange = range;

		if(toggleAnimationTime != 0) {
			// animate when turning on / off
			if(on && toggleLerpTime < 1)
				toggleLerpTime += delta / toggleAnimationTime;
			else if(!on && toggleLerpTime > 0)
				toggleLerpTime -= delta / toggleAnimationTime;

			// clamp!
			if(toggleLerpTime < 0)
				toggleLerpTime = 0;
			if(toggleLerpTime > 1)
				toggleLerpTime = 1;
		}
		
		if(lightType == LightType.steady) {
			// steady lights do nothing
		}
		else if(lightType == LightType.fire) {
			workColor.scl(1 - (float)Math.sin(time * 0.11f) * 0.1f);
			workColor.scl(1 - (float)Math.sin(time * 0.147f) * 0.1f);
			workColor.scl(1 - (float)Math.sin(time * 0.263f) * 0.1f);
			
			workRange *= 1 - (float)Math.sin(time * 0.111f) * 0.05f;
			workRange *= 1 - (float)Math.sin(time * 0.1477f) * 0.05f;
			workRange *= 1 - (float)Math.sin(time * 0.2631f) * 0.05f;
		}
		else if(lightType == LightType.torch) {
			workColor.scl(1 - (float)Math.sin(time * 0.11f) * 0.5f);
			workColor.scl(1 - (float)Math.sin(time * 0.147f) * 0.5f);
			workColor.scl(1 - (float)Math.sin(time * 0.263f) * 0.5f);
			
			workRange *= 1 - (float)Math.sin(time * 0.111f) * 0.05f;
			workRange *= 1 - (float)Math.sin(time * 0.1477f) * 0.05f;
			workRange *= 1 - (float)Math.sin(time * 0.2631f) * 0.05f;
		}
		else if(lightType == LightType.flicker_on) {
			workColor.scl(Game.rand.nextFloat() > 0.95f ? 1f : 0f);
		}
		else if(lightType == LightType.flicker_off) {
			workColor.scl(Game.rand.nextFloat() > 0.95f ? 0f : 1f);
		}
		else if(lightType == LightType.sin_slow) {
			workColor.scl((float)Math.sin(time * 0.02f) + 1f);
		}
		else if(lightType == LightType.sin_slight) {
			workColor.scl((float)(Math.sin(time * 0.05f) + 1f) * 0.2f + 1f);
		}
		else if(lightType == LightType.sin_fast) {
			workColor.scl((float)Math.sin(time * 0.2f) + 1f);
		}

		if(toggleLerpTime > 0 && toggleLerpTime < 1) {
			workColor.scl(Interpolation.linear.apply(toggleLerpTime));
		}
		
		if(colorLerpTarget != null) {
			float lerpA = lerpTimer / lerpTime;
			workColor.lerp(colorLerpTarget, lerpA);
			workRange = Interpolation.linear.apply(range, rangeLerpTarget, lerpA);
			lerpTimer += delta;
			
			if(lerpTimer >= lerpTime) {
				workColor.set(colorLerpTarget);
				
				colorLerpTarget = null;
				
				if(killAfterLerp != null && killAfterLerp) isActive = false;
			}
		}

		if(Float.isNaN(workRange)) workRange = 0;

		haloSize = workRange * 0.175f;
		haloSize *= Interpolation.circleOut.apply(workColor.len() * 0.4f);
		haloSize *= haloSizeMod;
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		if (!GameManager.renderer.enableLighting) {
			return;
		}

		updateLightColor(delta);
		
		if(isActive && (on || (toggleLerpTime > 0 && toggleLerpTime < 1))) {
			if(Game.instance.camera == null || Game.instance.camera.frustum.sphereInFrustum(workVector3.set(x,z,y), range * 1.5f)) {
				com.interrupt.dungeoneer.gfx.DynamicLight light = GlRenderer.getLight();
				if(light != null) {
					light.color.set(workColor.x, workColor.y, workColor.z);
					light.position.set(x, z, y);
					light.range = workRange;
				}
			}
		}
	}

	@Override
	public void editorTick(Level level, float delta) {
		super.editorTick(level, delta);
		tick(level, delta);
	}
	
	public DynamicLight startLerp(Vector3 endColor, float time, boolean killAfter)
	{
		colorLerpTarget = endColor;
		rangeLerpTarget = range;
		killAfterLerp = killAfter;
		
		lerpTime = time;
		lerpTimer = 0f;
		
		return this;
	}
	
	public DynamicLight startLerp(Vector3 endColor, float endRange, float time, boolean killAfter)
	{
		colorLerpTarget = endColor;
		rangeLerpTarget = endRange;
		killAfterLerp = killAfter;
		
		lerpTime = time;
		lerpTimer = 0f;
		
		return this;
	}

	public DynamicLight setHaloMode(HaloMode haloMode) {
		this.haloMode = haloMode;
		return this;
	}
	
	@Override
	public void init(Level level, Source source) {
		super.init(level, source);
		time = Game.rand.nextFloat() * 5000f;

		toggleLerpTime = on ? 1 : 0;
	}

	@Override
	public void onTrigger(Entity instigator, String value) {
		on = !on;
	}

	@Override
	public HaloMode getHaloMode() {
		return haloMode;
	}
}
