package com.interrupt.dungeoneer.entities.triggers;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.PositionedSound;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

public class TriggeredTeleportPlayer extends Trigger {
	public TriggeredTeleportPlayer() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@EditorProperty
	boolean doEffects=false;
	
	@EditorProperty
	boolean useOffsets=false;

	@EditorProperty
	public String toWarpMarkerId = null;
	
	@Override
	public void doTriggerEvent(String value) {
		Player p = Game.instance.player;

		float xTarget = 0;
		float yTarget = 0;
		
		if (useOffsets){
			xTarget = Math.round(p.x)-p.x;
			yTarget = Math.round(p.y)-p.y;
		}

		if(toWarpMarkerId != null) {
			float playerRot = p.rot;
			Game.instance.putPlayerAtWarpMarker(toWarpMarkerId);
			if(useOffsets) {
				p.rot = playerRot;
			}
		}

		if (doEffects) doEffect(new Vector3((float)p.x+0.5f,(float)p.y+0.5f,(float)p.z), Game.GetLevel());
		
		p.x -= xTarget;
		p.y -= yTarget;
		
		if (doEffects) doEffect(new Vector3((float)p.x+0.5f,(float)p.y+0.5f,(float)p.z), Game.GetLevel());
	}
	
	public void doEffect(Vector3 pos, Level level) {
		Random r = Game.rand;
		int particleCount = 3;
		particleCount *= Options.instance.gfxQuality;
		if(particleCount <= 0) particleCount = 1;
		
		for(int i = 0; i < particleCount; i++)
		{
			int speed = r.nextInt(35) + 20;
			Particle part = new Particle(pos.x + r.nextFloat() * 0.4f - 0.2f, pos.y + r.nextFloat() * 0.4f - 0.2f, pos.z, 0f, 0f, 0f, 0, Color.ORANGE, true);
			part.floating = true;
			part.playAnimation(8, 13, speed);
			level.SpawnNonCollidingEntity(part);
		}
		
		level.SpawnNonCollidingEntity( new DynamicLight(pos.x,pos.y,pos.z, new Vector3(Color.ORANGE.r * 2f, Color.ORANGE.g * 2f, Color.ORANGE.b * 2f)).startLerp(new Vector3(0,0,0), 40, true) );
		level.SpawnNonCollidingEntity( new PositionedSound(pos.x, pos.y, pos.z, Audio.spell, 0.9f, 12f, 200));
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		toWarpMarkerId = makeUniqueIdentifier(toWarpMarkerId, idPrefix);
	}
}
