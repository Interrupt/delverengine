package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;
import com.interrupt.dungeoneer.gfx.drawables.DrawableBeam;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class SpriteBeam extends DirectionalEntity {

	protected SpriteAnimation animation = null;

	public SpriteBeam() { artType = ArtType.sprite; fullbrite = true; }

	@EditorProperty
	public float size = 1f;

	@EditorProperty
	public DrawableBeam.BeamRenderModes beamRenderMode = DrawableBeam.BeamRenderModes.CROSS;

	public SpriteBeam(float x, float y, int tex)
	{
		super(x, y, tex, false);
		artType = ArtType.sprite;
		isSolid = false;
	}

	public SpriteBeam(float x, float y, float z, int tex)
	{
		super(x, y, tex, false);
		this.z = z;
		artType = ArtType.sprite;
		isSolid = false;
	}

	public SpriteBeam(float x, float y, int tex, boolean isStatic)
	{
		super(x, y, tex, false);
		this.isStatic = isStatic;
		artType = ArtType.sprite;
		isSolid = false;
	}

	public SpriteBeam(float x, float y, float z, int tex, boolean isStatic)
	{
		super(x, y, tex, false);
		this.z = z;
		this.isStatic = isStatic;
		artType = ArtType.sprite;
		isSolid = false;
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		if(drawable == null || !(drawable instanceof DrawableBeam)) {
			drawable = new DrawableBeam(tex, artType);
		}

		// static sprites don't do much
		if(animation != null && animation.playing) {
			animation.animate(delta, this);
			if(animation.done) isActive = false;	// die when done playing an animation
		}
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

	@Override
	public void updateDrawable() {
		if(drawable == null) {
			drawable = new DrawableBeam(tex, artType);
		}

		super.updateDrawable();

		if(drawable != null && (drawable instanceof DrawableBeam)) {
			DrawableBeam beam = (DrawableBeam)drawable;
			beam.tex = tex;
			beam.size = size;
			beam.dir = getDirection();
			beam.dir.rotate(Vector3.X, 90f);
			beam.dir.scl(-1f);
			beam.beamRenderMode = beamRenderMode;
		}

		if(owner != null) {
			drawable.dir.set(owner.xa, owner.za, owner.ya).nor();
		}
	}
}
