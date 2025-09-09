package com.interrupt.helpers;

import com.interrupt.helpers.InterpolationHelper.InterpolationMode;

public class LerpHelper {
	private float pos = 0;
	private float length = 1;
	private InterpolationMode lerpMode = InterpolationMode.circle;
	
	public LerpHelper(float length, InterpolationMode lerpMode) {
		this.length = length;
		this.lerpMode = lerpMode;
	}
	
	public void tick(float delta) {
		pos += delta;
		if(pos > length) pos = length;
	}
	
	public float getValue() {
		return InterpolationHelper.getInterpolator(lerpMode).apply(pos / length);
	}
}