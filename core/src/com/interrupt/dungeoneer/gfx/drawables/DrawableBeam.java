package com.interrupt.dungeoneer.gfx.drawables;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class DrawableBeam extends Drawable {
	public int tex = 0;
	public float rot = 0;
	public float yOffset = 0;
	public float size = 4;
	public float centerOffset = 1f;
	public float beamCrossOffset = 0f;
	public String spriteTex = null;
	
	public transient TextureAtlas atlas = null;

	public enum BeamRenderModes { CROSS, LINE }
	public BeamRenderModes beamRenderMode = BeamRenderModes.CROSS;
	
	public DrawableBeam() { }
	
	public DrawableBeam(int tex, ArtType artType) {
		this.tex = tex;
		this.artType = artType;
	}
	
	public DrawableBeam(int tex, ArtType artType, float rot) {
		this.tex = tex;
		this.artType = artType;
		this.rot = rot;
	}
	
	public void update(Entity e) {
		tex = e.tex;
		drawOffset.z = e.yOffset;
		fullbrite = e.fullbrite;
		color = e.color;
		scale = e.scale;
		rot = e.roll;
		artType = e.artType;
		yOffset = e.yOffset;

		shader = e.getShader();
		blendMode = e.blendMode;

		if(isDirty) {
			spriteTex = e.spriteAtlas;
			artType = e.artType;
			refreshTextureAtlas();
			isDirty = false;
		}
	}

	public void refreshTextureAtlas() {
		if(spriteTex != null) {
			atlas = TextureAtlas.getCachedRegion(spriteTex);
			if(atlas == null) atlas = TextureAtlas.getCachedRegion(artType.toString());
		}
		else if(artType != null) atlas = TextureAtlas.getCachedRegion(artType.toString());
	}
}
