package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Tooltip;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.ui.layout.Element;

import java.util.Objects;

public class ItemSlot extends Stack {
    private final Image itemImage;
    private Item value;
    private Item item;

    public static Item hovered;

    public ItemSlot(String image) {
        super();

        // Background image
        FileHandle file = Game.getInternal(image);
        if (file.exists()) {
            Texture texture = new Texture(file);
            addActor(new Image(texture));
        }

        // Item image
        itemImage = new Image();
        Item item = getItem();
        updateItemTexture(item);
        addActor(itemImage);

        pack();

        final ItemSlot self = this;

        // Tooltips
        Element template = Game.hudManager.getPrefab("itemTooltip");
        Element copy = (Element) KryoSerializer.copyObject(template);
        copy.init();
        Actor contents = copy.getActor();

        Tooltip<Actor> tooltip = new Tooltip<Actor>(contents) {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                hovered = getItem();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);

                if (Objects.equals(hovered, getItem())) {
                    hovered = null;
                }
            }
        };
        tooltip.setInstant(true);
        addListener(tooltip);

        // Drag and drop
        DragAndDrop dragAndDrop = new DragAndDrop();
        dragAndDrop.addSource(new DragAndDrop.Source(self) {
            final DragAndDrop.Payload payload = new DragAndDrop.Payload();

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                if (target == null) {
                    // Drop item from slot into level.
                    // TODO: Do fancy drop/throw logic
                    ItemSlot slot = (ItemSlot) payload.getObject();
                    Item item = slot.getItem();
                    slot.setItem(null);
                    Game.instance.player.throwItem(item, Game.instance.level, 0, 0);
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
                hideItem();
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

    /** Is slot empty? */
    public boolean isEmpty() {
        return getItem() == null;
    }

    /** Can given item be placed into slot? */
    public boolean allows(Item item) {
        return true;
    }

    public void updateItemTexture(Item i) {
        if (i == null) {
            itemImage.setDrawable(null);
            return;
        }

        itemImage.setDrawable(new TextureRegionDrawable(i.getInventoryTextureRegion()));
    }

    public void showItem() {
        updateItemTexture(getItem());
    }

    public void hideItem() {
        updateItemTexture(null);
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
        Player player = Game.instance.player;
        Item fromItem = fromSlot.getItem();
        Item toItem = toSlot.getItem();

        // Check if swap is allowed.
        if (fromSlot.equals(toSlot) || !fromSlot.allows(toItem) || !toSlot.allows(fromItem)) {
            fromSlot.showItem();
            return;
        }

        boolean isFromItemHeld = player.isHeld(fromItem);
        boolean isToItemHeld = player.isHeld(toItem);

        // Perform swap
        fromSlot.setItem(toItem);
        toSlot.setItem(fromItem);

        // Make sure held item is correct.
        if (isFromItemHeld) {
            player.equip(fromItem, false);
        }
        else if (isToItemHeld) {
            player.equip(toItem, false);
        }
    }
}
