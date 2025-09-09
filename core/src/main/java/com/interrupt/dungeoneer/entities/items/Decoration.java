package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

public class Decoration extends Item {
	
	public Decoration() { 
		stepHeight = 0.1f;
		collision.set(0.2f,0.2f,0.2f);
		isSolid = true;
		yOffset = -0.1f;
		equipSound = "/ui/ui_equip_item.mp3";
		equipLoc = "OFFHAND";
	}

	public Decoration(float x, float y, int tex, String name) {
		super(x, y, tex, ItemType.junk, name);
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		super.tick(level, delta);
	}
}
