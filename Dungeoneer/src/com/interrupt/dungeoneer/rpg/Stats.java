package com.interrupt.dungeoneer.rpg;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Armor;
import com.interrupt.dungeoneer.entities.items.ItemModification;

public class Stats {
	
	public Stats() { }
	
	public Stats(int ATK, int DEF, int DEX, int SPD, int MAG, int END) {
		this.ATK = ATK;
		this.DEF = DEF;
		this.DEX = DEX;
		this.SPD = SPD;
		this.MAG = MAG;
		this.END = END;
	}

	/** Attack stat. */
	public int ATK = 4;

	/** Defense stat. */
	public int DEF = 4;

	/** Dexterity stat. */
	public int DEX = 4;

	/** Speed stat. */
	public int SPD = 4;

	/** Magic stat. */
	public int MAG = 4;

	/** Endurance stat. */
	public int END = 4;

	/** Healthpoints stat. */
	public int HP = 8;

	/** Attack speed modifier. */
	public float attackSpeedMod = 0f;

	/** Knockback modifier. */
	public float knockbackMod = 0f;

	/** Magic resistance modifier. */
	public float magicResistMod = 0f;

	private transient int LAST_ATK;
	private transient int LAST_DEF;
	private transient int LAST_DEX;
	private transient int LAST_SPD;
	private transient int LAST_MAG;
	private transient int LAST_END;
	private transient int LAST_HP;

	private transient float LAST_attackSpeedMod;
	private transient float LAST_knockbackMod;
	private transient float LAST_magicResistMod;
	
	public void ResetStats() {

		LAST_ATK = ATK;
		LAST_DEF = DEF;
		LAST_DEX = DEX;
		LAST_SPD = SPD;
		LAST_MAG = MAG;
		LAST_END = END;
		LAST_HP = HP;

		LAST_attackSpeedMod = attackSpeedMod;
		LAST_knockbackMod = knockbackMod;
		LAST_magicResistMod = magicResistMod;

		ATK = 0;
		DEF = 0;
		DEX = 0;
		SPD = 0;
		MAG = 0;
		END = 0;
		HP = 0;

		attackSpeedMod = 0;
		knockbackMod = 0;
		magicResistMod = 0;
	}
	
	public void Recalculate(Player player) {
		ResetStats();
		boolean holdingTwoHanded = player.isHoldingTwoHanded();
		
		for(Item item : player.equippedItems.values()) {
			// Offhand items should be skipped when the player is holding a two handed weapon
			if(item == null) continue;
			if((item.equipLoc != null && item.equipLoc.equals("OFFHAND")) && holdingTwoHanded) continue;
			addItemStats(item);
		}

		addItemStats(player.GetHeldItem());
	}

	public void addItemStats(Item item) {
		if(item == null) return;

		for(ItemModification m : item.getEnchantments()) {
			addItemStats(item, m);
		}

		if(item instanceof Armor) {
			Armor armor = (Armor)item;
			DEF += armor.GetArmor();
			addItemStats(armor, armor.baseMods);
		}
	}

	public void addItemStats(Item i, ItemModification m) {
		if(m == null) return;
		HP += m.getHpMod(i);
		SPD += m.getMoveSpeedMod(i);
		MAG += m.getMagicMod(i);
		DEX += m.getAgilityMod(i);
		ATK += m.getAttackMod(i);
		DEF += m.getArmorMod(i);
		attackSpeedMod += m.getAttackSpeedMod(i);
		knockbackMod += m.getKnockbackMod(i);
		magicResistMod += m.getMagicResistMod(i);
	}

	public boolean statsChanged() {
		return (ATK != LAST_ATK || DEF != LAST_DEF || DEX != LAST_DEX || SPD != LAST_SPD || MAG != LAST_MAG || END != LAST_END || HP != LAST_HP || attackSpeedMod != LAST_attackSpeedMod || knockbackMod != LAST_knockbackMod || magicResistMod != LAST_magicResistMod);
	}
}
