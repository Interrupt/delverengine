package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;

public class ItemModification {
    /** Name of ItemModification */
	public String name;

	/** Health points modifier */
	protected int hpMod = 0;

    protected int mpMod = 0;

    /** Armor points modifier */
    protected int armorMod = 0;

    /** Move speed modifier */
    protected int moveSpeedMod = 0;

    /** Agility modifier */
    protected int agilityMod = 0;

    /** Magic modifier */
    protected int magicMod = 0;

    /** Attack modifier */
    protected int attackMod = 0;

    /** Attack speed modifier */
    protected float attackSpeedMod = 0;

    /** Damage modifier */
    protected int damageMod = 0;

    /** Knockback modifier */
    protected float knockbackMod = 0;

    /** Magic resistance modifier */
    protected float magicResistMod = 0f;

    protected int fireResist = 0;
    protected int iceResist = 0;
    protected int lightningResist = 0;
    protected int magicResist = 0;
    protected int physicalResist = 0;

    /** Damage type */
	public DamageType damageType = DamageType.PHYSICAL;
	
	public ItemModification() { }
	public ItemModification(String name) { this.name = name; }

	public int getHpMod(Item owner) {
	    return hpMod + (int)(hpMod * owner.itemLevel * 0.5f);
    }

    public int getMpMod(Item owner) {
	    return mpMod + (int)(mpMod * owner.itemLevel * 0.5f);
    }

    public int getArmorMod(Item owner) {
        return armorMod + (int)(armorMod * owner.itemLevel * 0.5f);
    }

    public int getMoveSpeedMod(Item owner) {
	    return moveSpeedMod;
    }

    public int getAgilityMod(Item owner) {
	    return agilityMod + (int)(agilityMod * owner.itemLevel * 0.5f);
    }

    public int getMagicMod(Item owner) {
	    return magicMod + (int)(magicMod * owner.itemLevel * 0.5f);
    }

    public int getAttackMod(Item owner) {
	    return attackMod + (int)(attackMod * owner.itemLevel * 0.5f);
    }

    public float getAttackSpeedMod(Item owner) {
	    return attackSpeedMod + (attackSpeedMod * owner.itemLevel * 0.05f);
    }

    public int getDamageMod(Item owner) {
	    return damageMod + (int)(damageMod * owner.itemLevel * 0.5f);
    }

    public float getKnockbackMod(Item owner) {
	    return knockbackMod + (knockbackMod * owner.itemLevel * 0.05f);
    }

    public void increaseAttackMod(int value) {
	    attackMod += value;
    }

    public void increaseArmorMod(int value) {
	    armorMod += value;
    }

    public float getMagicResistMod(Item owner) {
        return magicResistMod + (magicResistMod * owner.itemLevel * 0.05f);
    }

    public int getCostMod() {
	    int costMod = 0;
	    costMod += Math.min(hpMod * 10, 0);
        costMod += Math.min(mpMod * 10, 0);
        costMod += Math.min(armorMod * 10, 0);
        costMod += Math.min(moveSpeedMod * 10, 0);
        costMod += Math.min(agilityMod * 10, 0);
        costMod += Math.min(magicMod * 10, 0);
        costMod += Math.min(attackMod * 10, 0);
        costMod += Math.min(10 / attackSpeedMod, 0);
        costMod += Math.min(damageMod, 0);
        costMod += Math.min(25 * knockbackMod, 0);
        costMod += Math.min(1000 * magicResistMod, 0);

        if(damageType != DamageType.PHYSICAL) {
            costMod += 40;
        }

        // put a sane cap on things
        if(costMod > 3000)
            costMod = 3000;

	    return costMod;
    }
}
