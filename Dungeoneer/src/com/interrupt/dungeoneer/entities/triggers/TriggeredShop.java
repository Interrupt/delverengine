package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.items.BagUpgrade;
import com.interrupt.dungeoneer.entities.items.Elixer;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.overlays.MessageOverlay;
import com.interrupt.dungeoneer.overlays.Overlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.overlays.ShopOverlay;
import com.interrupt.dungeoneer.rpg.Stats;
import com.interrupt.helpers.ShopItem;
import com.interrupt.helpers.Upgrade;
import com.interrupt.managers.StringManager;

public class TriggeredShop extends Trigger {
	
	public enum ShopType { upgrades, scrolls, potions, weapons, wands, armor, persistent }

	@EditorProperty
	public String messageFile = null;
	
	@EditorProperty
	public ShopType shopType = ShopType.upgrades;
	
	@EditorProperty
	public String title = StringManager.get("triggers.TriggeredShop.titleText");
	
	@EditorProperty
	public String description = StringManager.get("triggers.TriggeredShop.descriptionText");

	@EditorProperty
	public boolean pausesGame = false;
	
	public Array<ShopItem> items = null;

	public TriggeredShop() { hidden = true; spriteAtlas = "editor"; tex = 16; isSolid = true; }

	private Integer messageProgression = null;

	@Override
	public void init(Level level, Level.Source source) {
		if(source == Level.Source.LEVEL_START) {
			if(messageFile != null && messageFile.contains(",")) {
				String[] messages = messageFile.split(",");

				Integer lastShown = null;
				if(title != null) {
					lastShown = Game.instance.progression.messagesSeen.get(title);
				}

				if(lastShown == null) {
					lastShown = -1;
				}
				int toShow = lastShown + 1;

				toShow = Math.min(toShow, messages.length - 1);

				messageProgression = toShow;
				messageFile = messages[toShow];
			}
		}

		super.init(level, source);
	}
	
	@Override
	public void doTriggerEvent(String value) {

		// We saw this, do we need to update the progression?
		if(messageProgression != null && title != null) {
			Game.instance.progression.messagesSeen.put(title, messageProgression);
		}

		if(messageFile != null && !messageFile.equals("")) {
			final MessageOverlay message = new MessageOverlay(messageFile, Game.instance.player, null, null);
			message.pausesGame = pausesGame;

			message.afterAction = new Action() {
				@Override
				public boolean act(float delta) {
					showShopOverlay(message);
					return true;
				}
			};

			OverlayManager.instance.push(message);
		}
		else {
			showShopOverlay(null);
		}
		
		super.doTriggerEvent(value);
	}

	public void showShopOverlay(Overlay previousOverlay) {
		if(items == null) {
			items = new Array<ShopItem>();
			if(shopType == ShopType.upgrades) {
				items.add(new ShopItem(Game.instance.itemManager.GetRandomRangedWeapon(7, Item.ItemCondition.normal), "SHOP_JOFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomRangedWeapon(7, Item.ItemCondition.normal), "SHOP_JOFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWeapon(7, Item.ItemCondition.normal), "SHOP_JOFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomArmor(7, Item.ItemCondition.normal), "SHOP_JOFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWand(), "SHOP_WANDS"));
				items.add(new ShopItem(new Elixer(), "SHOP_JOFF_ELIXER"));
			}
			else if(shopType == ShopType.scrolls) {
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll(), "SHOP_MAGIC"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll(), "SHOP_MAGIC"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll(), "SHOP_MAGIC"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll(), "SHOP_MAGIC"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll(), "SHOP_MAGIC"));
			}
			else if(shopType == ShopType.potions) {
				items.add(new ShopItem(Game.instance.itemManager.GetRandomPotion(), "SHOP_POTIONS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomPotion(), "SHOP_POTIONS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomPotion(), "SHOP_POTIONS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomPotion(), "SHOP_POTIONS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomPotion(), "SHOP_POTIONS"));
			}
			else if(shopType == ShopType.wands) {
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWand(), "SHOP_WANDS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWand(), "SHOP_WANDS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWand(), "SHOP_WANDS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWand(), "SHOP_WANDS"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWand(), "SHOP_WANDS"));
			}
			else if(shopType == ShopType.weapons) {
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWeapon(7),"SHOP_JEFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomWeapon(7),"SHOP_JEFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomRangedWeapon(7),"SHOP_JEFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomRangedWeapon(7),"SHOP_JEFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomArmor(7),"SHOP_JEFF"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomArmor(7),"SHOP_JEFF"));
			}
			else if(shopType == ShopType.armor) {
				items.add(new ShopItem(Game.instance.itemManager.GetRandomArmor(7),"SHOP_ARMOR"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomArmor(7),"SHOP_ARMOR"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomArmor(7),"SHOP_ARMOR"));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomArmor(7),"SHOP_ARMOR"));
			}
		}

		if(shopType == ShopType.persistent) {
			if(items == null) items = new Array<ShopItem>();
			else items.clear();

			if(Game.instance.player.canAddInventorySlot()) {
				BagUpgrade inventoryUpgrade = new BagUpgrade(BagUpgrade.BagUpgradeType.INVENTORY, true);
				inventoryUpgrade.name = "Soulbound Bag Expansion";
				inventoryUpgrade.cost = 30;
				inventoryUpgrade.cost += (Game.instance.progression.inventoryUpgrades * Game.instance.progression.inventoryUpgrades) * (int) (inventoryUpgrade.cost * 0.75f);
				items.add(new ShopItem(inventoryUpgrade, true,"SQUID2"));
			}
			if(Game.instance.player.canAddHotbarSlot()) {
				BagUpgrade hotbarUpgrade = new BagUpgrade(BagUpgrade.BagUpgradeType.HOTBAR, true);
				hotbarUpgrade.name = "Soulbound Belt Expansion";
				hotbarUpgrade.cost = 60;
				hotbarUpgrade.cost += (Game.instance.progression.hotbarUpgrades * Game.instance.progression.hotbarUpgrades) * (int) (hotbarUpgrade.cost);
				items.add(new ShopItem(hotbarUpgrade, true, "SQUID1"));
			}

			// No upgrades, sell scrolls instead
			if(items.size == 0) {
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll()));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll()));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll()));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll()));
				items.add(new ShopItem(Game.instance.itemManager.GetRandomScroll()));
			}
		}

		Array<ShopItem> toRemove = new Array<ShopItem>();
		for(ShopItem item : items) {
			if(item.item == null && item.upgrade == null) toRemove.add(item);
		}
		items.removeAll(toRemove, true);

		if(previousOverlay != null) {
			ShopOverlay shopOverlay = new ShopOverlay(Game.instance.player, null, null, items);
			shopOverlay.pausesGame = pausesGame;
			shopOverlay.timer = 1000f;
			OverlayManager.instance.replace(previousOverlay, shopOverlay);
		}
		else {
			ShopOverlay shopOverlay = new ShopOverlay(Game.instance.player, title, description, items);
			shopOverlay.pausesGame = pausesGame;
			OverlayManager.instance.push(shopOverlay);
		}
	}
}
