package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Gun;
import com.interrupt.dungeoneer.game.Game;

public class PlayerAmmoElement extends Element {
    @Override
    public Actor getActor() {
        Label label = new Label("AMMO: 0", UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);

                Player player = Game.instance.player;
                Integer heldItem = player.heldItem;
                if (heldItem == null) return;

                Item item = player.inventory.get(heldItem);

                if (item instanceof Gun) {
                    Gun gun = (Gun)item;
                    setText("AMMO: " + gun.getAmmoCount());
                    setPosition(x, y);
                    return;
                }

                setText("");
            }
        };
        label.setPosition(x, y);
        label.setColor(Color.WHITE);

        return label;
    }
}
