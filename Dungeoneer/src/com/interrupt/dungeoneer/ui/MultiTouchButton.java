package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class MultiTouchButton extends Button {
	
	public boolean isDragging = false;

	public MultiTouchButton(Drawable region) {
		super(region);
	}
	
	public MultiTouchButton(Drawable regionUp, Drawable regionDown) {
		super(regionUp, regionDown);
		
		final MultiTouchButton btn = this;
		
		addListener(new InputListener() {
			@Override
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
	        	btn.isDragging = button == 0;
	        	return true;
	        }
	        
	        @Override
	        public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
	        	btn.isDragging = false;
	        }
		});
	}

	/*@Override
	public boolean touchDown (float x, float y, int pointer) {
		pointer = 0;
		if (super.touchDown(x, y, pointer)) return true;
		if (pointer != 0) return false;
		isPressed = true;
		return true;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
		if (hit(x, y) != null) click(x, y);
		isPressed = false;
	}*/
}
