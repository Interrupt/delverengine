package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.projectiles.Missile;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class ItemStack extends Item {
	
	public int count = 1;
	public Item item;

	public ItemStack() { name = StringManager.get("items.ItemStack.defaultNameText"); itemType = itemType.stack; tex = 57; yOffset = -0.18f; }

	@EditorProperty
	public String stackType = "ARROW";
	
	public ItemStack(Item item, int count, String stackType) {
		this();
		this.item = item;
		this.count = count;
		this.stackType = stackType;
		tex = item.tex;
	}
	
	public String GetInfoText() {
		return MessageFormat.format(StringManager.get("items.ItemStack.infoText"), count);
	}
}