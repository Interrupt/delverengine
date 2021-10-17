package com.interrupt.dungeoneer.gfx.animation.lerp3d;

import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

public class LerpedAnimationManager {
	public HashMap<String, LerpedAnimation> animations = new HashMap<String, LerpedAnimation>();

	// Need to keep multiple copies of an animation depending on the animation ID
	private IntMap<LerpedAnimation> animationCache = new IntMap<LerpedAnimation>();

    public LerpedAnimation Copy(Class<?> type, LerpedAnimation tocopy) { return (LerpedAnimation) KryoSerializer.copyObject(tocopy); }
	
	public LerpedAnimation getAnimation(String anim, String animId) {
        int animCacheKey = Objects.hash(anim, animId);
	    LerpedAnimation cached = animationCache.get(animCacheKey);
	    if(cached != null)
	        return cached;

	    LerpedAnimation foundAnimation = animations.get(anim);
	    if(foundAnimation == null)
	        return null;

	    LerpedAnimation copy = Copy(LerpedAnimation.class, foundAnimation);
        animationCache.put(animCacheKey, copy);
		return copy;
	}
}
