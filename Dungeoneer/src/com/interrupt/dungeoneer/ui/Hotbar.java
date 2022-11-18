package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Hotbar {
    public final Map<Integer, InventoryItemButton> itemButtons = new HashMap<>();
    public int columns = 6;
    public int rows = 1;
    public int invOffset = 0;
    public float yOffset = 0;
    public boolean visible = true;
    public Item dragging = null;
    public Integer gamepadPosition = null;

    private Integer mouseOverSlot = null;
    private Integer lastUiTouchPointer = null;

    private static InventoryItemButton itemBeingDragged;
    private static InventoryItemButton itemClickedInitially;

    public Hotbar() { }

    public Hotbar(int columns, int rows, int invOffset) {
        this.columns = columns;
        this.rows = rows;
        this.invOffset = invOffset;
    }

    public void init() {
        refresh();
    }

    public void refresh() {
        GameInput input = Game.instance.input;

        for (Entry<Integer, InventoryItemButton> entry : itemButtons.entrySet()) {
            Game.ui.getActors().removeValue(entry.getValue(), true);
        }

        lastUiTouchPointer = null;

        itemButtons.clear();

        if (Game.ui == null || !visible)
            return;

        if (this == Game.hudManager.quickSlots) {
            columns = Game.instance.player.hotbarSize;
        } else if (this == Game.hudManager.backpack) {
            invOffset = Game.instance.player.hotbarSize;
            rows = (int) Math.ceil((Game.instance.player.inventorySize - invOffset) / columns) + 1;
            if ((Game.instance.player.inventorySize - invOffset) % columns == 0)
                rows--;
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                final int btnLoc = x + (y * columns) + invOffset;
                if (btnLoc < Game.instance.player.inventorySize) {
                    Item itm = Game.instance.player.inventory.get(btnLoc);
                    if (itm != null) {
                        InventoryItemButton itmButton = new InventoryItemButton(null, btnLoc, new TextureRegionDrawable(itm.getInventoryTextureRegion()));
                        itemButtons.put(btnLoc, itmButton);
                        Game.ui.addActor(itmButton);
                    }
                }
            }
        }

        initButtons();
    }

    private void initButtons() {
        final float uiSize = Game.GetUiSize();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                final int i = x + (y * columns) + invOffset;
                if (i < Game.instance.player.inventorySize) {
                    if (itemButtons.containsKey(i)) {
                        InventoryItemButton hb = itemButtons.get(i);

                        int yPos = (int) (((Gdx.graphics.getHeight() - uiSize) - (int) (y * uiSize))
                                - (yOffset * uiSize));
                        int xPos = (int) (uiSize * x + Gdx.graphics.getWidth() / 2.0 - uiSize * (columns / 2.0));

                        // Put the items not being dragged in the grid
                        if(getItemBeingDragged() != hb) {
                            hb.setY(yPos);
                            hb.setX(xPos);
                        }

                        hb.setWidth(uiSize);
                        hb.setHeight(uiSize);
                    }
                }
            }
        }
    }

    public Integer getMouseOverSlot(GameInput input, Integer uiTouchPointer) {
        final float uiSize = Game.GetUiSize();
        final float xCursorPos = input.getPointerX(uiTouchPointer) - Gdx.graphics.getWidth() / 2.0f;
        final float yCursorPos = input.getPointerY(uiTouchPointer);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                final int i = x + (y * columns);
                float xPos = -((uiSize * columns) / 2.0f) + (uiSize * x);
                float yPos = (y * uiSize) + (yOffset * uiSize);

                if (xCursorPos > xPos && xCursorPos <= xPos + uiSize
                    && yCursorPos > yPos && yCursorPos <= yPos + uiSize) {
                    final int btnLoc = x + (y * columns) + invOffset;
                    if (btnLoc < Game.instance.player.inventorySize) {
                        return i;
                    }
                }
            }
        }

        return null;
    }

    public void tickUI(GameInput input) {
        Integer uiTouchPointer = input.uiTouchPointer;
        if (uiTouchPointer != null)
            lastUiTouchPointer = uiTouchPointer;
        else if (lastUiTouchPointer != null)
            uiTouchPointer = lastUiTouchPointer;
        else
            uiTouchPointer = 0;

        final float uiSize = Game.GetUiSize();
        mouseOverSlot = null;
        dragging = null;

        if (!visible)
            return;

        // We are able to drag inventory items whenever in mobile mode, or whenever the cursor is showing
        boolean canDrag = Game.isMobile || !Game.instance.input.caughtCursor;

        // If we can drag, figure out which inventory slot the cursor is over
        if (canDrag) {
            mouseOverSlot = getMouseOverSlot(input, uiTouchPointer);
        }

        // Snap the button positions to the grid
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                final int i = x + (y * columns) + invOffset;
                if (itemButtons.containsKey(i)) {
                    InventoryItemButton hb = itemButtons.get(i);
                    int yPos = (int) (((Gdx.graphics.getHeight() - uiSize) - (int) (y * uiSize)) - (yOffset * uiSize));
                    int xPos = (int) (uiSize * x + Gdx.graphics.getWidth() / 2.0 - uiSize * (columns / 2.0));

                    hb.setWidth(uiSize);
                    hb.setHeight(uiSize);

                    // Snap items not being dragged to the grid
                    if(getItemBeingDragged() != hb) {
                        hb.setX(xPos);
                        hb.setY(yPos);
                    }
                }
            }
        }
    }

    public Integer getMouseOverSlot() {
        return mouseOverSlot;
    }

    public static InventoryItemButton getItemBeingDragged() {
        return itemBeingDragged;
    }

    public static void setItemBeingDragged(InventoryItemButton itemButton) {
        itemBeingDragged = itemButton;

        if(itemButton == null) {
            Game.dragging = null;
            return;
        }

        if(itemBeingDragged.inventorySlot != null) {
            Game.dragging = Game.instance.player.inventory.get(itemBeingDragged.inventorySlot);
            return;
        }

        if(itemButton.equipLoc != null) {
            Game.dragging = Game.instance.player.equippedItems.get(itemButton.equipLoc.equipLoc);
        }
    }

    public static InventoryItemButton getItemAtTouchStart() {
        return itemClickedInitially;
    }

    public static void setItemAtTouchStart(InventoryItemButton clickStartItem) {
        itemClickedInitially = clickStartItem;
    }
}
