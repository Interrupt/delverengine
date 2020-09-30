package com.interrupt.dungeoneer.gfx.animation;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.interfaces.Directional;

public class SpriteAnimation {
	/** Starting frame sprite index. */
	public int start;

	/** Ending frame sprite index. */
	public int end;

	/** Animation length in milliseconds. */
	public float speed;

	public int nextFrameOffset = 1;
	
	private int lastTex;
	private int currentTex;
	private float time;

	/** Is animation finished? */
	public boolean done = false;

	/** Does animation loop? */
	public boolean looping = false;

	/** Is animation playing? */
	public boolean playing = false;

	/** Mapping of animation frame indices to AnimationAction arrays. */
	public HashMap<String, Array<AnimationAction>> actions = null;

	/** Number of sprite directions. */
	public int directions = 1;

	/** Flip sprite for left/right facing? */
	public boolean flipDirections = true;

	public int directionsOffset = 1;

	public boolean reverseDirectionOrder = false;
	
	public SpriteAnimation() { }
	
	public SpriteAnimation(int start, int end, float speed, HashMap<String, Array<AnimationAction>> actions) {
		this.start = start;
		this.end = end;
		this.actions = actions;
		this.speed = speed;
	}
	
	public void play() {
		currentTex = start - 1;
		time = 0;
		looping = false;
		playing = true;
	}
	
	public void loop() {
		currentTex = start - 1;
		time = 0;
		looping = true;
		playing = true;
	}
	
	// advance time, do any actions, returns the current animation frame
	public void animate(float delta, Entity owner) {
		
		time += delta;
		lastTex = currentTex;
		
		// get the frame at this point in time
		float pos = time / speed;
		if(pos > 1.0f && looping == false) {
			done = true;
			playing = false;
			currentTex = end;
		}
		else {
			if(looping == true && pos > 1.0f) {
				time = 1.0f - pos;
				pos = time / speed;
			}
			currentTex = (int)((float)(end + 1 - start) * pos) + start;
		}
		
		if(currentTex < start) currentTex = start;
		if(currentTex > end) currentTex = end;

		currentTex *= nextFrameOffset;

		if(directions > 1) {
			Vector3 direction = new Vector3(owner.x, owner.z, owner.y);
			direction.sub(GameManager.renderer.camera.position).nor();

			float angle = (float)Math.atan2(direction.z, direction.x) * 57.2958f + 180;

			if(owner instanceof Directional) {
				Vector3 dir = ((Directional) owner).getDirection();
				direction.set(dir.x, dir.z, dir.y);
			}
			else {
				direction.set(owner.xa, 0f, owner.ya).nor();
			}

			float angle2 = (float)Math.atan2(direction.z, direction.x) * 57.2958f + 180;

			angle2 += 22.5f * directions;
			angle2 %= 360f;

			angle -= angle2;
			angle -= 360f / (directions * 2);
			if(angle < 0) angle += 360f;

			float d = angle / 360f;

			if(reverseDirectionOrder) {
				d = 1.0f - d;
			}

			boolean flip = false;
			int angleMod = (int)(d * directions);

			if(flipDirections) {
				int mid = (int)(directions * 0.5f);
				if (angleMod > mid) {
					int offset = angleMod - mid;
					angleMod = mid - offset;
					flip = true;
				}
			}

			currentTex += directionsOffset * angleMod;

			if(owner.drawable != null && owner.drawable instanceof DrawableSprite) {
				DrawableSprite s = (DrawableSprite)owner.drawable;
				s.xScale = flip ? -1.0f : 1f;
			}
		}
		
		// do any actions that occurred between the last frame and this one
		if(actions != null) {
			for(int i = lastTex + 1; i <= currentTex; i++) {
				Integer curFrame = i;

				if(nextFrameOffset != 0)
					curFrame /= nextFrameOffset;

				String key = curFrame.toString();
				Array<AnimationAction> actionList = actions.get(key);
				if(actionList != null) {
					for(AnimationAction action : actionList) {
						action.doAction(owner);
					}
				}
			}
		}
		
		owner.tex = currentTex;
	}

	public void randomizeTime() {
		time = Game.rand.nextFloat() * speed;
	}
}
