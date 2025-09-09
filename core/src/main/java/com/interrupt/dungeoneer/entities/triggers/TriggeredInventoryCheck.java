package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;

public class TriggeredInventoryCheck extends Trigger {

	public enum CompareType {HAS_ITEM, DOES_NOT_HAVE_ITEM, HAS_AT_LEAST, HAS_LESS_THAN, HAS_ROOM_IN_INVENTORY_FOR};

	@EditorProperty
	public CompareType compareType = CompareType.HAS_ITEM;

	@EditorProperty
	public String itemName = "ITEM_TO_FIND";

	@EditorProperty
	public int compareAmount = 1;

	@EditorProperty
	public String triggersOnFail = null;

	@EditorProperty
	public boolean removeItems = false;

	private transient Array<Item> foundItems = new Array<Item>();

	public TriggeredInventoryCheck() { hidden = true; spriteAtlas = "editor"; tex = 11; }
	
	@Override
	public void doTriggerEvent(String value) {

		if(triggerStatus == TriggerStatus.RESETTING)
			return;

		foundItems.clear();

		String[] itemNames = itemName.split(",");

		int freeInventorySlots = 0;
		if(Game.instance.player != null && Game.instance.player.inventory != null) {
			Array<Item> inventory = Game.instance.player.inventory;
			for(int i = 0; i < inventory.size; i++) {
				Item item = inventory.get(i);
				if(item != null) {
					for(int ii = 0; ii < itemNames.length; ii++) {
						String itemToLookFor = itemNames[ii];
						if (item.name != null && item.name.equals(itemToLookFor)) {
							foundItems.add(item);
						}
					}
				}
				else {
					freeInventorySlots++;
				}
			}
		}

		if(compareType == CompareType.HAS_ITEM) {
			if(foundItems.size >= itemNames.length) {
				foundItems(foundItems, value);
			}
			else {
				Game.instance.level.trigger(this, triggersOnFail, null);
				triggerStatus = TriggerStatus.RESETTING;
			}
		}

		if(compareType == CompareType.DOES_NOT_HAVE_ITEM) {
			if(foundItems.size == 0)
				foundItems(foundItems, value);
			else
				Game.instance.level.trigger(this, triggersOnFail, null);
		}

		if(compareType == CompareType.HAS_AT_LEAST) {
			if(foundItems.size >= compareAmount * itemNames.length && itemNames.length > 0)
				foundItems(foundItems, value);
			else
				Game.instance.level.trigger(this, triggersOnFail, null);
		}

		if(compareType == CompareType.HAS_LESS_THAN) {
			if(foundItems.size < compareAmount * itemNames.length && itemNames.length > 0)
				foundItems(foundItems, value);
			else
				Game.instance.level.trigger(this, triggersOnFail, null);
		}

		if(compareType == CompareType.HAS_ROOM_IN_INVENTORY_FOR) {
			if(freeInventorySlots >= compareAmount ) {
				super.doTriggerEvent(value);
			}
			else {
				Game.instance.level.trigger(this, triggersOnFail, null);
			}
		}

		foundItems.clear();
	}

	public void foundItems(Array<Item> found, String value) {
		String[] itemNames = itemName.split(",");

		Array<String> itemsToRemove = new Array<String>();
		for(int i = 0; i < itemNames.length; i++) {
			for(int ii = 0; ii < compareAmount; ii++) {
				itemsToRemove.add(itemNames[i]);
			}
		}

		if(removeItems) {
			if (Game.instance.player != null) {
				for (int i = 0; i < found.size && i < compareAmount * itemNames.length; i++) {
					if(itemsToRemove.contains(found.get(i).name, false)) {
						Game.instance.player.removeFromInventory(found.get(i));
					}
				}
			}
		}
		super.doTriggerEvent(value);
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		triggersOnFail = makeUniqueIdentifier(triggersOnFail, idPrefix);
	}
}
