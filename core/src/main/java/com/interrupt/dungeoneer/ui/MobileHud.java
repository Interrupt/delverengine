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
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.drawables.Drawable;
import com.interrupt.dungeoneer.overlays.MapOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.overlays.PauseOverlay;

public class MobileHud extends Hud {

	private MultiTouchButton attackBtn;
	private MultiTouchButton useBtn;
	private MultiTouchButton inventoryBtn;
	private MultiTouchButton mapBtn;
	private MultiTouchButton pauseBtn;

	private boolean wasAttackPressed = false;

	public void refresh() {

		super.refresh();

        // Attack button
		if(attackBtn != null) Game.ui.getActors().removeValue(attackBtn, true);
		attackBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[124]));
		attackBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				Game.instance.player.attackButtonTouched();
			}
		});
		Game.ui.addActor(attackBtn);

        // Use button
		if(useBtn != null) Game.ui.getActors().removeValue(useBtn, true);
		useBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[125]));
		useBtn.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				Game.instance.player.Use(Game.instance.level);
			}
		});
		Game.ui.addActor(useBtn);

        // Inventory button
        if(inventoryBtn != null) Game.ui.getActors().removeValue(inventoryBtn, true);
        inventoryBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[57]));
        inventoryBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Game.instance.toggleInventory();
            }
        });
        Game.ui.addActor(inventoryBtn);

        // Map button
        if(mapBtn != null) Game.ui.getActors().removeValue(mapBtn, true);
        mapBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[61]));
        mapBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                toggleMap();
            }
        });
        // Hide the button to make the minimap show through behind!
        mapBtn.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        Game.ui.addActor(mapBtn);

        // Pause Button, to show the pause menu
        if(pauseBtn != null) Game.ui.getActors().removeValue(pauseBtn, true);
        pauseBtn = new MultiTouchButton(new TextureRegionDrawable(itemTextures[61]));
        pauseBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // Show the pause overlay!
                OverlayManager.instance.push(new PauseOverlay());
            }
        });
        Game.ui.addActor(pauseBtn);

        // Size all of the mobile buttons
		final float uiSize = Game.GetUiSize() * 2.0f;
		attackBtn.setSize(uiSize, uiSize);
		useBtn.setSize(uiSize, uiSize);
        inventoryBtn.setSize(uiSize, uiSize);
        mapBtn.setSize(uiSize, uiSize);
        pauseBtn.setSize(uiSize, uiSize);
	}

	public void tick(GameInput input) {
		if(attackBtn == null || useBtn == null || inventoryBtn == null) GameManager.renderer.initHud();

		super.tick(input);

		final float uiSize = Game.GetUiSize() * 2f;
        final float gutterSize = Gdx.graphics.getWidth() * 0.02f;

		if(attackBtn != null) {
			float btnSize = uiSize + uiSize * (attackBtn.isPressed() ? 0.1f : 0);
			float drawSize = (btnSize - uiSize) / 2f + uiSize;

			attackBtn.setSize(btnSize, btnSize);
			attackBtn.setY((int) btnSize / 10f);
			attackBtn.setX((int) (Gdx.graphics.getWidth() - ((drawSize * 1.5f) + gutterSize)));
		}

		if(useBtn != null) {
			float btnSize = uiSize + uiSize * (useBtn.isPressed() ? 0.1f : 0);
			float drawSize = (btnSize - uiSize) / 2f + uiSize;

			useBtn.setSize(btnSize, btnSize);
			useBtn.setY((int) uiSize * 1.2f);
			useBtn.setX((int) (Gdx.graphics.getWidth() - (drawSize + gutterSize)));
		}

        if(inventoryBtn != null) {
            float btnSize = uiSize + uiSize * (inventoryBtn.isPressed() ? 0.1f : 0);
            float drawSize = (btnSize - uiSize) / 2f + uiSize;

            inventoryBtn.setSize(btnSize, btnSize);
            inventoryBtn.setY((int) Gdx.graphics.getHeight() - (drawSize + gutterSize));
            inventoryBtn.setX((int) (gutterSize));
        }

        if(mapBtn != null) {
            float btnSize = uiSize + uiSize * (mapBtn.isPressed() ? 0.1f : 0);
            float drawSize = (btnSize - uiSize) / 2f + uiSize;

            mapBtn.setSize(btnSize, btnSize);
            mapBtn.setY((int) (Gdx.graphics.getHeight() - (drawSize + gutterSize * 0.5f)));
            mapBtn.setX((int) (Gdx.graphics.getWidth() - (drawSize + gutterSize)));
        }

        if(pauseBtn != null) {
            float btnSize = uiSize + uiSize * (pauseBtn.isPressed() ? 0.1f : 0);
            float drawSize = (btnSize - uiSize) / 2f + uiSize;

            pauseBtn.setSize(btnSize, btnSize);
            pauseBtn.setY((int) Gdx.graphics.getHeight() - (drawSize + gutterSize) - drawSize);
            pauseBtn.setX((int) (gutterSize));
        }
	}

    public void toggleMap() {
        // Toggle map! There is probably a better way to do this, maybe by injecting events into the input system
        if(OverlayManager.instance.current() == null)
            OverlayManager.instance.push(new MapOverlay());
        else
            OverlayManager.instance.clear();

        if (GameManager.renderer.showMap && Game.instance.getShowingMenu()) {
            Game.instance.toggleInventory();
        }

        if (GameManager.renderer.showMap) Audio.playSound("/ui/ui_map_open.mp3", 0.3f);
        else Audio.playSound("/ui/ui_map_close.mp3", 0.3f);
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
}
