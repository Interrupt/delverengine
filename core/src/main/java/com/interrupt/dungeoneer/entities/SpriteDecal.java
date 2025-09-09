package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class SpriteDecal extends DirectionalEntity {
	
	public SpriteDecal() { yOffset = -0.5f; }
	
	public SpriteDecal(float x, float y, int tex)
	{
		super(x, y, tex, false);
		artType = ArtType.sprite;
		
		drawable = null;
		
		isSolid = false;
	}
	
	public SpriteDecal(float x, float y, float z, int tex, Vector3 dir)
	{
		super(x, y, tex, false);
		this.z = z;
		//this.dir = dir;
		artType = ArtType.sprite;
		
		drawable = null;
		
		isSolid = false;
	}
	
	public SpriteDecal(float x, float y, int tex, boolean isStatic)
	{
		super(x, y, tex, false);
		this.isStatic = isStatic;
		artType = ArtType.sprite;
		
		drawable = null;
		
		isSolid = false;
	}
	
	public SpriteDecal(float x, float y, float z, int tex, boolean isStatic)
	{
		super(x, y, tex, false);
		this.z = z;
		this.isStatic = isStatic;
		artType = ArtType.sprite;
		
		drawable = null;
		
		isSolid = false;
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		// sprites don't do much
	}
	
	@Override
	public void updateLight(Level level) {
		color = level.getLightColorAt(x, y, z + 0.08f, null, new Color());
	}
	
	@Override
	public void updateDrawable() {
		if(drawable != null && drawable instanceof DrawableSprite) {
			DrawableSprite drbls = (DrawableSprite) drawable;
			drawable.dir.set(drawable.dir);
			drbls.rotation = rotation;
			drawable.update(this);
		}
		else if(artType != ArtType.hidden) {
			DrawableSprite drbls = new DrawableSprite(tex, artType);
			drbls.billboard = false;
			
			drawable = drbls;
			
			drawable.dir.set(drawable.dir);
			drbls.rotation = rotation;
			
			drawable.update(this);
		}
	}
}