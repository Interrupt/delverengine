package com.interrupt.dungeoneer.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.Hud.DragAndDropResult;

public class Hotbar {
    public final Map<Integer, MultiTouchButton> itemButtons = new HashMap<>();
    public final Map<Integer, Boolean> wasPressedLast = new HashMap<>();
    public final Map<Integer, Boolean> isDragging = new HashMap<>();
    public int columns = 6;
    public int rows = 1;
    public int invOffset = 0;
    public float yOffset = 0;
    public boolean visible = true;
    public Item dragging = null;
    public Integer gamepadPosition = null;

    private Integer mouseOverSlot = null;
    private Integer lastUiTouchPointer = null;

    public Hotbar() {}

    public Hotbar(int columns, int rows, int invOffset) {
        this.columns = columns;
        this.rows = rows;
        this.invOffset = invOffset;
    }

    public void init() {
        refresh();
    }

    public void refresh() {
        for (Entry<Integer, MultiTouchButton> entry : itemButtons.entrySet()) {
            Game.ui.getActors().removeValue(entry.getValue(), true);
        }

        lastUiTouchPointer = null;

        itemButtons.clear();
        wasPressedLast.clear();
        isDragging.clear();

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
                        MultiTouchButton itmButton = new MultiTouchButton(
                                new TextureRegionDrawable(itm.getInventoryTextureRegion()),
                                new TextureRegionDrawable(itm.getInventoryTextureRegion()));
                        itemButtons.put(btnLoc, itmButton);

                        Game.ui.addActor(itmButton);

                        itmButton.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                if (Game.isMobile || !Game.instance.input.caughtCursor) {
                                    if (!isDragging.containsKey(btnLoc) || !isDragging.get(btnLoc)) {
                                        isDragging.clear();
                                        wasPressedLast.clear();
                                        mouseOverSlot = null;
                                        Game.instance.player.UseInventoryItem(btnLoc);
                                    }
                                }
                            }
                        });
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
                        MultiTouchButton hb = itemButtons.get(i);

                        int yPos = (int) (((Gdx.graphics.getHeight() - uiSize) - (int) (y * uiSize))
                                - (yOffset * uiSize));
                        int xPos = (int) (uiSize * x + Gdx.graphics.getWidth() / 2.0 - uiSize * (columns / 2.0));

                        if (!hb.isDragging) {
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

    public void tickUI(GameInput input) {
        Integer uiTouchPointer = input.uiTouchPointer;
        if (uiTouchPointer != null)
            lastUiTouchPointer = uiTouchPointer;
        else if (lastUiTouchPointer != null)
            uiTouchPointer = lastUiTouchPointer;
        else
            uiTouchPointer = 0;

        final float uiSize = Game.GetUiSize();
        final float xCursorPos = input.getPointerX(uiTouchPointer) - Gdx.graphics.getWidth() / 2.0f;
        final float yCursorPos = input.getPointerY(uiTouchPointer);
        mouseOverSlot = null;
        dragging = null;

        if (!visible)
            return;

        if (Game.isMobile || !Game.instance.input.caughtCursor) {
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < columns; x++) {
                    final int i = x + (y * columns);
                    float xPos1 = -((uiSize * columns) / 2.0f) + uiSize * x;
                    float xPosD = xPos1;

                    if ((Game.isMobile || !input.caughtCursor) && xCursorPos > xPosD && xCursorPos <= xPosD + uiSize
                            && yCursorPos <= ((y + 1) * uiSize) + (yOffset * uiSize)
                            && yCursorPos > (y * uiSize) + (yOffset * uiSize)) {
                        final int btnLoc = x + (y * columns) + invOffset;
                        if (btnLoc < Game.instance.player.inventorySize) {
                            mouseOverSlot = i;
                        }
                    }
                }
            }
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                final int i = x + (y * columns) + invOffset;
                if (itemButtons.containsKey(i)) {
                    MultiTouchButton hb = itemButtons.get(i);

                    int yPos = (int) (((Gdx.graphics.getHeight() - uiSize) - (int) (y * uiSize)) - (yOffset * uiSize));
                    int xPos = (int) (uiSize * x + Gdx.graphics.getWidth() / 2.0 - uiSize * (columns / 2.0));

                    hb.setWidth(uiSize);
                    hb.setHeight(uiSize);

                    if (hb.isDragging) {
                        if (Game.isMobile || !Game.instance.input.caughtCursor) {
                            if ((isDragging.containsKey(i) && isDragging.get(i))
                                    || Math.abs(input.getUiTouchPosition().x
                                            - input.getPointerX(uiTouchPointer)) > uiSize / 8
                                    || Math.abs(input.getUiTouchPosition().y
                                            - input.getPointerY(uiTouchPointer)) > uiSize / 8) {
                                hb.setY(-input.getPointerY(uiTouchPointer) + Gdx.graphics.getHeight() - uiSize / 2);
                                hb.setX(input.getPointerX(uiTouchPointer) - uiSize / 2);
                                isDragging.put(i, true);

                                dragging = Game.instance.player.inventory.get(i);
                                Game.dragging = dragging;
                            }
                        }

                        if (!isDragging.containsKey(i) || !isDragging.get(i)) {
                            hb.setX(xPos);
                            hb.setY(yPos);
                        }
                    } else {
                        // item was dragged, switch inv slot or drop item
                        if (wasPressedLast.containsKey(i) && wasPressedLast.get(i)) {
                            DragAndDropResult movedItem = Game
                                    .DragAndDropInventoryItem(Game.instance.player.inventory.get(i), i, null);

                            if (movedItem == DragAndDropResult.drop) {
                                Item draggedItem = Game.instance.player.inventory.get(i);
                                Game.instance.player.dropItemFromInv(i, Game.GetLevel(), 0, 0);

                                Vector3 levelIntersection = new Vector3();
                                Ray ray = Game.camera.getPickRay(input.getPointerX(uiTouchPointer),
                                        input.getPointerY(uiTouchPointer));
                                float distance = 0;
                                if (Collidor.intersectRayTriangles(ray,
                                        GameManager.renderer.GetCollisionTrianglesAlong(ray, 20f), levelIntersection,
                                        null)) {
                                    distance = ray.origin.sub(levelIntersection).len();
                                }

                                draggedItem.xa = ray.direction.x * 0.28f * Math.min(1, distance / 6.0f);
                                draggedItem.za = ray.direction.y * 0.5f * Math.min(1, distance / 6.0f) + 0.04f;
                                draggedItem.ya = ray.direction.z * 0.28f * Math.min(1, distance / 6.0f);
                            }

                            Game.RefreshUI();
                            break;
                        }

                        hb.setY(yPos);
                        hb.setX(xPos);
                    }

                    if (Game.isMobile || !Game.instance.input.caughtCursor) {
                        wasPressedLast.put(i, hb.isDragging);
                    }
                }
            }
        }
    }

    public Integer getMouseOverSlot() {
        return mouseOverSlot;
    }
}
