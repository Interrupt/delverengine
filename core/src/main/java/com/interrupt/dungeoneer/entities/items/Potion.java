package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.statuseffects.*;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;
import java.util.Random;

public class Potion extends Item {

	public enum PotionType { health, magic, maxhealth, poison, restore, shield, paralyze }

	/** Potion type. */
	@EditorProperty
	public PotionType potionType = PotionType.health;

	public Potion() { }

	public Potion(float x, float y) {
		super(x, y, 0, ItemType.potion, StringManager.get("items.Potion.defaultNameText"));
	}

	public void Drink(Player player) {
		Random r = new Random();

		player.history.drankPotion(this);
		Audio.playSound("cons_drink.mp3", 0.5f);


		String displayText = "";

		if(potionType == PotionType.health) {
			displayText = StringManager.get("items.Potion.healDisplayText");
			player.hp += r.nextInt(5) + 4;
			if(player.hp > player.getMaxHp()) player.hp = player.getMaxHp();
		}
		else if(potionType == PotionType.poison) {
			displayText = StringManager.get("items.Potion.poisonDisplayText");
		}
		else if(potionType == PotionType.maxhealth) {
			displayText = StringManager.get("items.Potion.maxHealDisplayText");
			player.hp = player.getMaxHp();
			player.clearStatusEffects();
		}
		else if(potionType == PotionType.restore) {
			player.hp += 1;
			if(player.hp > player.getMaxHp()) player.hp = player.getMaxHp();
		}

		StatusEffect e = getStatusEffect();
		if(e != null) {
			player.addStatusEffect(e);
		}

		// remove from the inventory
		int location = player.inventory.indexOf(this, true);
		player.inventory.set(location, null);
		Game.RefreshUI();

		// maybe add to discovered list
		if(Game.rand.nextFloat() > 0.5f && !player.discoveredPotions.contains(potionType, true)) {
			player.discoveredPotions.add(potionType);
			player.history.identified(this);
			Game.ShowMessage(displayText + "\n" + MessageFormat.format(StringManager.get("items.Potion.discoverDisplayText"), GetIdentifiedName()), 1.5f, 1f);
		}
		else {
			Game.ShowMessage(displayText, 1.5f, 1f);
		}
	}

	public boolean inventoryUse(Player player){
		Drink(player);
        return true;
	}

	public String GetInfoText() {
		if(Game.instance.player.discoveredPotions.contains(potionType, true)) {
			return GetIdentifiedName();
		}
		return StringManager.get("items.Potion.unidentifiedInfoText");
	}

	public String GetIdentifiedName() {
		if(potionType == PotionType.health)
			return StringManager.get("items.Potion.healingNameText");
		else if(potionType == PotionType.poison)
			return StringManager.get("items.Potion.poisonNameText");
		else if(potionType == PotionType.maxhealth)
			return StringManager.get("items.Potion.restorationNameText");
		else if(potionType == PotionType.restore)
			return StringManager.get("items.Potion.colaNameText");
		else if(potionType == PotionType.magic)
			return StringManager.get("items.Potion.magicShieldNameText");
		else if(potionType == PotionType.shield)
			return StringManager.get("items.Potion.ironSkinNameText");
		else if(potionType == PotionType.paralyze)
			return StringManager.get("items.Potion.paralyzeNameText");
		else
			return StringManager.get("items.Potion.unknownNameText");
	}

	@Override
	public void hitWorld(float xSpeed, float ySpeed, float zSpeed) {
		super.hitWorld(xSpeed, ySpeed, zSpeed);

		if(Math.abs(xSpeed) > 0.02f || Math.abs(ySpeed) > 0.02f || Math.abs(zSpeed) > 0.2f) {
			activateExplosion(false);
		}
	}

	@Override
	public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator) {
		super.hit(projx, projy, damage, knockback, damageType, instigator);
		activateExplosion(false);
	}

	@Override
	public void applyPhysicsImpulse(Vector3 impulse) {
		super.applyPhysicsImpulse(impulse);
		if(impulse.len() > 0.02f)
			activateExplosion(false);
	}

	@Override
	public void encroached(Entity hit) {
		if(hit instanceof Actor) {
			activateExplosion(true);
		}
	}

	public float getExplosionDamageAmount(){
		switch (potionType) {
			case poison:
				return 3f;
			case paralyze:
				return 1f;
			case shield:
				return 6f;
			case maxhealth:
				return 24f;
			default:
				return 8f;
		}
	}

	public DamageType getExplosionDamageType(){
		switch (potionType) {
			case poison:
				return DamageType.POISON;
			case paralyze:
				return DamageType.PARALYZE;
			case shield:
				return DamageType.LIGHTNING;
			case health:
			case maxhealth:
			case restore:
				return DamageType.HEALING;
			default:
				return DamageType.MAGIC;
		}
	}

	public Color getExplosionColor() {
		switch(potionType) {
			case poison:
				return Colors.POISON;
			case paralyze:
				return Colors.PARALYZE;
			case shield:
				return Colors.LIGHTNING;
			case health:
			case maxhealth:
			case restore:
				return Colors.HEALING;
			default:
				return Colors.MAGIC;
		}
	}

	public StatusEffect getStatusEffect() {
		if(potionType == PotionType.poison) {
			return new PoisonEffect(1600, 160, 1, false);
		}
		else if(potionType == PotionType.restore) {
			return new RestoreHealthEffect(1600, 160, 1);
		}
		else if(potionType == PotionType.magic) {
			return new ShieldEffect(StringManager.get("items.Potion.magicShieldStatusEffectNameText"), 0.5f, 0.1f, 1000);
		}
		else if(potionType == PotionType.shield) {
			StatusEffect shield = new StatusEffect();
			shield.name = StringManager.get("items.Potion.ironSkinStatusEffectNameText");
			shield.damageMod = 0.5f;
			shield.timer = 1000;
			return shield;
		}
		else if(potionType == PotionType.paralyze) {
			return new ParalyzeEffect(1000);
		}

		return null;
	}

	public Bomb activateExplosion(boolean immediate) {
		if(isActive) {
			// uhoh!
			Bomb bomb = new Bomb();
			bomb.matchEntity(this);
			bomb.explosionDamageType = getExplosionDamageType();
			bomb.explosionColor = new Color(getExplosionColor());
			bomb.explosionDamage = getExplosionDamageAmount();
			bomb.ignorePlayerCollision = ignorePlayerCollision;
			bomb.applyStatusEffect = getStatusEffect();
			if(immediate) bomb.countdownTimer = 0;

			Game.GetLevel().entities.add(bomb);

			Audio.playPositionedSound("potions/sfx_boil.mp3", new Vector3(x,y,z), 0.6f, 12f);

			this.isActive = false;

			return bomb;
		}
		return null;
	}

	@Override
	public void tossItem(Level level, float attackPower) {
		if (attackPower > 0.5f) {
			this.activateExplosion(false);
		}
	}

	@Override
	public void doPickup(Player player) {
		super.doPickup(player);
		Drink(player);
	}
}
