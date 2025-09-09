package com.interrupt.dungeoneer.entities;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;

public class AnimatedSprite extends Sprite {
	
	@EditorProperty
	public int endAnimTex = 1;
	
	@EditorProperty
	public float animSpeed = 30f;

	@EditorProperty
	public boolean randomizeAnimation = false;

	public void playAnimation() {
		if(endAnimTex > 0 && animation == null) {
			this.playAnimation(new SpriteAnimation(tex, endAnimTex, animSpeed, null), true, randomizeAnimation);
		}
	}
	
	@Override
	public void init(Level level, Source source) {
		super.init(level, source);
		playAnimation();
	}
	
	public void editorTick(Level level, float delta) {
		super.editorTick(level, delta);
		if(animation != null) animation.animate(delta, this);
	}

	public void editorStartPreview(Level level) { if(endAnimTex > 0) this.playAnimation(new SpriteAnimation(tex, endAnimTex, animSpeed, null), true); }
	public void editorStopPreview(Level level) {
		if(animation != null) { 
			tex = animation.start;
			stopAnimation();
			animation = null;
		}
	}
}
