package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.managers.StringManager;

public class Key extends Item {
	
	public Key() { }

	public Key(float x, float y) {
		super(x, y, 0, ItemType.key, StringManager.get("Key"));
	}
	
}
