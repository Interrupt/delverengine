package com.interrupt.dungeoneer.ui;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.managers.StringManager;

public class Hud {

	protected TextureRegion itemTextures[];
	MultiTouchButton inventoryBtn;
	MultiTouchButton mapBtn;

    // Item currently being dragged
	public Item dragging = null;

	public ArrayMap<String,EquipLoc> equipLocations = new ArrayMap<String,EquipLoc>();
	public enum DragAndDropResult { equip, drop, invalid, ignore };

    // Item Button being dragged
    private static InventoryItemButton itemBeingDragged;
    private static InventoryItemButton itemClickedInitially;

	public void init(TextureRegion itemTextures[])
	{
		this.itemTextures = itemTextures;

		float startX = -4.7f;
		float startY = 1.7f;

		equipLocations.put( "HAT", new EquipLoc("HAT", startX, startY, 103) );
		equipLocations.put( "ARMOR", new EquipLoc("ARMOR", startX, startY + 1f, 119) );
		equipLocations.put( "PANTS", new EquipLoc("PANTS", startX, startY + 2f, 111) );

		equipLocations.put( "OFFHAND", new EquipLoc("OFFHAND", startX + 1f, startY, 102) );
		equipLocations.put( "RING", new EquipLoc("RING", startX + 1f, startY + 1f, 118) );
		equipLocations.put( "AMULET", new EquipLoc("AMULET", startX + 1f, startY + 2f, 110) );

		for(EquipLoc loc : equipLocations.values())
		{
			loc.init(itemTextures);
		}

		refresh();
	}

	public void refreshEquipLocations()
	{
		for(EquipLoc loc : equipLocations.values())
		{
			loc.refresh();
		}
	}

	public void refresh() {

		for(EquipLoc loc : equipLocations.values())
		{
			loc.refresh();
		}

		if(inventoryBtn != null) Game.ui.getActors().removeValue(inventoryBtn, true);
		if(mapBtn != null) Game.ui.getActors().removeValue(mapBtn, true);

		inventoryBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[57]));
		inventoryBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if(Game.isMobile || !Game.instance.input.caughtCursor)
					Game.instance.toggleInventory();
			}
		});

		if(Game.isMobile) {
			Game.ui.addActor(inventoryBtn);
		}

		//mapBtn = new MultiTouchButton(itemTextures[61]);
		mapBtn = new MultiTouchButton(null);
		mapBtn.addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(Game.isMobile || !Game.instance.input.caughtCursor)
					GameManager.renderer.showMap = !GameManager.renderer.showMap;
			}

		});
		Game.ui.addActor(mapBtn);

		final float uiSize = Game.GetUiSize();
		final float inventoryBtnSize = uiSize + (inventoryBtn.isPressed() ? 0f : 0.1f) * uiSize;

		inventoryBtn.setWidth(inventoryBtnSize);
		inventoryBtn.setHeight(inventoryBtnSize);

		mapBtn.setWidth(uiSize * 1.4f);
		mapBtn.setHeight(uiSize * 1.4f);

		int yPos = (int) (Gdx.graphics.getHeight() - uiSize);
		int xPos = (int) (Gdx.graphics.getWidth() - uiSize);
		inventoryBtn.setY(yPos);
		inventoryBtn.setX(xPos);

		mapBtn.setX(0);
		mapBtn.setY((int) (Gdx.graphics.getHeight() - mapBtn.getHeight()));
	}

	public void tick(GameInput input) {

		for(EquipLoc loc : equipLocations.values())
		{
			loc.tickUI(input);
		}

		float uiSize = Game.GetUiSize();
		final float inventoryBtnSize = uiSize + (inventoryBtn.isPressed() ? 0.1f : 0f) * uiSize;

        // Inventory Button position and sizing
		int yPos = (int) (Gdx.graphics.getHeight() - uiSize);
		int xPos = (int) (Gdx.graphics.getWidth() - uiSize);
		inventoryBtn.setY(yPos);
		inventoryBtn.setX(xPos);

        inventoryBtn.setWidth(inventoryBtnSize);
        inventoryBtn.setHeight(inventoryBtnSize);

        // Map Button position and sizing
		mapBtn.setX(0);
		mapBtn.setY((int) (Gdx.graphics.getHeight() - mapBtn.getHeight()));

        mapBtn.setWidth(uiSize * 1.4f);
        mapBtn.setHeight(uiSize * 1.4f);
	}

	public Item getHoveredInventoryItem()
	{
		Integer bagOver = Game.hudManager.backpack.getMouseOverSlot();
		if(bagOver != null) return Game.instance.player.inventory.get(bagOver + Game.hudManager.backpack.invOffset);

		Integer hotbarOver = Game.hudManager.quickSlots.getMouseOverSlot();
		if(hotbarOver != null) return Game.instance.player.inventory.get(hotbarOver + Game.hudManager.quickSlots.invOffset);

		for(EquipLoc loc : equipLocations.values()) {
            if(loc.isHovered()) {
				return Game.instance.player.equippedItems.get(loc.equipLoc);
			}
		}

		return null;
	}

    public void startDragFromWorld(Item item, int cursor) {
        InventoryItemButton newDraggingItem = new InventoryItemButton(null, null, new TextureRegionDrawable(item.getInventoryTextureRegion()));
        newDraggingItem.itemFromWorld = item;
        newDraggingItem.touchStarted(cursor);
        newDraggingItem.dragStarted();

        // Remove the item from the game world
        item.isActive = false;

        // Add this new button to the UI
        Game.ui.addActor(newDraggingItem);
    }

	public boolean isAttackPressed() { return false; }

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
            return;
        }
        if(itemButton.itemFromWorld != null) {
            Game.dragging = itemButton.itemFromWorld;
        }
    }

    public static InventoryItemButton getItemAtTouchStart() {
        return itemClickedInitially;
    }

    public static void setItemAtTouchStart(InventoryItemButton clickStartItem) {
        itemClickedInitially = clickStartItem;
    }
}
