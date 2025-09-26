package com.interrupt.dungeoneer.ui;

import java.util.HashMap;
import java.util.Map.Entry;

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
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.ui.Hud.DragAndDropResult;

public class EquipLoc {

	protected TextureRegion itemTextures[];
	public final HashMap<String, InventoryItemButton> itemButtons = new HashMap<>();
    private boolean isHovered = false;

	public float yOffset = 0;
	public float xOffset = 0;
	public int bgTex = 127;

	public boolean visible = false;

	public Integer lastUiTouchPointer = null;

	public String equipLoc = "ARMOR";

	public EquipLoc(String equipLoc, float xOffset, float yOffset, int tex)
	{
		this.equipLoc = equipLoc;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.bgTex = tex;
	}

	public void init(TextureRegion itemTextures[])
	{
		this.itemTextures = itemTextures;
	}

	public void refresh() {
		for(Entry<String,InventoryItemButton> entry : itemButtons.entrySet()) {
			Game.ui.getActors().removeValue(entry.getValue(), true);
		}

		lastUiTouchPointer = null;
		itemButtons.clear();

		if(Game.ui == null || !visible) return;

		if(Game.instance.player.equippedItems.containsKey(equipLoc))
		{
			Item itm = Game.instance.player.equippedItems.get(equipLoc);

			if(itm != null) {
				TextureAtlas atlas = TextureAtlas.cachedAtlases.get(itm.spriteAtlas);
				if(atlas == null) atlas = TextureAtlas.cachedAtlases.get(itm.artType.toString());
				if(atlas != null) {
                    InventoryItemButton itmButton = new InventoryItemButton(this, null, new TextureRegionDrawable(itm.getInventoryTextureRegion()));
					itemButtons.put(equipLoc, itmButton );
					Game.ui.addActor(itmButton);
				}
			}
		}

		initButtons();
	}

	public void initButtons()
	{
		final float uiSize = Game.GetInventoryUiSize();

		int x = 0;
		int y = 0;

		if(itemButtons.containsKey(equipLoc)) {
			Button hb = itemButtons.get(equipLoc);

			int yPos = (int) (((Gdx.graphics.getHeight() - uiSize) - (int)(y * uiSize)) - (yOffset * uiSize));
			int xPos = (int) ((uiSize * x + Gdx.graphics.getWidth() / 2.0 - uiSize * ( 1 / 2.0 )) + (xOffset * uiSize));

			if(!hb.isPressed())
			{
				hb.setY(yPos);
				hb.setX(xPos);
			}

			hb.setWidth(uiSize);
			hb.setHeight(uiSize);
		}
	}

	public void tickUI(GameInput input)
	{
		Integer uiTouchPointer = input.uiTouchPointer;
		if(uiTouchPointer != null) lastUiTouchPointer = uiTouchPointer;
		else if (lastUiTouchPointer != null) uiTouchPointer = lastUiTouchPointer;
		else uiTouchPointer = 0;

		final float uiSize = Game.GetInventoryUiSize();
		final float xCursorPos = input.getPointerX(uiTouchPointer) - Gdx.graphics.getWidth() / 2.0f;
		final float yCursorPos = input.getPointerY(uiTouchPointer);
        isHovered = false;

		if(!visible) return;

		if(Game.isMobile || !Game.instance.input.caughtCursor) {
			int x = 0;
			int y = 0;

			float xPos1 = -((uiSize * 1) / 2.0f) + uiSize * x;
			float xPosD = xPos1 + (xOffset * uiSize);

			if((Game.isMobile || !input.caughtCursor) && xCursorPos > xPosD && xCursorPos <= xPosD + uiSize && yCursorPos <= ((y + 1) * uiSize) + (yOffset * uiSize) && yCursorPos > (y * uiSize) + (yOffset * uiSize))
			{
				isHovered = true;
			}
		}

		if(itemButtons.containsKey(equipLoc)) {
			InventoryItemButton hb = itemButtons.get(equipLoc);

			int x = 0;
			int y = 0;

			int yPos = (int) (((Gdx.graphics.getHeight() - uiSize) - (int)(y * uiSize)) - (yOffset * uiSize));
			int xPos = (int) ((uiSize * x + Gdx.graphics.getWidth() / 2.0 - uiSize * ( 1 / 2.0 )) + (xOffset * uiSize));

			hb.setWidth(uiSize);
			hb.setHeight(uiSize);

            if(!hb.isBeingDragged) {
                hb.setY(yPos);
                hb.setX(xPos);
            }
		}
	}

    public boolean isHovered() {
        return isHovered;
    }
}
