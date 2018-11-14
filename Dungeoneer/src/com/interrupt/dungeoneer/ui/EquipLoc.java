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
	public final HashMap<String, MultiTouchButton> itemButtons = new HashMap<String, MultiTouchButton>();
	public final HashMap<String, Boolean> wasPressedLast = new HashMap<String, Boolean>();
	public final HashMap<String, Boolean> isDragging = new HashMap<String, Boolean>();
	private String mouseOverSlot = null;
	
	public float yOffset = 0;
	public float xOffset = 0;
	public int bgTex = 127;
	
	public boolean visible = false;
	
	public Item dragging = null;
	
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
		for(Entry<String,MultiTouchButton> entry : itemButtons.entrySet()) {
			Game.ui.getActors().removeValue(entry.getValue(), true);
		}
		
		lastUiTouchPointer = null;
		
		itemButtons.clear();
		wasPressedLast.clear();
		isDragging.clear();
		
		if(Game.ui == null || !visible) return;
		
		if(Game.instance.player.equippedItems.containsKey(equipLoc))
		{
			Item itm = Game.instance.player.equippedItems.get(equipLoc);
			
			if(itm != null) {
				TextureAtlas atlas = TextureAtlas.cachedAtlases.get(itm.spriteAtlas);
				if(atlas == null) atlas = TextureAtlas.cachedAtlases.get(itm.artType.toString());
				if(atlas != null) {
					TextureRegion region = atlas.getSprite(itm.getInventoryTex());
					
					MultiTouchButton itmButton = new MultiTouchButton(new TextureRegionDrawable(itm.getInventoryTextureRegion()), new TextureRegionDrawable(itm.getInventoryTextureRegion()));
					itemButtons.put(equipLoc, itmButton );
					
					Game.ui.addActor(itmButton);
					
					itmButton.addListener(
						new ClickListener() {
							@Override
							public void clicked(InputEvent event, float x, float y) {
								if(Game.isMobile || !Game.instance.input.caughtCursor)
								{
									if(!isDragging.containsKey(equipLoc) || !isDragging.get(equipLoc))
									{
										// TODO: equip, unequip here
										//Game.instance.player.DoHotbarAction(btnLoc + 1);
									}
								}
							}
					    }
					);
				}
			}
		}
		
		initButtons();
	}
	
	public void initButtons()
	{
		final float uiSize = Game.GetUiSize();
		
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
		
		final float uiSize = Game.GetUiSize();
		final float xCursorPos = input.getPointerX(uiTouchPointer) - Gdx.graphics.getWidth() / 2.0f;
		final float yCursorPos = input.getPointerY(uiTouchPointer);
		mouseOverSlot = null;
		dragging = null;
		
		if(!visible) return;
		
		if(Game.isMobile || !Game.instance.input.caughtCursor) {
			int x = 0;
			int y = 0;
			
			float xPos1 = -((uiSize * 1) / 2.0f) + uiSize * x;
			float xPosD = xPos1 + (xOffset * uiSize);
			
			if((Game.isMobile || !input.caughtCursor) && xCursorPos > xPosD && xCursorPos <= xPosD + uiSize && yCursorPos <= ((y + 1) * uiSize) + (yOffset * uiSize) && yCursorPos > (y * uiSize) + (yOffset * uiSize))
			{
				mouseOverSlot = equipLoc;
			}
		}
		
		if(itemButtons.containsKey(equipLoc)) {
			MultiTouchButton hb = itemButtons.get(equipLoc);
			
			int x = 0;
			int y = 0;
			
			int yPos = (int) (((Gdx.graphics.getHeight() - uiSize) - (int)(y * uiSize)) - (yOffset * uiSize));
			int xPos = (int) ((uiSize * x + Gdx.graphics.getWidth() / 2.0 - uiSize * ( 1 / 2.0 )) + (xOffset * uiSize));
			
			hb.setWidth(uiSize);
			hb.setHeight(uiSize);
			
			if(hb.isDragging)
			{
				
				if(Game.isMobile || !Game.instance.input.caughtCursor)
				{							
					if( (isDragging.containsKey(equipLoc) && isDragging.get(equipLoc) ) || Math.abs(input.getUiTouchPosition().x - input.getPointerX(uiTouchPointer)) > uiSize / 8 || Math.abs(input.getUiTouchPosition().y - input.getPointerY(uiTouchPointer)) > uiSize / 8) {
						hb.setY(-input.getPointerY(uiTouchPointer) + Gdx.graphics.getHeight() - uiSize / 2);
						hb.setX(input.getPointerX(uiTouchPointer) - uiSize / 2);
						isDragging.put(equipLoc, true);
						
						dragging = Game.instance.player.equippedItems.get(equipLoc);
						Game.dragging = dragging;
					}
				}
				
				if(!isDragging.containsKey(equipLoc) || !isDragging.get(equipLoc)) {
					hb.setX(xPos);
					hb.setY(yPos);
				}
			}
			else
			{
				// item was dragged out of the equip slot, switch inv slot or drop item
				if(wasPressedLast.containsKey(equipLoc) && wasPressedLast.get(equipLoc)) {
					
					DragAndDropResult movedItem = Game.DragAndDropInventoryItem( Game.instance.player.equippedItems.get(equipLoc), null, equipLoc );
					
					if(movedItem == DragAndDropResult.drop)
					{
						Item dragging = Game.instance.player.equippedItems.get(equipLoc);
						Game.instance.player.equippedItems.put(equipLoc, null);
						
						Vector3 levelIntersection = new Vector3();
						Ray ray = Game.camera.getPickRay(input.getPointerX(uiTouchPointer), input.getPointerY(uiTouchPointer));
						float distance = 0;
						if(Collidor.intersectRayTriangles(ray, GameManager.renderer.GetCollisionTrianglesAlong(ray, 20f), levelIntersection, null)) {
							distance = ray.origin.sub(levelIntersection).len();
						}
						
						Game.instance.player.throwItem(dragging, Game.instance.level, 0, 0);
						
						dragging.xa = ray.direction.x * 0.28f * Math.min(1, distance / 6.0f);
						dragging.za = ray.direction.y * 0.5f * Math.min(1, distance / 6.0f) + 0.04f;
						dragging.ya = ray.direction.z * 0.28f * Math.min(1, distance / 6.0f);
					}
					
					Game.RefreshUI();
				}
				
				hb.setY(yPos);
				hb.setX(xPos);
			}
			
			if(Game.isMobile || !Game.instance.input.caughtCursor) {
				wasPressedLast.put(equipLoc, hb.isDragging);
			}
		}
	}
	
	public String getMouseOverSlot() {
		return mouseOverSlot;
	}
}
