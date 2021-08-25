package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.MultiTouchButton;
import com.interrupt.dungeoneer.ui.UiSkin;

public class PlayerHotbarElement extends Element {
    public String image;

    @Override
    public Actor createActor() {
        HorizontalGroup group = new HorizontalGroup();
        // Align children inside of group to the bottom left
        group.align(Align.bottomLeft);

        // Get texture
        FileHandle file = Game.getInternal(image);
        if (!file.exists()) {
            return null;
        }
        Texture texture = new Texture(file);

        // Build out slots
        int size = Game.instance.player.hotbarSize;
        for (int i = 0; i < size; i++) {
            Stack slot = new Stack();
            slot.addActor(new Image(texture));
            Label slotNumberLabel = new Label(String.valueOf(i + 1), UiSkin.getSkin());
            slotNumberLabel.setAlignment(Align.bottomLeft);
            slot.addActor(slotNumberLabel);
            slot.pack();

            Item item = Game.instance.player.inventory.get(i);
            if(item != null) {
                final int inventoryIndex = i;
                MultiTouchButton button = new MultiTouchButton(
                    new TextureRegionDrawable(item.getInventoryTextureRegion()),
                    new TextureRegionDrawable(item.getInventoryTextureRegion())
                );

                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Game.instance.player.UseInventoryItem(inventoryIndex);
                        Item i = Game.instance.player.inventory.get(inventoryIndex);
                        if (i == null) {
                            button.remove();
                        }
                    }
                });

                slot.add(button);
            }

            group.addActor(slot);
        }

        group.pack();

        return group;
    }
}
