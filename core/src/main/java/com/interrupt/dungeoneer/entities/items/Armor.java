package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;

public class Armor extends Item {
    /** Base modifiers */
	public ItemModification baseMods;

	/** Armor point value. */
	@EditorProperty
	public int armor;

	/** How durable armor is. */
	@EditorProperty
	public int durability = 25;
	private int currentDurability = 0;

	public String set = null;
	
	public Armor() { equipLoc = "ARMOR"; isSolid = true; yOffset = -0.1f; equipSound = "/ui/ui_equip_armor.mp3"; }
	
	public Armor(int ac, String equipLoc, int tex)
	{
		armor = ac;
		this.equipLoc = equipLoc;
		this.tex = tex;
	}

	public Armor(float x, float y) {
		super(x, y, 24, ItemType.armor, "ARMOR");
		armor = 1;
		equipLoc = "ARMOR";
	}

	@Override
	public String GetItemText() {
		if(armor > 0) return GetArmor() + " AC";
		else return "";
	}

	public int GetArmor() {
		int mod = 0;
		if(enchantment != null) mod += enchantment.getArmorMod(this);
		if(baseMods != null) mod += baseMods.getArmorMod(this);

		if(identified) {
			for (ItemModification enchantment : getEnchantments()) {
				mod += enchantment.getArmorMod(this);
			}
		}

		// item scaling
        if(itemLevel > 1)
		    mod += (itemLevel * 0.75f);
		
		if(this.itemCondition != null) mod += (this.itemCondition.ordinal()) - 2;
		return Math.max(0, armor + mod);
	}
	
	public boolean inventoryUse(Player player){
		player.equip(this);
        return true;
	}

	// reduce durability over time when used
	private static Array<ItemCondition> conditions = new Array<ItemCondition>(ItemCondition.values());
	public void wasUsed() {
		currentDurability++;
		if(currentDurability > durability) {
			currentDurability = 0;
			int currentConditionIndex = conditions.indexOf(itemCondition, true);
			if(currentConditionIndex > 0) {
				itemCondition = conditions.get(currentConditionIndex - 1);

				if (this.itemCondition == ItemCondition.broken) {
					this.onBroken();
				}
			}
		}
	}

	public void onBroken() {
		this.enchantment = null;
		this.prefixEnchantment = null;
	}
	
	public void equipEvent() {
		
	}

	@Override
	public Array<ItemModification> getEnchantments() {
		t_enchantments.clear();
		if(!identified) return t_enchantments;
		if(prefixEnchantment != null) t_enchantments.add(prefixEnchantment);
		if(enchantment != null) t_enchantments.add(enchantment);
		if(baseMods != null) t_enchantments.add(baseMods);
		return t_enchantments;
	}

	@Override
	public String getShader() {
		Array<ItemModification> enchantments = getEnchantments();
		if(shader == null && enchantments.size > 0) {
			if(baseMods == null || enchantments.size > 1) {
				shader = "magic-item";
			}
		}

		return shader;
	}
}
