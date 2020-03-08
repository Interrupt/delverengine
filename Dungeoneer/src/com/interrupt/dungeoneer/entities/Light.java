package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.LongMap;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;

public class Light extends Entity {
	/** Color of Light. */
	@EditorProperty
	public Color lightColor = new Color(Color.WHITE);

	/** Range of Light. */
	@EditorProperty
	public float range = 3.2f;

	/** Is Light infinitely tall? */
	public boolean lightFullHeight = false;

	/** Is Light inverted? */
	@EditorProperty
	public boolean invertLight = false;

	@EditorProperty
	public boolean shadowTiles = true;

	/** Has halo effect? */
	@Deprecated
	public boolean hasHalo = false;

	/** Sprite to use for corona effect. */
	@EditorProperty
	public Material corona = null;

	/** Halo vertical offset. */
	public float haloOffset = 0.25f;

	/** Scale of halo effect. */
	@EditorProperty
	public float haloSize = 0.5f;

	/** Is halo animated? */
	@EditorProperty
	public boolean animateHalo = true;

	@EditorProperty
	public HaloMode haloMode = HaloMode.NONE;

	/** Is Light on? */
	@EditorProperty
	public boolean on = true;
	
	public Light() { hidden = true; spriteAtlas = "editor"; tex = 12; isSolid = false; }
	
	private transient LongMap<Boolean> canSeeCache = new LongMap<Boolean>();
	private transient LongMap<Float> canSeeHowMuchCache = new LongMap<Float>();
	private transient LongMap<Color> colorVoxels = new LongMap<Color>();
	
	public Light(float x, float y, Color lightColor, float range) {
		super(x, y, 0, false);
		artType = ArtType.hidden;
		this.lightColor = lightColor;
		this.range = range;
	}
	
	public Light(float x, float y, float z, Color lightColor, float range) {
		super(x, y, 0, false);
		this.z = z;
		artType = ArtType.hidden;
		this.lightColor = lightColor;
		this.range = range;
	}
	
	public Light(float x, float y, float z, Color lightColor, float range, boolean fullHeight) {
		super(x, y, 0, false);
		this.z = z;
		artType = ArtType.hidden;
		this.lightColor = lightColor;
		this.range = range;
		this.lightFullHeight = fullHeight;
	}

	public Light(float x, float y, float z, Color lightColor, float range, boolean fullHeight, boolean castShadows) {
		super(x, y, 0, false);
		this.z = z;
		artType = ArtType.hidden;
		this.lightColor = lightColor;
		this.range = range;
		this.lightFullHeight = fullHeight;
		this.shadowTiles = castShadows;
	}
	
	@Override
	public void tick(Level level, float delta)
	{
	}
	
	public boolean canSee(Level level, float x, float y, float z) {
		if(shadowTiles) {
			long key = getLightVoxelKey(x, y);

			if (canSeeCache.containsKey(key)) {
				return canSeeCache.get(key);
			}

			float canSeeHowMuch = level.canSeeHowMuch(this.x, this.y, x, y);
			boolean canSee = level.canSee(this.x, this.y, x, y);
			canSeeCache.put(key, canSee);
			canSeeHowMuchCache.put(key, canSeeHowMuch);

			return canSee;
		}

		return true;
	}

	public float canSeeHowMuch(Level level, float x, float y, float z) {
		if(shadowTiles) {
			long key = getLightVoxelKey(x, y);

			if (canSeeHowMuchCache.containsKey(key)) {
				return canSeeHowMuchCache.get(key);
			}

			float canSeeHowMuch = level.canSeeHowMuch(this.x, this.y, x, y);
			boolean canSee = canSeeHowMuch >= 1f;
			canSeeCache.put(key, canSee);
			canSeeHowMuchCache.put(key, canSeeHowMuch);

			return canSeeHowMuch;
		}

		return 1f;
	}
	
	public void clearCanSee() {
		canSeeCache.clear();
		canSeeHowMuchCache.clear();
		colorVoxels.clear();
	}

	public void cacheLightColor(float x, float y, float z, Color color) {
		long key = getLightVoxelKey(x, y, z);
		colorVoxels.put(key, new Color(color));
	}

	public Color getCachedLightColor(float x, float y, float z) {
		return colorVoxels.get(getLightVoxelKey(x, y, z));
	}

	public Color getColor() {
		if(on)
			return lightColor;
		else
			return Color.BLACK;
	}

	public long getLightVoxelKey(float x, float y) {
		x = (float)(Math.floor(x * 8));
		y = (float)(Math.floor(y * 8));
		return (long)(x + (y * 3000));
	}

	public long getLightVoxelKey(float x, float y, float z) {
		x = (float)(Math.floor(x * 8));
		y = (float)(Math.floor(y * 8));
		z = (float)(Math.floor(z * 8));
		return (long)(x + (y * 3000) + (z * 9000000));
	}

	@Override
	public HaloMode getHaloMode() { return haloMode; }

	@Override
	public void onTrigger(Entity instigator, String value) {
		on = !on;
		GameManager.renderer.refreshChunksNear(x, y, range * 2f);
	}
}
