package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.InventorySlot;

public class PlayerBag extends Element {
    public String image;
    public int columns = 6;

    @Override
    public Actor createActor() {
        HorizontalGroup group = new HorizontalGroup() {
            private int value = Game.instance.player.inventorySize;
            @Override
            public void act(float delta) {
                super.act(delta);

                if (value != Game.instance.player.inventorySize) {
                    for (int i = value; i < Game.instance.player.inventorySize; i++) {
                        addActor(new InventorySlot(image, i));
                    }
                    value = Game.instance.player.inventorySize;
                    Game.layout.resize();
                }
            }
        };
        // Align children inside of group to the bottom left
        group.align(Align.bottomLeft);
        int slotWidth = 0;
        int slotHeight = 0;

        // Build out slots
        for (int i = Game.instance.player.hotbarSize; i < Game.instance.player.inventorySize; i++) {
            InventorySlot slot = new InventorySlot(image, i);
            slotWidth = (int)Math.max(slotWidth, slot.getWidth());
            slotHeight = (int)Math.max(slotHeight, slot.getHeight());
            group.addActor(slot);
        }

        int rows = (int)Math.ceil((Game.instance.player.inventorySize - Game.instance.player.hotbarSize) / (float)columns);
        group.setHeight(slotHeight * rows);
        group.setWidth(slotWidth * columns);
        group.rowAlign(Align.left);
        group.wrap();

        return group;
    }
}