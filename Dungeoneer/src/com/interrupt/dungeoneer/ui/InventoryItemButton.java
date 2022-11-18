package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;

public class InventoryItemButton extends Button {

    public boolean isTouched = false;
	public boolean isBeingDragged = false;
    public int cursor;

    public Integer inventorySlot;
    EquipLoc equipLoc;

    GameInput gameInput;

	public InventoryItemButton(EquipLoc inEquipLoc, Integer inInventorySlot, Drawable itemRegion) {
		super(itemRegion);

		final InventoryItemButton thisButton = this;

        // Keep track of where we started
        equipLoc = inEquipLoc;
        gameInput = Game.instance.input;
        inventorySlot = inInventorySlot;

        // Add a listener for drag events
		addListener(new InputListener() {
			@Override
	        public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                // Start a drag, if nobody else has yet
                if(button == 0 && Hotbar.getItemAtTouchStart() == null) {
                    thisButton.touchStarted(pointer);
                }
	        	return true;
	        }
		});

        // Also add a listener for click events
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(thisButton.isBeingDragged)
                    return;

                Game.instance.player.UseInventoryItem(inventorySlot);

                // Reset for next time
                thisButton.isTouched = false;
                thisButton.isBeingDragged = false;
                Hotbar.setItemBeingDragged(null);
                Hotbar.setItemAtTouchStart(null);
            }
        });
	}

    public void touchStarted(int pointer) {
        Gdx.app.log("DelverInventory", "Touch Started");
        Hotbar.setItemAtTouchStart(this);
        isTouched = true;
        cursor = pointer;
    }

    public void dragStarted() {
        // Set ourselves as the item being dragged.
        Gdx.app.log("DelverInventory", "Drag started!");
        Hotbar.setItemBeingDragged(this);
        isBeingDragged = true;

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
    }

    public void touchEnded() {
        if(isBeingDragged) {
            Hotbar.setItemBeingDragged(null);
            finishDragAndDrop();
        }

        // Reset back to defaults
        isTouched = false;
        isBeingDragged = false;
        Hotbar.setItemAtTouchStart(null);
    }

    public void finishDragAndDrop() {
        Item draggedItem = getItem();

        // Either drop the item on the ground, or in the inventory somewhere
        Hud.DragAndDropResult movedItem = Game
            .DragAndDropInventoryItem(draggedItem, inventorySlot, equipLoc != null ? equipLoc.equipLoc : null);

        if (movedItem == Hud.DragAndDropResult.drop) {
            // Can either be dropped from the inventory, or an equip slot
            if(inventorySlot != null)
                Game.instance.player.dropItemFromInv(inventorySlot, Game.GetLevel(), 0, 0);
            else
                Game.instance.player.dropItem(draggedItem, Game.instance.level, 0);

            Vector3 levelIntersection = new Vector3();
            Ray ray = Game.camera.getPickRay(gameInput.getPointerX(cursor), gameInput.getPointerY(cursor));
            float distance = 0;
            if (Collidor.intersectRayTriangles(ray,
                GameManager.renderer.GetCollisionTrianglesAlong(ray, 20f), levelIntersection,
                null)) {
                distance = ray.origin.sub(levelIntersection).len();
            }

            draggedItem.xa = ray.direction.x * 0.28f * Math.min(1, distance / 6.0f);
            draggedItem.za = ray.direction.y * 0.5f * Math.min(1, distance / 6.0f) + 0.04f;
            draggedItem.ya = ray.direction.z * 0.28f * Math.min(1, distance / 6.0f);

            // Clear the equip location when dropped
            if(equipLoc != null) {
                Game.instance.player.equippedItems.put(equipLoc.equipLoc, null);
            }
        }

        Game.RefreshUI();
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Nothing to do if we we were not clicked on before
        if(!isTouched)
            return;

        // Check if we should end our touch / drag and drop
        if(!gameInput.isPointerTouched(cursor)) {
            touchEnded();
            return;
        }

        final float uiSize = Game.GetUiSize();
        if(isBeingDragged) {
            // If we are being dragged, our position should be the cursor position
            setY(-gameInput.getPointerY(cursor) + Gdx.graphics.getHeight() - uiSize / 2);
            setX(gameInput.getPointerX(cursor) - uiSize / 2);
            return;
        }

        float movedAmountX = Math.abs(gameInput.getUiTouchPosition().x  - gameInput.getPointerX(cursor));
        float movedAmountY = Math.abs(gameInput.getUiTouchPosition().y  - gameInput.getPointerY(cursor));

        // Check if we have been moved far enough to start being dragged
        boolean hasMovedEnoughX = movedAmountX > uiSize / 8;
        boolean hasMovedEnoughY = movedAmountY > uiSize / 8;
        boolean hasMovedEnough = hasMovedEnoughX || hasMovedEnoughY;

        //Gdx.app.log("DelverInventory", "Drag amount: " + moveAmountX + "," + moveAmountY);

        if(hasMovedEnough) {
            dragStarted();
        }
    }

    public Item getItem() {
        if(inventorySlot != null) {
            return Game.instance.player.inventory.get(inventorySlot);
        }

        if(equipLoc != null) {
            return Game.instance.player.equippedItems.get(equipLoc.equipLoc);
        }

        return null;
    }
}
