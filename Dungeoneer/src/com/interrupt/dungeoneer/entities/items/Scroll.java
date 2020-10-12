package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.spells.Spell;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class Scroll extends Item {
    /** Spell to cast when consumed. */
	public Spell spell;
	
	public void Read(Player player) {
		
		player.history.usedScroll(this);
		
		if(spell != null) {
			spell.zap(player, Game.camera.direction.cpy());
		}
		else {
			Game.ShowMessage(StringManager.get("items.Scroll.nothingHappensText"), 1);
		}
		
		int location = player.inventory.indexOf(this, true);
		player.inventory.set(location, null);
		
		Game.RefreshUI();
	}

	public boolean inventoryUse(Player player){
		Read(player);
        return true;
	}

	@Override
	public void doPickup(Player player) {
		super.doPickup(player);
		Read(player);
	}
}