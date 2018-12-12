package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.overlays.LevelUpOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.managers.StringManager;

public class Elixer extends Potion {
	public Elixer() { name = StringManager.get("items.Elixer.defaultNameText"); tex = 7; cost = 250; pickupSound = "pu_glass.mp3"; }

	public Elixer(float x, float y) {
		super(x, y);
		name = StringManager.get("items.Elixer.defaultNameText");
		tex = 7;
		cost = 250;
		pickupSound = "pu_glass.mp3";
	}
	
	public void Drink(Player player) {	
		player.history.drankPotion(this);
		Audio.playSound("cons_drink.mp3", 0.5f);
		OverlayManager.instance.push(new LevelUpOverlay(player));
		player.removeFromInventory(this);
	}
}
