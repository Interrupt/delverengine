package com.interrupt.dungeoneer.gfx.animation.lerp3d;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LerpedAnimationManager {
	
	public LerpedAnimation decorationCharge;
	
	public HashMap<String, LerpedAnimation> animations = new HashMap<String, LerpedAnimation>();
	
	public LerpedAnimation getAnimation(String anim) {
		return animations.get(anim);
	}
}
