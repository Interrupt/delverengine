package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.game.Game;

public class MobileHud extends Hud {
	
	private MultiTouchButton attackBtn;
	private MultiTouchButton useBtn;
	
	private boolean wasAttackPressed = false;
		
	public void refresh() {
		
		super.refresh();
		
		if(attackBtn != null) Game.ui.getActors().removeValue(attackBtn, true);
		attackBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[124]));
		attackBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				Game.instance.player.attackButtonTouched();
			}			
		});
		Game.ui.addActor(attackBtn);
		
		if(useBtn != null) Game.ui.getActors().removeValue(useBtn, true);
		useBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[125]));
		useBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				Game.instance.player.Use(Game.instance.level);
			}			
		});
		Game.ui.addActor(useBtn);
		
		final float uiSize = Game.GetUiSize();
		attackBtn.setWidth(uiSize);
		attackBtn.setHeight(uiSize);
		
		useBtn.setWidth(uiSize);
		useBtn.setHeight(uiSize);
	}
	
	public void tick(GameInput input) {
		if(attackBtn == null || useBtn == null || inventoryBtn == null) GameManager.renderer.initHud();
		
		super.tick(input);
				
		final float uiSize = Game.GetUiSize() * 1.5f;
		
		if(attackBtn != null) {
			float btnSize = uiSize + uiSize * (attackBtn.isPressed() ? 0.1f : 0);
			float drawSize = (btnSize - uiSize) / 2f + uiSize;
			
			attackBtn.setWidth(btnSize);
			attackBtn.setHeight(btnSize);
			attackBtn.setY((int) btnSize / 10f);
			attackBtn.setX((int) (Gdx.graphics.getWidth() - drawSize * 1.5f));
		}
		
		if(useBtn != null) {
			float btnSize = uiSize + uiSize * (useBtn.isPressed() ? 0.1f : 0);
			float drawSize = (btnSize - uiSize) / 2f + uiSize;
			
			useBtn.setWidth(btnSize);
			useBtn.setHeight(btnSize);
			useBtn.setY((int) uiSize * 1.2f);
			useBtn.setX((int) (Gdx.graphics.getWidth() - drawSize * 1f));
		}
	}
	
	public boolean isAttackPressed() {
		if(attackBtn == null) return false;
		if(attackBtn.isPressed()) {
			wasAttackPressed = true;
			return true;
		}
		
		if(Game.instance.input.getRightTouchPosition() != null && Game.instance.input.isRightTouched()) {
			Vector2 touchPos = Game.instance.input.getRightTouchPosition();
			if(Math.abs(touchPos.x - (attackBtn.getX() + attackBtn.getWidth())) < attackBtn.getWidth() && Math.abs(touchPos.y - (Gdx.graphics.getHeight() - attackBtn.getY())) < attackBtn.getHeight())
				return true;
		}
		
		if(Game.instance.input.uiTouchPointer != null && wasAttackPressed) return true;
		
		wasAttackPressed = false;
		return false;
	}
	
	public void init(TextureRegion itemTextures[])
	{
		this.itemTextures = itemTextures;
		
		equipLocations.put( "HAT", new EquipLoc("HAT", -3.73f, 1.52f, 103) );
		equipLocations.put( "ARMOR", new EquipLoc("ARMOR", -3.73f, 2.66f, 119) );
		equipLocations.put( "PANTS", new EquipLoc("PANTS", -3.73f, 3.79f, 111) );
		
		equipLocations.put( "OFFHAND", new EquipLoc("OFFHAND", 3.73f, 1.52f, 102) );
		equipLocations.put( "RING", new EquipLoc("RING", 3.73f, 2.66f, 118) );
		equipLocations.put( "AMULET", new EquipLoc("AMULET", 3.73f, 3.79f, 110) );
		
		for(EquipLoc loc : equipLocations.values())
		{
			loc.init(itemTextures);
		}
		
		refresh();
	}
}
