package com.interrupt.dungeoneer.entities;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.managers.ItemManager;

public class ItemSpawner extends DirectionalEntity {
	public ItemSpawner() { artType = ArtType.sprite; tex = 0; spriteAtlas = "editor"; tex = 5; hidden = true; floating = true; }

	public enum ItemType { melee, ranged, armor, wands, potions, scrolls, decorations, unique, junk, food }

	public enum ItemPlacement { world, player_hotbar, player_inventory, player_equip }
	
	@EditorProperty(group = "Spawns") public ItemType itemType = ItemType.melee;
	@EditorProperty(group = "Spawns") public Integer itemLevel = null;
	@EditorProperty(group = "Spawns") public Item.ItemCondition itemCondition = Item.ItemCondition.normal;
	@EditorProperty(group = "Spawns") public String itemName = null;
	@EditorProperty(group = "Spawns") boolean waitForTrigger = false;
	@EditorProperty(group = "Spawns") ItemPlacement placement = ItemPlacement.world;
	
	@Override
	public void init(Level level, Source source) {
		if(!waitForTrigger && (source == Source.LEVEL_START || source == Source.SPAWNED) && isActive) {
			spawn(level);
		}
	}

	public void spawn(Level level) {
		if(isActive) {
			Entity i = getItem();
			if (i != null) {
				i.x = x;
				i.y = y;
				i.z = z;
				i.init(level, Source.LEVEL_START);

				// Place in the world, or into player inventory
				if(placement == ItemPlacement.world)
					level.entities.add(i);
				else
					placeInInventory(i);
			}
			isActive = false;
		}
	}

	public Entity getItem() {
		ItemManager manager = Game.instance.itemManager;

		if(itemLevel == null && Game.instance != null && Game.instance.player != null) itemLevel = Game.instance.player.level;
		if(itemLevel == null) itemLevel = 1;
		if(itemLevel <= 0) itemLevel = 1;

		if(itemName != null) {
			Entity e = manager.FindItem(itemName, itemCondition);
			if(e != null) return e;
		}

		if(itemType == ItemType.melee) {
			return manager.GetRandomWeapon(itemLevel, itemCondition);
		}
		else if(itemType == ItemType.ranged) {
			return manager.GetRandomRangedWeapon(itemLevel, itemCondition);
		}
		else if(itemType == ItemType.armor) {
			return manager.GetRandomArmor(itemLevel, itemCondition);
		}
		else if(itemType == ItemType.wands) {
			return manager.GetRandomWand(itemCondition);
		}
		else if(itemType == ItemType.potions) {
			return manager.GetRandomPotion();
		}
		else if(itemType == ItemType.scrolls) {
			return manager.GetRandomScroll();
		}
		else if(itemType == ItemType.decorations) {
			return manager.GetRandomDecoration();
		}
		else if(itemType == ItemType.unique) {
			return manager.GetUniqueItem(itemLevel, Game.instance.progression);
		}
		else if(itemType == ItemType.junk) {
			return manager.GetRandomJunk();
		}
		else if(itemType == ItemType.food) {
			return manager.GetRandomFood();
		}

		return null;
	}

	public void placeInInventory(Entity e) {
		if(e == null)
			return;

		if(e instanceof Item) {
			Item item = (Item)e;

			if(Game.instance == null || Game.instance.player == null)
				return;

			Player p = Game.instance.player;

			// might need to initialize this?
			if(p.inventory != null) {
				if (p.inventory.size < p.inventorySize) {
					for (int i = 0; i < p.inventorySize; i++)
						p.inventory.add(null);
				}
			}

			boolean foundSpot = false;

			if(placement == ItemPlacement.player_hotbar || placement == ItemPlacement.player_equip) {
				foundSpot = p.addToInventory(item, placement == ItemPlacement.player_equip);
			}
			else {
				foundSpot = p.addToBackpack(item);
				if(!foundSpot) {
					foundSpot = p.addToInventory(item, placement == ItemPlacement.player_equip);
				}
			}

			if(!foundSpot) {
				p.throwItem(item, Game.GetLevel(), 0f, 0f);
			}
		}
	}

	@Override
	public void onTrigger(Entity instigator, String value) {
		if(waitForTrigger) {
			spawn(Game.GetLevel());
		}
	}
}