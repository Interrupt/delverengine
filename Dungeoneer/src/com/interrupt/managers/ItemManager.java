package com.interrupt.managers;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.items.*;
import com.interrupt.dungeoneer.entities.items.Potion.PotionType;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.rpg.Stats;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.helpers.LootListHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ItemManager {
    /** Mapping of dungeon level to melee weapon table. */
	public HashMap<String, Array<Sword>> melee;

    /** Mapping of dungeon level to armor table. */
	public HashMap<String, Array<Armor>> armor;

	/** Mapping of dungeon level to ranged weapon table. */
	public HashMap<String, Array<Item>> ranged;

	/** Unique item table. */
    public Array<Item> unique;

    /** Wand table. */
	public Array<Wand> wands;

	/** Potion table. */
	public Array<Potion> potions;

	/** Food table */
	public Array<Food> food;

	/** Scroll table */
	public Array<Scroll> scrolls;

	/** Decoration table */
	public Array<Decoration> decorations;

	/** Junk table */
	public Array<Item> junk;

	/** Weapon enchantment table */
	public Array<ItemModification> weaponEnchantments;

	/** Weapon prefix enchantment table */
    public Array<ItemModification> weaponPrefixEnchantments = null;

	/** Armor enchantment table */
    public Array<ItemModification> armorEnchantments = null;

	/** Armor prefix enchantment table */
    public Array<ItemModification> armorPrefixEnchantments = null;

	public HashMap<String, Item> itemsByName = null;
	
	Random random;
	
	public ItemManager() { random = new Random(); }

	private class ItemManagerEntry {
		int itemLevel;
		Item item;

		public ItemManagerEntry(int itemLevel, Item item) {
			this.itemLevel = itemLevel;
			this.item = item;
		}
	}
	
	public ItemModification GetRandomWeaponSuffixEnchantment() {
	    if(weaponEnchantments == null || weaponEnchantments.size == 0) return null;
		int r = random.nextInt(weaponEnchantments.size);
		ItemModification picked = Copy( ItemModification.class, weaponEnchantments.get(r) );
		return picked;
	}

    public ItemModification GetRandomWeaponPrefixEnchantment() {
        if(weaponPrefixEnchantments == null || weaponPrefixEnchantments.size == 0) return null;
        int r = random.nextInt(weaponPrefixEnchantments.size);
        ItemModification picked = Copy( ItemModification.class, weaponPrefixEnchantments.get(r) );
        return picked;
    }

    public ItemModification GetRandomArmorSuffixEnchantment() {
        if(armorEnchantments == null || armorEnchantments.size == 0) return null;
        int r = random.nextInt(armorEnchantments.size);
        ItemModification picked = Copy( ItemModification.class, armorEnchantments.get(r) );
        return picked;
    }

    public ItemModification GetRandomArmorPrefixEnchantment() {
        if(armorPrefixEnchantments == null || armorPrefixEnchantments.size == 0) return null;
        int r = random.nextInt(armorPrefixEnchantments.size);
        ItemModification picked = Copy( ItemModification.class, armorPrefixEnchantments.get(r) );
        return picked;
    }

    public static void setItemLevel(Integer level, Item item) {
        if(level == null || item == null) return;

        if(item.minItemLevel != null || item.maxItemLevel != null) {
            item.itemLevel = level;
        }

        if(item.minItemLevel != null) {
            item.itemLevel = Math.max(item.minItemLevel, item.itemLevel);
        }
        if(item.maxItemLevel != null) {
            item.itemLevel = Math.min(item.maxItemLevel, item.itemLevel);
        }
    }

	private transient LootListHelper<Sword> swordLootListHelper = new LootListHelper<Sword>("melee");
	public Sword GetRandomWeapon(Integer level) {
		Integer difficultyLvl = random.nextInt(level) + 1;

		Sword picked = swordLootListHelper.GetLeveledLoot(difficultyLvl, melee);
		if(picked == null)
			return null;

		if(picked.canSpawnEnchanted) {
			if (random.nextFloat() > 0.8f) {
				picked.enchantment = GetRandomWeaponSuffixEnchantment();
				if (random.nextFloat() > 0.8f) picked.identified = false;
			}
			if (random.nextFloat() > 0.8f) {
				picked.prefixEnchantment = GetRandomWeaponPrefixEnchantment();
				if (random.nextFloat() > 0.8f) picked.identified = false;
			}
		}

		if(picked.randomizeCondition) {
			int condition = random.nextInt(Item.ItemCondition.values().length - 1) + 1;
			picked.itemCondition = Item.ItemCondition.values()[condition];
		}

		updateCostWithEnchantments(picked);
        setItemLevel(level, picked);
		
		return picked;
	}

	public Array<Item> GetAllUnspawnedUniques(Progression progression) {
        Array<Item> allUniques = new Array<Item>();

        if(unique != null && unique.size > 0) {
            for (int i = 0; i < unique.size; i++) {
                Item itm = unique.get(i);
                if (!progression.hasSpawnedUniqueItem(itm)) {
                    allUniques.add(itm);
                }
            }
        }

        return allUniques;
    }

    // warning, removes unique item from the spawnable list once it is returned
    public Item GetUniqueItem(Integer level, Progression progression) {
        Array<Item> allUniques = GetAllUnspawnedUniques(progression);
        if(allUniques == null || allUniques.size == 0) return null;

	    int r = random.nextInt(allUniques.size);
        Item picked = Copy( Item.class, allUniques.get(r));

        setItemLevel(level, picked);

        progression.spawnedUniqueItem(picked);
        return picked;
    }

	public Sword GetRandomWeapon(Integer level, Item.ItemCondition condition) {
		Sword item = GetRandomWeapon(level);
		if(item != null) item.itemCondition = condition;
		return item;
	}

	private transient LootListHelper<Item> rangedLootListHelper = new LootListHelper<Item>("ranged");
	public Item GetRandomRangedWeapon(Integer level) {
		Integer difficultyLvl = random.nextInt(level) + 1;

		Item picked = rangedLootListHelper.GetLeveledLoot(difficultyLvl, ranged);
		if(picked == null)
			return null;
		
		if(picked instanceof Weapon) {
			if(picked.randomizeCondition) {
				int condition = random.nextInt(Item.ItemCondition.values().length - 1) + 1;
				picked.itemCondition = Item.ItemCondition.values()[condition];
			}

			if(picked.canSpawnEnchanted) {
				if (random.nextFloat() > 0.8f) {
					picked.enchantment = GetRandomWeaponSuffixEnchantment();
					if (random.nextFloat() > 0.8f) picked.identified = false;
				}
				if (random.nextFloat() > 0.8f) {
					picked.prefixEnchantment = GetRandomWeaponPrefixEnchantment();
					if (random.nextFloat() > 0.8f) picked.identified = false;
				}
			}
		}

		updateCostWithEnchantments(picked);
        setItemLevel(level, picked);
		
		return picked;
	}
	
	public Item GetRandomRangedWeapon(Integer level, Item.ItemCondition condition) {
		Item item = GetRandomRangedWeapon(level);
		if(item != null) item.itemCondition = condition;
		return item;
	}

	private transient LootListHelper<Armor> armorLootListHelper = new LootListHelper<Armor>("armor");
	public Armor GetRandomArmor(Integer level) {
		Integer difficultyLvl = random.nextInt(level) + 1;

		Armor picked = armorLootListHelper.GetLeveledLoot(difficultyLvl, armor);
		if(picked == null)
			return null;

		if(picked.randomizeCondition) {
			int condition = random.nextInt(Item.ItemCondition.values().length - 1) + 1;
			picked.itemCondition = Item.ItemCondition.values()[condition];
		}

		if(picked.canSpawnEnchanted) {
			if (random.nextFloat() > 0.8f) {
				picked.enchantment = GetRandomArmorSuffixEnchantment();
				if (random.nextFloat() > 0.8f) picked.identified = false;
			}
			if (random.nextFloat() > 0.8f) {
				picked.prefixEnchantment = GetRandomArmorPrefixEnchantment();
				if (random.nextFloat() > 0.8f) picked.identified = false;
			}
		}

		updateCostWithEnchantments(picked);
        setItemLevel(level, picked);
		
		return picked;
	}

	public void updateCostWithEnchantments(Item item) {
		// Adjust price with condition
		item.cost *= Item.itemConditionCostMod[item.itemCondition.ordinal()];

		// Adjust price with enchantments
		Array<ItemModification> modifications = item.getEnchantmentsEvenUnidentified();
		for(int i = 0; i < modifications.size; i++) {
			item.cost += modifications.get(i).getCostMod();
		}

		// Always worth something!
		if(item.cost <= 0)
			item.cost = 1;
	}
	
	public Armor GetRandomArmor(Integer level, Item.ItemCondition condition) {
		Armor item = GetRandomArmor(level);
		if(item != null) item.itemCondition = condition;
		return item;
		
	}
	
	public Wand GetRandomWand() {
		if(wands == null || wands.size == 0) return null;
		
		int r = random.nextInt(wands.size);
		Wand picked = (Wand)Copy( Wand.class, wands.get(r) );

		if(picked.randomizeCondition) {
			int condition = random.nextInt(Item.ItemCondition.values().length - 1) + 1;
			picked.itemCondition = Item.ItemCondition.values()[condition];
		}
		
		return picked;
	}

	public Wand GetRandomWand(Item.ItemCondition condition) {
		Wand item = GetRandomWand();
		if(item != null) item.itemCondition = condition;
		return item;
	}

	public Potion GetRandomPotion() {
		if(Game.instance.player == null || potions == null) return null;
		
		// fill this games shuffled potion list if it hasn't been done yet
		if(Game.instance.player.shuffledPotions.size == 0 && potions.size > 0) {
			for(int i = 0; i < potions.size; i++) {
				Potion p = (Potion)Copy(Potion.class, potions.get(i));
				Game.instance.player.shuffledPotions.add(p);
			}

			Game.instance.player.shuffledPotions.shuffle();
			
			// this assumes that the number of data potions matches the number of potion types
			PotionType[] allPotionTypes = PotionType.values();
			for(int i = 0; i < potions.size; i++) {
				if(i < allPotionTypes.length)
					Game.instance.player.shuffledPotions.get(i).potionType = allPotionTypes[i];
			}
		}
		
		if(Game.instance.player.shuffledPotions.size == 0) return null;
		return (Potion)Copy(Potion.class, Game.instance.player.shuffledPotions.get(Game.rand.nextInt(Game.instance.player.shuffledPotions.size)));
	}
	
	public Food GetRandomFood() {
		if(Game.instance.player == null || food == null || food.size == 0) return null;
		
		int r = random.nextInt(food.size);
		return (Food)Copy( Food.class, food.get(r) );
	}
	
	public Item GetRandomJunk() {
		if(junk == null || junk.size == 0) return null;
		int r = random.nextInt(junk.size);
		return Copy( Item.class, junk.get(r));
	}
	
	public Scroll GetRandomScroll() {
		if(scrolls == null || scrolls.size == 0) return null;
		
		int r = random.nextInt(scrolls.size);
		return (Scroll)Copy( Scroll.class, scrolls.get(r) );
	}
	
	public Entity GetRandomDecoration() {
		if(decorations == null || decorations.size == 0) return null;
		
		int r = random.nextInt(decorations.size);
		return Copy( Entity.class, decorations.get(r) );
	}

	public Item GetWeapon(Integer level, int num) {
		if( !melee.containsKey(level.toString()) ) return null;
		return Copy( Sword.class, melee.get(level.toString()).get(num) );
	}
	
	public Item GetArmor(Integer level, int num) {
		if( !armor.containsKey(level.toString()) ) return null;
		return Copy( Armor.class, armor.get(level.toString()).get(num) );
	}
	
	public Item GetLevelLoot(Integer level) {
		int num = (int)(random.nextDouble() * 100);
		Item itm = null;

		Stats s = Game.instance.player.stats;
		float totalPlayerStatPoints = s.ATK + s.DEF + s.DEX + s.END + s.MAG + s.SPD;

		int chanceForMeleeItem = (int)(s.ATK / totalPlayerStatPoints * 100);
		int chanceForRangedItem = (int)(chanceForMeleeItem + (s.DEX / totalPlayerStatPoints * 100));
		int chanceForMagicItem = (int)(chanceForRangedItem + (s.MAG / totalPlayerStatPoints * 100));
		int chanceForArmorItem = (int)(chanceForMagicItem + (s.DEF / totalPlayerStatPoints * 100));
		int chanceForFoodItem = (int)(chanceForArmorItem + (s.END / totalPlayerStatPoints * 100));
		int chanceForPotionItem = (int)(chanceForFoodItem + (s.SPD / totalPlayerStatPoints * 100));

		// ATK
		if(num < chanceForMeleeItem) {
			itm = Game.GetItemManager().GetRandomWeapon(level);
		}
		// DEX
		else if (num < chanceForRangedItem) {
			itm = Game.GetItemManager().GetRandomRangedWeapon(level);
		}
		// MAG
		else if (num < chanceForMagicItem) {
			if(random.nextDouble() < 0.6) {
				itm = Game.GetItemManager().GetRandomWand();
			}
			else {
				itm = Game.GetItemManager().GetRandomScroll();
			}
		}
		// DEF
		else if (num < chanceForArmorItem) {
			itm = Game.GetItemManager().GetRandomArmor(level);
		}
		// END
		else if (num < chanceForFoodItem) {
			itm = Game.GetItemManager().GetRandomFood();
		}
		// SPD
		else if (num < chanceForPotionItem) {
			itm = Game.GetItemManager().GetRandomPotion();
		}
		else {
			itm = Game.GetItemManager().GetRandomJunk();
		}
		
		return itm;
	}
	
	public Item GetMonsterLoot(Integer level, boolean canSpawnGold) {

		// Make unique items more common as levels get higher
		// TODO: Replace hard coded value 6.0f with how many dungeon levels are present.
		//       My concern here are custom player levels may skew the drop rate.
		float uniqueChanceMod = Math.min(1.0f, level / 6.0f);

        // Unique items
        if(random.nextFloat() >= 1.0f - (0.02f * uniqueChanceMod)) {
            Item unique = Game.GetItemManager().GetUniqueItem(level, Game.instance.progression);
            if(unique != null) return unique;
        }

		// 50% chance of just being gold
		if(canSpawnGold && random.nextBoolean()) {
			if(random.nextFloat() < 0.5f) {
				return new Gold(Game.rand.nextInt(level * 5) + 1);
			}
		}
		
		return GetLevelLoot(level);
	}
	
	public static Item Copy(Class<?> type, Item tocopy)
	{
		return (Item) KryoSerializer.copyObject(tocopy);
	}
	
	public ItemModification Copy(Class<?> type, ItemModification tocopy)
	{
		return (ItemModification) KryoSerializer.copyObject(tocopy);
	}

    public void merge(ItemManager otherItemManager) {
        // merge weapons
        if(otherItemManager.melee != null) {
            for(Map.Entry<String,Array<Sword>> entry  : otherItemManager.melee.entrySet()) {
                if(melee.containsKey(entry.getKey())) {
                    for(Sword s : entry.getValue()) {
                        mergeItem(melee.get(entry.getKey()), s);
                    }
                }
                else {
                    melee.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // merge armor
        if(otherItemManager.armor != null) {
            for(Map.Entry<String,Array<Armor>> entry  : otherItemManager.armor.entrySet()) {
                if(armor.containsKey(entry.getKey())) {
                    for(Armor a : entry.getValue()) {
                        mergeItem(armor.get(entry.getKey()), a);
                    }
                }
                else {
                    armor.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // merge ranged
        if(otherItemManager.ranged != null) {
            for(Map.Entry<String,Array<Item>> entry  : otherItemManager.ranged.entrySet()) {
                if(ranged.containsKey(entry.getKey())) {
                    for(Item i : entry.getValue()) {
                        mergeItem(ranged.get(entry.getKey()), i);
                    }
                }
                else {
                    ranged.put(entry.getKey(), entry.getValue());
                }
            }
        }

        mergeLists(wands, otherItemManager.wands);
        mergeLists(potions, otherItemManager.potions);
        mergeLists(food, otherItemManager.food);
        mergeLists(scrolls, otherItemManager.scrolls);
        mergeLists(decorations, otherItemManager.decorations);
        mergeLists(junk, otherItemManager.junk);
        mergeLists(unique, otherItemManager.unique);

        if(otherItemManager.weaponEnchantments != null) weaponEnchantments.addAll(otherItemManager.weaponEnchantments);
		if(otherItemManager.weaponPrefixEnchantments != null) weaponPrefixEnchantments.addAll(otherItemManager.weaponPrefixEnchantments);
		if(otherItemManager.armorEnchantments!= null) armorEnchantments.addAll(otherItemManager.armorEnchantments);
		if(otherItemManager.armorPrefixEnchantments!= null) armorPrefixEnchantments.addAll(otherItemManager.armorPrefixEnchantments);
	}

    // add or replace things from the newlist into the existing list
    public void mergeLists(Array existinglist, Array newlist) {
        if(existinglist != null && newlist != null) {
            for (Object i : newlist) {
                mergeItem(existinglist, (Item)i);
            }
        }
    }

    // If an item name already exists that is the same as the new one, replace it. Otherwise add it.
    public void mergeItem(Array bucket, Item item) {
        int foundIndex = -1;
        for(int i = 0; i < bucket.size && foundIndex == -1; i++) {
            Item ii = (Item)bucket.get(i);

            if(ii.name != null && ii.name.equals(item.name)) {
                foundIndex = i;
            }
        }

        // if this already exists, replace it. Otherwise just add it
        if(foundIndex != -1)
            bucket.set(foundIndex, item);
        else
            bucket.add(item);
    }

    // keep track of all items by their name
    public void IndexItemsByName(Array array) {
    	for(int i = 0; i < array.size; i++) {
    		Object e = array.get(i);
			if(e instanceof Item) {
				Item item = (Item)e;
				if(item.name != null)
					itemsByName.put(item.name.toLowerCase(), item);
			}
		}
	}

    // Look up items by name
	public Item FindItem(String itemName, Item.ItemCondition itemCondition) {

		if(itemsByName == null) {
			itemsByName = new HashMap<String, Item>();

			for(Array list : melee.values()) {
				IndexItemsByName(list);
			}
			for(Array list : armor.values()) {
				IndexItemsByName(list);
			}
			for(Array list : ranged.values()) {
				IndexItemsByName(list);
			}

			IndexItemsByName(unique);
			IndexItemsByName(wands);
			IndexItemsByName(potions);
			IndexItemsByName(food);
			IndexItemsByName(scrolls);
			IndexItemsByName(decorations);
			IndexItemsByName(junk);
		}

		Item item = itemsByName.get(itemName.toLowerCase());
		if(item != null) {
			Item newItem = Copy(Item.class, item);
			newItem.itemCondition = itemCondition;
			return newItem;
		}

		return null;
	}
}
