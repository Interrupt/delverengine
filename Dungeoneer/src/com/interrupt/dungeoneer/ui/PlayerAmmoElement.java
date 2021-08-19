package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Gun;
import com.interrupt.dungeoneer.game.Game;

public class PlayerAmmoElement extends Element {
    @Override
    public Actor createActor() {
        Label label = new Label(String.valueOf(getAmmoCount()), UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);

                int ammoCount = getAmmoCount();

                if (ammoCount >= 0) {
                    setText(ammoCount);
                }
                else {
                    setText("");
                }
            }
        };
        label.setColor(75 / 255f, 91 / 255f, 143 / 255f, 1f);

        return label;
    }

    public static int getAmmoCount() {
        if (Game.instance == null) return -1;
        Player player = Game.instance.player;
        if (player == null) return -1;
        Integer heldItem = player.heldItem;
        if (heldItem == null) return -1;

        Item item = player.inventory.get(heldItem);

        if (item instanceof Gun) {
            Gun gun = (Gun)item;
            return gun.getAmmoCount();
        }

        return -1;
    }
}
