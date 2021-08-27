package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;

import java.util.Objects;

public class ItemSlot extends Stack {
    private final Image itemImage;
    private Item value;
    private Item item;

    public ItemSlot(String image) {
        super();

        FileHandle file = Game.getInternal(image);
        if (file.exists()) {
            Texture texture = new Texture(file);
            addActor(new Image(texture));
        }

        itemImage = new Image();
        Item item = getItem();
        updateItemTexture(item);
        addActor(itemImage);

        pack();

        final ItemSlot self = this;
        DragAndDrop dragAndDrop = new DragAndDrop();
        dragAndDrop.addSource(new DragAndDrop.Source(self) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null) {
                    // Show item if not a valid drop
                    updateItemTexture(getItem());
                }
            }

            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                // Don't do drag if slot is empty
                Item item = getItem();
                if (item == null) {
                    return null;
                }

                // Reconfigure the drag and drop object
                dragAndDrop.clear();
                dragAndDrop.addSource(this);

                Array<ItemSlot> targets = Game.canvas.find(
                    ItemSlot.class,
                    new Predicate<ItemSlot>() {
                        @Override
                        public boolean evaluate(ItemSlot slot) {
                            return true;
                        }
                    });

                // Setup targets for all found ItemSlots
                for (ItemSlot target : targets) {
                    dragAndDrop.addTarget(new DragAndDrop.Target(target) {
                        @Override
                        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                            return true;
                        }

                        @Override
                        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                            ItemSlot slot = (ItemSlot) payload.getObject();
                            swapItems(slot, target);
                        }
                    });
                }

                payload.setObject(self);
                payload.setDragActor(new Image(new TextureRegionDrawable(item.getInventoryTextureRegion())));
                dragAndDrop.setDragActorPosition(x, -y);

                // Hide item while dragging
                updateItemTexture(null);
                return payload;
            }
        });
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
        updateItemTexture(item);
    }

    public boolean isEmpty() {
        return getItem() == null;
    }

    public void updateItemTexture(Item i) {
        if (i == null) {
            itemImage.setDrawable(null);
            return;
        }

        itemImage.setDrawable(new TextureRegionDrawable(i.getInventoryTextureRegion()));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Item current = getItem();
        if (!Objects.equals(current, value)) {
            value = current;
            updateItemTexture(value);
        }
    }

    public static void swapItems(ItemSlot fromSlot, ItemSlot toSlot) {
        if (fromSlot.equals(toSlot)) {
            return;
        }

        Player player = Game.instance.player;
        Item fromItem = fromSlot.getItem();
        Item toItem = toSlot.getItem();

        boolean isFromItemHeld = player.isHeld(fromItem);
        boolean isToItemHeld = player.isHeld(toItem);

        fromSlot.setItem(toItem);
        toSlot.setItem(fromItem);

        if (isFromItemHeld) {
            player.equip(fromItem, false);
        }
        else if (isToItemHeld) {
            player.equip(toItem, false);
        }
    }
}
