package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;

public class InventorySlot extends ItemSlot {
    public int index;

    public InventorySlot(int index, String image) {
        super(image);
        this.index = index;

        updateItemTexture(getItem());

        if (index < Game.instance.player.hotbarSize) {
            Label slotNumberLabel = new Label(String.valueOf(index + 1), UiSkin.getSkin());
            slotNumberLabel.setAlignment(Align.bottomLeft);
            addActor(slotNumberLabel);
        }

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Item item = getItem();
                if (item != null) {
                    Game.instance.player.UseInventoryItem(index);
                }
            }
        });
    }

    @Override
    public Item getItem() {
        return Game.instance.player.inventory.get(index);
    }

    @Override
    public void setItem(Item item) {
        Game.instance.player.inventory.set(index, item);
        updateItemTexture(item);
    }
}
