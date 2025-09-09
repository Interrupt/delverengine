package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class ItemStack extends Item {
	/** Stack count. */
	public int count = 1;

	/** Item contained in stack. */
	public Item item;

	/** Stack type. */
	@EditorProperty
	public String stackType = "ARROW";

	public ItemStack() { name = StringManager.get("items.ItemStack.defaultNameText"); itemType = itemType.stack; tex = 57; yOffset = -0.18f; }

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