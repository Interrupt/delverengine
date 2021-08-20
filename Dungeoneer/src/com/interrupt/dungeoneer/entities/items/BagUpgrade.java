package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class BagUpgrade extends Item {
    public BagUpgrade() { name = "Expanded Bag"; tex = 58; cost = 40; unique = true; }

    public BagUpgrade(BagUpgradeType bagUpgradeType, boolean persistentUpgrade) {
        name = "Expanded Bag";
        tex = 58;
        unique = true;
        this.persistentUpgrade = persistentUpgrade;
        this.bagUpgradeType = bagUpgradeType;
    }

    public enum BagUpgradeType { INVENTORY, HOTBAR }

    @EditorProperty
    public BagUpgradeType bagUpgradeType = BagUpgradeType.INVENTORY;

    @EditorProperty
    public boolean persistentUpgrade = false;

    public boolean inventoryUse(Player player){
        UpgradeInventory(player);
        return true;
    }

    public void UpgradeInventory(Player player) {
        try {
            if (bagUpgradeType == BagUpgradeType.HOTBAR) {
                player.addHotbarSlot();

                String upgradeStr = StringManager.get("message.beltupgrade");

                if (persistentUpgrade) {
                    Game.instance.progression.addHotbarSlot();
                    String message = StringManager.get("message.soulbound") + "\n" + upgradeStr;
                    Game.ShowMessage(message, 5f);
                }
                else {
                    Game.ShowMessage(upgradeStr, 3f);
                }
            } else {
                player.addInventorySlot();

                String upgradeStr = StringManager.get("message.bagupgrade");

                if (persistentUpgrade) {
                    Game.instance.progression.addInventorySlot();
                    String message = StringManager.get("message.soulbound") + "\n" + upgradeStr;
                    Game.ShowMessage(message, 5f);
                }
                else {
                    Game.ShowMessage(upgradeStr, 3f);
                }
            }
        }
        catch(Exception ex) {
            Gdx.app.log("Delver", ex.getMessage());
        }

        // remove from the inventory
        int location = player.inventory.indexOf(this, true);
        if(location >= 0) {
            player.inventory.set(location, null);
        }

        Game.hudManager.backpack.refresh();
        Game.hudManager.quickSlots.refresh();

        Audio.playSound("inventory/open_inventory.mp3", 0.9f);
    }
}
