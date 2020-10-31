package com.interrupt.dungeoneer.gfx.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class DrawableSprite extends Drawable {
	public int tex = 0;
	public float rot = 0;
	public float yOffset = 0;
	public boolean billboard = true;
	public Vector2 transformationOffset = null;
	public boolean isStatic = false;
	public String spriteTex = null;
	public Float cameraPull = null;
	public boolean isFogSprite = false;
	public transient Color colorLastFrame = null;
	public boolean scaleWithOffsets = true;
	
	public Vector3 rotation = Vector3.Zero;
	public float xScale = 1;

    public DrawableSprite() { }
	
	public transient TextureAtlas atlas = null;
	
	public DrawableSprite(int tex, ArtType artType) {
		this.tex = tex;
		this.artType = artType;
	}

	public DrawableSprite(int tex, ArtType artType, boolean scaleWithOffsets) {
		this.tex = tex;
		this.artType = artType;
		this.scaleWithOffsets = scaleWithOffsets;
	}
	
	public void update(Entity e) {
		tex = e.tex;
		drawOffset.z = e.yOffset;
		fullbrite = e.fullbrite;
		color = e.color;
		scale = e.scale;
		rot = e.roll;

		shader = e.getShader();

		if(shader == null || shader.equals("")) {
			if(fullbrite) {
				shader = "sprite-fullbrite";
			}
		}

		blendMode = e.blendMode;
		
		if(isDirty) {
			spriteTex = e.spriteAtlas;
			artType = e.artType;
			isStatic = !e.isDynamic;
			refreshTextureAtlas();
			isDirty = false;
		}
	}
	
	public void refreshTextureAtlas() {
		if(spriteTex != null) {
			atlas = TextureAtlas.getCachedRegion(spriteTex);
			if(atlas == null && artType != null) atlas = TextureAtlas.getCachedRegion(artType.toString());
		}
		else if(artType != null) atlas = TextureAtlas.getCachedRegion(artType.toString());
	}
}
