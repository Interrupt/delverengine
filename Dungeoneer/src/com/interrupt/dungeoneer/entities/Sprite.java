package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class Sprite extends Entity {
	
	protected SpriteAnimation animation = null;
	
	public Sprite() { artType = ArtType.sprite; }
	
	public Sprite(float x, float y, int tex)
	{
		super(x, y, tex, false);
		artType = ArtType.sprite;
		
		isSolid = false;
		
		drawable = new DrawableSprite(tex, artType);
	}
	
	public Sprite(float x, float y, float z, int tex)
	{
		super(x, y, tex, false);
		this.z = z;
		artType = ArtType.sprite;
		
		isSolid = false;
		
		drawable = new DrawableSprite(tex, artType);
	}
	
	public Sprite(float x, float y, int tex, boolean isStatic)
	{
		super(x, y, tex, false);
		this.isStatic = isStatic;
		artType = ArtType.sprite;
		
		isSolid = false;
		
		drawable = new DrawableSprite(tex, artType);
	}
	
	public Sprite(float x, float y, float z, int tex, boolean isStatic)
	{
		super(x, y, tex, false);
		this.z = z;
		this.isStatic = isStatic;
		artType = ArtType.sprite;
		
		isSolid = false;
		
		drawable = new DrawableSprite(tex, artType);
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		// static sprites don't do much
		if(animation != null && animation.playing) {
			animation.animate(delta, this);
			if(animation.done) isActive = false;	// die when done playing an animation
		}
	}

	public void tickPhysics(Level level, float delta) {
		super.tick(level, delta);
	}
	
	@Override
	public void updateLight(Level level) {
		if(!fullbrite)
            color = level.getLightColorAt(x, y, z + 0.08f, null, new Color());
	}
	
	// Start an animation on this sprite
	public void playAnimation(SpriteAnimation animation, boolean looping) {
		this.animation = animation;
		if(looping) this.animation.loop();
		else this.animation.play();
	}

	public void playAnimation(SpriteAnimation animation, boolean looping, boolean randomize) {
		playAnimation(animation, looping);

		if(randomize && this.animation != null) {
			animation.randomizeTime();
		}
	}
	
	public void stopAnimation() {
		animation = null;
	}

	public void setSpriteAtlas(String newAtlas) {
		if(spriteAtlas != newAtlas && drawable != null) {
			spriteAtlas = newAtlas;
			drawable.refresh();
		}
		else {
			spriteAtlas = newAtlas;
		}
	}
}
