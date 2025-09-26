package com.interrupt.helpers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ArrayMap;

public class InterpolationHelper {

	public enum InterpolationMode { bounce, circle, elastic, exp10, exp5, linear, fade, sine, swing, bounceIn, circleIn, elasticIn, exp10In, exp5In, sineIn, swingIn, bounceOut, circleOut, elasticOut, exp10Out, exp5Out, sineOut, swingOut }
	private static ArrayMap<InterpolationMode, Interpolation> interpolatorMap;
	
	static {
		interpolatorMap = new ArrayMap<InterpolationMode, Interpolation>();
		interpolatorMap.put(InterpolationMode.bounce, Interpolation.bounce);
		interpolatorMap.put(InterpolationMode.circle, Interpolation.circle);
		interpolatorMap.put(InterpolationMode.elastic, Interpolation.elastic);
		interpolatorMap.put(InterpolationMode.exp10, Interpolation.exp10);
		interpolatorMap.put(InterpolationMode.exp5, Interpolation.exp5);
		interpolatorMap.put(InterpolationMode.linear, Interpolation.linear);
		interpolatorMap.put(InterpolationMode.fade, Interpolation.fade);
		interpolatorMap.put(InterpolationMode.sine, Interpolation.sine);
		interpolatorMap.put(InterpolationMode.swing, Interpolation.swing);
		interpolatorMap.put(InterpolationMode.bounceIn, Interpolation.bounceIn);
		interpolatorMap.put(InterpolationMode.circleIn, Interpolation.circleIn);
		interpolatorMap.put(InterpolationMode.elasticIn, Interpolation.elasticIn);
		interpolatorMap.put(InterpolationMode.exp10In, Interpolation.exp10In);
		interpolatorMap.put(InterpolationMode.exp5In, Interpolation.exp5In);
		interpolatorMap.put(InterpolationMode.sineIn, Interpolation.sineIn);
		interpolatorMap.put(InterpolationMode.swingIn, Interpolation.swingIn);
		interpolatorMap.put(InterpolationMode.bounceOut, Interpolation.bounceOut);
		interpolatorMap.put(InterpolationMode.circleOut, Interpolation.circleOut);
		interpolatorMap.put(InterpolationMode.elasticOut, Interpolation.elasticOut);
		interpolatorMap.put(InterpolationMode.exp10Out, Interpolation.exp10Out);
		interpolatorMap.put(InterpolationMode.exp5Out, Interpolation.exp5Out);
		interpolatorMap.put(InterpolationMode.sineOut, Interpolation.sineOut);
		interpolatorMap.put(InterpolationMode.swingOut, Interpolation.swingOut);
	}
	
	public static Interpolation getInterpolator(InterpolationMode mode) {
		return interpolatorMap.get(mode);
	}

}
