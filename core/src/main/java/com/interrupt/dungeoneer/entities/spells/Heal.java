package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;

import java.util.Random;

public class Heal extends Spell {
    /** Heal amount. */
	private int heal = 6;

	public Heal() { }

	@Override
	public void doCast(Entity owner, Vector3 direction, Vector3 position) {
		if(owner instanceof Actor) {
			Actor caster = (Actor)owner;
			caster.hp += heal;
			if(caster.hp > caster.getMaxHp()) caster.hp = caster.getMaxHp();
		}
	}

	@Override
	protected void doCastEffect(Vector3 pos, Level level, Entity owner) {
		pos.z -= 0.275f;

		Random r = Game.rand;
		int particleCount = 16;
		particleCount *= Options.instance.gfxQuality;

		for(int i = 0; i < particleCount; i++)
		{
			float xS = 0;
			float yS = 0;
			float zS = 0.004f + (r.nextFloat() * 0.001f);

			Particle p = new Particle(pos.x + r.nextFloat() * 0.95f - 0.475f, pos.y + r.nextFloat() * 0.95f - 0.475f, pos.z + (r.nextFloat() * owner.collision.z), xS, yS, zS, 0, Color.CYAN, true);
			p.za = 0;
			p.floating = true;
			p.playAnimation(8, 12, r.nextInt(45) + 40);

			level.SpawnNonCollidingEntity( p );

			if(owner instanceof Player) {
				// Push the particles in the camera direction to be more visible to the player
				p.x += Game.camera.direction.x * 0.5f;
				p.y += Game.camera.direction.z * 0.5f;
				p.z += Game.camera.direction.y * 0.5f;
			}
		}

		level.SpawnNonCollidingEntity( new DynamicLight(pos.x,pos.y,pos.z, new Vector3(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b)).startLerp(new Vector3(0,0,0), 40, true).setHaloMode(Entity.HaloMode.BOTH) );

		level.SpawnNonCollidingEntity( new PositionedSound(pos.x, pos.y, pos.z, Audio.spell, 0.75f, 12f, 200));
	}
}
