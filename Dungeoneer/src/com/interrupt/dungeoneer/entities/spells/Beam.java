package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.PositionedSound;
import com.interrupt.dungeoneer.entities.projectiles.BeamProjectile;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;

public class Beam extends Spell {
	
	public Beam() { }

	public float beamScale = 1f;
	public int startTex = 14;
	public int endTex = 15;
	public String spriteAtlas = "sprites";
	public float speed = 0.9f;
	
	@Override
	public void doCast(Entity owner, Vector3 direction, Vector3 position) {
		Player p = Game.instance.player;
		float xOffset = (owner == p) ? 0f : 0f;
		float yOffset = (owner == p) ? 0f : 0f;
		float zOffset = (owner == p) ? 0f : 0.31f;
		
		//float speed = 0.9f;
		
		int dmg = doAttackRoll();

		spellColor.a = 1f;

		BeamProjectile projectile = new BeamProjectile(position.x + xOffset, position.y + yOffset, position.z + zOffset, direction.x * speed, direction.z * speed, direction.y * speed, dmg, damageType, spellColor, owner);
		projectile.scale = beamScale;
		projectile.tex = startTex;
		projectile.startTex = startTex;
		projectile.endTex = endTex;
		projectile.spriteAtlas = spriteAtlas;
		Game.GetLevel().entities.add(projectile);
	}
	
	@Override
	public void playCastSound(Actor owner) {
		Audio.playPositionedSound("mg_light_shoot_01.mp3,mg_light_shoot_02.mp3,mg_light_shoot_03.mp3,mg_light_shoot_04.mp3", new Vector3(owner.x, owner.y, owner.z), 0.75f, 13f);
	}
}
