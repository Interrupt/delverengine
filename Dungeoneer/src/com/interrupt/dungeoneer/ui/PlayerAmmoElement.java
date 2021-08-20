package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Gun;
import com.interrupt.dungeoneer.game.Game;

public class PlayerAmmoElement extends Element {
    public Color color = Color.WHITE;
    
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
        label.setColor(color.r, color.g, color.b, 1f);

        switch (pivot) {
            case BOTTOM_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottomLeft);
                break;

            case BOTTOM_CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottom | com.badlogic.gdx.utils.Align.center);
                break;

            case BOTTOM_RIGHT:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottomRight);
                break;

            case CENTER_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.left | com.badlogic.gdx.utils.Align.center);
                break;

            case CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.center);
                break;

            case CENTER_RIGHT:
                label.setAlignment(com.badlogic.gdx.utils.Align.right | com.badlogic.gdx.utils.Align.center);
                break;

            case TOP_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.topLeft);
                break;

            case TOP_CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.top | com.badlogic.gdx.utils.Align.center);
                break;

            case TOP_RIGHT:
                label.setAlignment(Align.topRight);
                break;
        }

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
