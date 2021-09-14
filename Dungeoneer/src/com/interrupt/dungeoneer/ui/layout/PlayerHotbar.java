package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.InventorySlot;

public class PlayerHotbar extends Element {
    public String image;

    @Override
    public Actor createActor() {
        HorizontalGroup group = new HorizontalGroup() {
            private int value = Game.instance.player.hotbarSize;
            @Override
            public void act(float delta) {
                super.act(delta);

                if (value != Game.instance.player.hotbarSize) {
                    for (int i = value; i < Game.instance.player.hotbarSize; i++) {
                        addActor(new InventorySlot(image, i));
                    }
                    value = Game.instance.player.hotbarSize;
                    Game.layout.resize();
                }
            }
        };
        // Align children inside of group to the bottom left
        group.align(Align.bottomLeft);

        // Build out slots
        for (int i = 0; i < Game.instance.player.hotbarSize; i++) {
            group.addActor(new InventorySlot(image, i));
        }

        group.pack();

        return group;
    }
}
