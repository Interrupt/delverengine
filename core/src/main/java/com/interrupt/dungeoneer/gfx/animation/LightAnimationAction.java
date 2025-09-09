package com.interrupt.dungeoneer.gfx.animation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.game.Game;

public class LightAnimationAction extends AnimationAction {
	
	private Color startColor = new Color(1f, 1f, 1f, 1f);
	private Color endColor = new Color(0f, 0f, 0f, 1f);
	private float lightTime = 50;
	public boolean useSpellColor = true;
	
	public LightAnimationAction() { }

	@Override
	public void doAction(Entity instigator) {
		DynamicLight l = new DynamicLight(instigator.x, instigator.y, instigator.z, new Vector3(startColor.r, startColor.g, startColor.b).scl(1.5f));
		l.startLerp(new Vector3(endColor.r,endColor.g,endColor.b).scl(1.5f), lightTime, true);
		Game.instance.level.SpawnEntity(l);
	}
	
	public void setEndColor(Color color) {
		endColor.set(color);
	}
	
	public void setStartColor(Color color) {
		startColor.set(color);
	}
}
