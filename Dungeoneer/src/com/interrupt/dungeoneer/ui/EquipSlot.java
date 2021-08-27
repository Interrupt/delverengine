package com.interrupt.dungeoneer.ui;

import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;

public class EquipSlot extends ItemSlot {
    public String equipLoc;

    public EquipSlot(String image, String equipLoc) {
        super(image);

        if (equipLoc == null || equipLoc.isEmpty()) {
            equipLoc = "ARMOR";
        }

        this.equipLoc = equipLoc;
    }

    @Override
    public Item getItem() {
        return Game.instance.player.equippedItems.get(equipLoc);
    }

    @Override
    public void setItem(Item item) {
        Game.instance.player.equippedItems.put(equipLoc, item);
    }
}
