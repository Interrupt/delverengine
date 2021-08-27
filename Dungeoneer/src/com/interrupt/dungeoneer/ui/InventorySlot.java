package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Predicate;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;

import java.util.Objects;

public class InventorySlot extends Stack {
        public int index;
        private final Image itemImage;
        private Item item;

        public InventorySlot(int index, String image) {
            super();
            this.index = index;

            FileHandle file = Game.getInternal(image);
            if (file.exists()) {
                Texture texture = new Texture(file);
                addActor(new Image(texture));
            }

            if (index < Game.instance.player.hotbarSize) {
                Label slotNumberLabel = new Label(String.valueOf(index + 1), UiSkin.getSkin());
                slotNumberLabel.setAlignment(Align.bottomLeft);
                addActor(slotNumberLabel);
            }

            itemImage = new Image();
            Item item = getItem();
            updateItemTexture(item);
            addActor(itemImage);

            pack();

            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Item item = getItem();
                    if (item != null) {
                        Game.instance.player.UseInventoryItem(index);
                    }
                }
            });

            final InventorySlot self = this;
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

                    Array<InventorySlot> targets = Game.canvas.find(
                        InventorySlot.class,
                        new Predicate<InventorySlot>() {
                            @Override
                            public boolean evaluate(InventorySlot slot) {
                                return index != slot.index;
                            }
                        });

                    // Setup targets for all found InventorySlots
                    for (InventorySlot target : targets) {
                        dragAndDrop.addTarget(new DragAndDrop.Target(target) {
                            @Override
                            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                                return true;
                            }

                            @Override
                            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                                InventorySlot slot = (InventorySlot)payload.getObject();
                                Game.SwapInventoryItems(slot.index, target.index);
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
            return Game.instance.player.inventory.get(index);
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
            if (!Objects.equals(current, item)) {
                item = current;
                updateItemTexture(item);
            }
        }
    }
