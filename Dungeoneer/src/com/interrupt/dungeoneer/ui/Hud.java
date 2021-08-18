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

	MultiTouchButton draggingBtn = null;
	public Item dragging = null;

	private Integer lastUiTouchPointer;

	public ArrayMap<String,EquipLoc> equipLocations = new ArrayMap<String,EquipLoc>();

	public enum DragAndDropResult { equip, drop, invalid, ignore };

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

		if(dragging != null) {
			TextureAtlas atlas = TextureAtlas.cachedAtlases.get(dragging.spriteAtlas);
			if(atlas == null) atlas = TextureAtlas.cachedAtlases.get("item");

			draggingBtn = new MultiTouchButton(new TextureRegionDrawable(dragging.getInventoryTextureRegion()));
			Game.ui.addActor(draggingBtn);
		} else if(draggingBtn != null) {
			Game.ui.getActors().removeValue(draggingBtn, true);
			draggingBtn = null;
		}

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

		inventoryBtn.setWidth(inventoryBtnSize);
		inventoryBtn.setHeight(inventoryBtnSize);

		mapBtn.setWidth(uiSize * 1.4f);
		mapBtn.setHeight(uiSize * 1.4f);

		Integer uiTouchPointer = input.uiTouchPointer;
		if(uiTouchPointer != null) lastUiTouchPointer = uiTouchPointer;
		else if (lastUiTouchPointer != null) uiTouchPointer = lastUiTouchPointer;
		else uiTouchPointer = 0;

		if(draggingBtn != null && input.uiTouchPointer != null) {
			draggingBtn.setWidth(uiSize);
			draggingBtn.setHeight(uiSize);

			draggingBtn.setY(-Game.instance.input.getPointerY(uiTouchPointer) + Gdx.graphics.getHeight() - uiSize / 2);
			draggingBtn.setX(Game.instance.input.getPointerX(uiTouchPointer) - uiSize / 2);
		}
		else if(draggingBtn != null)
		{
			String equipOverSlot = null;
			for(EquipLoc loc : equipLocations.values())
			{
				equipOverSlot = loc.getMouseOverSlot();
				if(equipOverSlot != null) break;
			}

			if(Game.hudManager.quickSlots.getMouseOverSlot() != null) {
				Game.DragAndDropInventoryItem(dragging, null, null);
				Game.hudManager.quickSlots.refresh();
				dragging = null;
			}
			else if(Game.hudManager.backpack.getMouseOverSlot() != null) {
				Game.DragAndDropInventoryItem(dragging, null, null);
				Game.hudManager.backpack.refresh();
				dragging = null;
			}
			else if(equipOverSlot != null) {
				DragAndDropResult res = Game.DragAndDropInventoryItem(dragging, null, null);

				if(res == DragAndDropResult.invalid)
				{
					Game.instance.player.throwItem(dragging, Game.instance.level, 0, 0);
					dragging.isActive = true;
				}

				dragging = null;

				refresh();
			}
			else if(Game.instance.input.getPointerX(uiTouchPointer) > Gdx.graphics.getWidth() - uiSize && Game.instance.input.getPointerY(uiTouchPointer) < uiSize) {
				// dropping item into inventory
				boolean foundSlot = false;
				for(int i = 6; i < Game.instance.player.inventorySize; i++) {
					if(Game.instance.player.inventory.get(i) == null) {
						foundSlot = true;
						Game.instance.player.inventory.set(i, dragging);

						if(dragging instanceof QuestItem) {
							((QuestItem)dragging).doQuestThing();
						}

						dragging = null;

						Game.hudManager.backpack.refresh();
					}
				}
				if(!foundSlot) Game.ShowMessage(StringManager.get("ui.Hud.noRoomText"), 0.6f, 1f);
			}
			else {

				Vector3 levelIntersection = new Vector3();
				Ray ray = Game.camera.getPickRay(Game.instance.input.getPointerX(uiTouchPointer), Game.instance.input.getPointerY(uiTouchPointer));
				float distance = 0;
				if(Collidor.intersectRayTriangles(ray, GameManager.renderer.GetCollisionTrianglesAlong(ray, 20f), levelIntersection, null)) {
					distance = ray.origin.sub(levelIntersection).len();
				}

				Game.instance.player.throwItem(dragging, Game.instance.level, 0, 0);

				dragging.xa = ray.direction.x * 0.28f * Math.min(1, distance / 6.0f);
				dragging.za = ray.direction.y * 0.5f * Math.min(1, distance / 6.0f) + 0.04f;
				dragging.ya = ray.direction.z * 0.28f * Math.min(1, distance / 6.0f);
				dragging = null;
			}

			refresh();
		}

		int yPos = (int) (Gdx.graphics.getHeight() - uiSize);
		int xPos = (int) (Gdx.graphics.getWidth() - uiSize);
		inventoryBtn.setY(yPos);
		inventoryBtn.setX(xPos);

		mapBtn.setX(0);
		mapBtn.setY((int) (Gdx.graphics.getHeight() - mapBtn.getHeight()));
	}

	public Integer getMouseOverSlot()
	{
		if(Game.hudManager.backpack.getMouseOverSlot() != null)
			return Game.hudManager.backpack.getMouseOverSlot() + Game.hudManager.backpack.invOffset;

		if(Game.hudManager.quickSlots.getMouseOverSlot() != null)
			return Game.hudManager.quickSlots.getMouseOverSlot() + Game.hudManager.quickSlots.invOffset;

		return null;
	}

	public Item getMouseOverItem()
	{
		Integer bagOver = Game.hudManager.backpack.getMouseOverSlot();
		if(bagOver != null) return Game.instance.player.inventory.get(bagOver + Game.hudManager.backpack.invOffset);

		Integer hotbarOver = Game.hudManager.quickSlots.getMouseOverSlot();
		if(hotbarOver != null) return Game.instance.player.inventory.get(hotbarOver + Game.hudManager.quickSlots.invOffset);

		for(EquipLoc loc : equipLocations.values())
		{
			if(loc.getMouseOverSlot() != null) {
				return Game.instance.player.equippedItems.get(loc.equipLoc);
			}
		}

		return null;
	}

	public boolean isAttackPressed() { return false; }
}
