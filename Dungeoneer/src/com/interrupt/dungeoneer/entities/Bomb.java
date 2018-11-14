package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.entities.spells.Spell;
import com.interrupt.dungeoneer.entities.spells.SplashExplosion;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;

public class Bomb extends Entity {
	public Bomb() { super.artType = ArtType.sprite; shadowType = ShadowType.BLOB; }
	
	public boolean countdownStarted = true;
	public float countdownTimer = 150f;
	
	public float explosionRadius = 2f;
	public float explosionImpulse = 0.2f;
	public float explosionDamage = 3f;
	public DamageType explosionDamageType = DamageType.PHYSICAL;
	public Color explosionColor = Colors.EXPLOSION;
	
	private Color flashColor = new Color(Colors.BOMB_FLASH);
	
	SplashExplosion bombSpell = new SplashExplosion();

	public StatusEffect applyStatusEffect = null;
	
	public float timerStart = -1f;
	
	@Override
	public void tick(Level level, float delta) {
		super.tick(level, delta);
		
		if(timerStart < 0) timerStart = countdownTimer;
		
		if(countdownStarted) {
			fullbrite = true;
			color = flashColor;
			
			float sinMod = (float)Math.sin((timerStart / (countdownTimer + 22f)) * 15f) * 0.5f + 0.5f;
			if(sinMod < 0) sinMod = 0;
			if(sinMod > 1) sinMod = 1;
			
			flashColor.set(0.85f * sinMod + 0.15f, 0.35f * sinMod + 0.15f, 0.35f * sinMod + 0.15f, 1f);

            // Explode if the fuse runs out
			countdownTimer -= delta;
			if(countdownTimer <= 0) {
				isActive = false;
				explode(level);
			}

            // Cut the fuse if we touch a monster
            Entity e = level.checkEntityCollision(x - 0.5f, y - 0.5f, z, collision.x, collision.y, collision.z, null);
            if(e != null && (e instanceof Monster)) countdownTimer = 0;
		}
	}
	
	public void explode(Level level) {
		bombSpell.damageType = explosionDamageType;
		bombSpell.damage = (int)explosionDamage;
		bombSpell.explodeSound = "potions/sfx_explode.mp3";
		bombSpell.applyStatusEffect = applyStatusEffect;
		bombSpell.doCast(new Vector3(x,y,z + yOffset + 0.2f), Vector3.Z);
	}
}
