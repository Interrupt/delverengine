package com.interrupt.helpers;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.helpers.InterpolationHelper.InterpolationMode;

public class AnimationHelper {
	
	private Vector3 startPosition;
	private Vector3 startRotation;

	private Vector3 endPosition;
	private Vector3 endRotation;
	
	private float animLength;
	private float animPosition;
	
	private boolean reversed = false;
	
	private transient Vector3 currentPosition = new Vector3();
	private transient Vector3 currentRotation = new Vector3();
	private transient boolean done = false;
	
	private InterpolationMode interpolationMode = InterpolationMode.exp5;  
	
	private boolean paused = false;
	
	public AnimationHelper() { }
	
	public AnimationHelper(Vector3 startPos, Vector3 startRot, Vector3 movesTo, Vector3 rotatesTo, float length) {
		startPosition = startPos;
		startRotation = startRot;
		endPosition = movesTo;
		endRotation = rotatesTo;
		animLength = length;
	}
	
	public AnimationHelper(Vector3 startPos, Vector3 startRot, Vector3 movesTo, Vector3 rotatesTo, float length, InterpolationMode interpolator) {
		startPosition = startPos;
		startRotation = startRot;
		endPosition = movesTo;
		endRotation = rotatesTo;
		animLength = length;
		interpolationMode = interpolator;
	}

	public void tickAnimation(float delta) {
		if(!paused) {
			if(!reversed) animPosition += delta;
			else animPosition -= delta;
		}
		
		// clamp to animation bounds
		if(!reversed && animPosition > animLength) animPosition = animLength;
		else if(reversed && animPosition < 0) animPosition = 0;
		
		// is this done?
		if(!reversed && animPosition == animLength) done = true;
		else if(reversed && animPosition == 0) done = true;
		else done = false;
		
		float a = animPosition / animLength;
		
		Interpolation interpolator = InterpolationHelper.getInterpolator(interpolationMode);
		
		// set the animation vectors
		currentPosition.set(
			interpolator.apply(startPosition.x, endPosition.x, a),
			interpolator.apply(startPosition.y, endPosition.y, a),
			interpolator.apply(startPosition.z, endPosition.z, a)
		);
		
		currentRotation.set(
			interpolator.apply(startRotation.x, endRotation.x, a),
			interpolator.apply(startRotation.y, endRotation.y, a),
			interpolator.apply(startRotation.z, endRotation.z, a)
		);
	}
	
	public Vector3 getCurrentPosition() {
		return currentPosition;
	}
	
	public Vector3 getCurrentRotation() {
		return currentRotation;
	}
	
	public boolean isDonePlaying() {
		return done;
	}
	
	public void reverse() {
		reversed = !reversed;
	}
	
	public void togglePause() {
		paused = !paused;
	}
	
	public boolean isPaused() { return paused; }
	
	public void setInterpolator(InterpolationMode interpolator) {
		this.interpolationMode = interpolator;
	}

	public boolean isReversed() {
		return reversed;
	}
	
	public float getAnimationPosition() {
		return animPosition;
	}
	
	public void setAnimationPosition(float animPos) {
		animPosition = animPos;
	}
}
