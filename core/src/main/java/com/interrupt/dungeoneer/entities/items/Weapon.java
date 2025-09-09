package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;
import java.util.Random;

public class Weapon extends Item {
	public enum DamageType { PHYSICAL, MAGIC, FIRE, ICE, LIGHTNING, POISON, HEALING, PARALYZE, VAMPIRE }

	/** Base modifiers */
	public ItemModification baseMods;

	/** Damage type */
	@EditorProperty
	public DamageType damageType = DamageType.PHYSICAL;

	public static String damageTypeToString(DamageType dType) {
		return StringManager.get("items.Weapon.damageType." + dType.toString().toUpperCase());
	}

	public Weapon() { }

	/** Base amount of damage to deal */
	@EditorProperty
	protected int baseDamage = 2;

	/** Random amount of damage to deal */
	@EditorProperty
	protected int randDamage = 2;

	/** Strength of knockback effect */
	@EditorProperty
	public float knockback = 0.8f;

	/** Distance which weapon can hit */
	@EditorProperty
	public float reach = 0.5f;

	/** Length of attack animation */
	@EditorProperty
	public float speed = 0.5f;

	@EditorProperty
	public float chargespeed = 1;

	/** Name of animation to play for a standard attack */
	@EditorProperty
	public String attackAnimation = null;

	/** Name of animation to play for a charged attack */
	@EditorProperty
	public String attackStrongAnimation = null;

	/** Name of animation to play for charging windup */
	@EditorProperty
	public String chargeAnimation = null;

	/** Requires two hands? */
	@EditorProperty
	public boolean twoHanded = false;

	/** How durable weapon is */
	@EditorProperty
	public int durability = 25;
	private int currentDurability = 0;

	/** Can perform a charged attack? */
	@EditorProperty
	public boolean chargesAttack = true;

	/** Sprite index of item while held */
	@EditorProperty( group = "Visual", type = "SPRITE_ATLAS_NUM" )
	public int brokenTex = -1;

	public Weapon(float x, float y, int tex, ItemType itemType, String name) {
		super(x, y, tex, itemType, name);
	}

	public int doAttackRoll(float attackPower, Player p)
	{
		Random r = new Random();
		int dmg = (int)(getBaseDamage() * attackPower);
		dmg += r.nextInt(getRandDamage() + 1 + p.getDamageStatBoost());

		dmg += getElementalDamage();

		wasUsed();

		return Math.max(dmg, 1);
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

		// TODO: Audio and particle cues for weapon break
        if(this.brokenTex >= 0) {
            this.tex = this.brokenTex;
            Game.hudManager.quickSlots.refresh();
        }

		this.reach = this.reach * 0.75f;
	}

	@Override
	public String GetItemText() {
		int dmg = getBaseDamage();
		int rdmg = getRandDamage();

		String dmgType = Weapon.damageTypeToString(this.damageType);
		//if(damageType == DamageType.PHYSICAL) dmgType = "DMG";

		String infoText = MessageFormat.format(StringManager.get("items.Weapon.damageRangeText"), dmg, (dmg + rdmg), dmgType.toLowerCase());
		if(getElementalDamage() > 0)
			infoText += "\n" + MessageFormat.format(StringManager.get("items.Weapon.elementalDamageText"), getElementalDamage(), Weapon.damageTypeToString(getDamageType()));

		if(twoHanded) infoText += "\n"+ StringManager.get("items.Weapon.twoHandedText");

		return infoText;
	}

	public int getBaseDamage() {
		int dmgMod = 0;
		if(this.itemCondition != null) dmgMod = (this.itemCondition.ordinal() * 2) - 4;

		for(ItemModification enchantment : getEnchantments()) {
			if((enchantment.damageType == null || enchantment.damageType == DamageType.PHYSICAL)) dmgMod += enchantment.damageMod;
		}

		// item scaling
        if(itemLevel > 1)
		    dmgMod += (itemLevel * 0.75f);

		return Math.max(1, baseDamage + dmgMod);
	}

	public int getElementalDamage() {
		if(!identified || enchantment == null || enchantment.damageType == DamageType.PHYSICAL) return 0;
		return enchantment.damageMod;
	}

	public int getRandDamage() {
		int dmgMod = 0;

        // item scaling
        if(itemLevel > 1)
            dmgMod += (itemLevel * 0.75f);

		return Math.max(1, randDamage + dmgMod);
	}

	public float getSpeed() {
		return speed;
	}

	public float getChargeSpeed() {
		return speed;
	}

	public DamageType getDamageType() {
		if(enchantment != null) return enchantment.damageType;
		return damageType;
	}

	public Color getEnchantmentColor() {
		return Weapon.getEnchantmentColor(this.getDamageType());
	}

	public static Color getEnchantmentColor(DamageType damageType) {
		switch (damageType) {
			case FIRE:
				return Colors.FIRE;

			case ICE:
				return Colors.ICE;

			case LIGHTNING:
				return Colors.LIGHTNING;

			case MAGIC:
				return Colors.MAGIC;

			case POISON:
				return Colors.POISON;

			case PARALYZE:
				return Colors.PARALYZE;

			case HEALING:
				return Colors.HEALING;

			case VAMPIRE:
				return Colors.VAMPIRE;

			default:
				return Colors.MUNDANE;
		}
	}

	private static transient Vector3 rayLevelIntersection = new Vector3();
	public Vector3 getCrosshairDirection(float zOffset) {
        Ray ray = Game.camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        boolean hitLevel = Collidor.intersectRayTriangles(ray, GameManager.renderer.GetCollisionTrianglesAlong(ray, 20f), rayLevelIntersection, null);
        if(hitLevel) {
            Vector3 dir = rayLevelIntersection;
            Vector3 start = new Vector3(x, z + zOffset, y);
            dir = dir.sub(start).nor();
            return dir;
        }
        return null;
    }

	// override this
	public void doAttack(Player p, Level lvl, float attackPower) { }

	// and this
	public void tickAttack(Player p, Level lvl, float time) { }

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
			DamageType damageType = getDamageType();
			if(baseMods == null || enchantments.size > 1) {
				shader = "magic-item";
			}

			if(damageType == DamageType.FIRE) {
				shader = "magic-item-red";
			}
			else if(damageType == DamageType.LIGHTNING) {
				shader = "magic-item-white";
			}
			else if(damageType == DamageType.MAGIC) {
				shader = "magic-item-purple";
			}
			else if(damageType == DamageType.POISON) {
				shader = "magic-item-green";
			}
			else if(damageType == DamageType.VAMPIRE) {
				shader = "magic-item-red";
			}
		}

		return shader;
	}
}
