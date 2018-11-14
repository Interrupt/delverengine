package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.GlRenderer;

public abstract class Overlay {
	protected Stage ui;
	protected GlRenderer renderer;
	protected GL20 gl;
	public boolean pausesGame = true;
	public boolean visible = false;
	public boolean running = false;
	public boolean showCursor = true;
	public boolean catchInput = true;
	
	private InputProcessor previousInputProcessor = null;
	
	private boolean cursorWasShownBefore = false;
	
	public Overlay() { }

	public void show() {
		show(true);
	}
	
	public void show(boolean setInputSettings) {
		visible = true;
		running = true;

		if(setInputSettings) {
			cursorWasShownBefore = !Gdx.input.isCursorCatched();

			if (Game.instance == null || !Game.instance.input.usingGamepad) {
				if (showCursor != cursorWasShownBefore) {
					Gdx.input.setCursorCatched(!showCursor);
					Game.instance.input.caughtCursor = !showCursor;
				}
			}

			if(catchInput) {
				if (Gdx.input.getInputProcessor() instanceof GameInput)
					((GameInput) Gdx.input.getInputProcessor()).clear();
				previousInputProcessor = Gdx.input.getInputProcessor();
			}
		}
		
		onShow();
	}
	
	public void hide() {
		visible = false;
		running = false;

		if(showCursor != cursorWasShownBefore) {
			Gdx.input.setCursorCatched(!cursorWasShownBefore);
			Game.instance.input.caughtCursor = !cursorWasShownBefore;
		}
		
		if(previousInputProcessor != null)
			Gdx.input.setInputProcessor(previousInputProcessor);
		
		onHide();
	}
	
	protected void draw(float delta) {
		renderer = GameManager.renderer;
		gl = renderer.getGL();
		
		if(ui != null) {
			if(running) ui.act(delta);
			ui.draw();
		}
	}
	
	public abstract void tick(float delta);
	public abstract void onShow();
	public abstract void onHide();

	public void pause() {
		running = false;
	}
	
	public void resume() {
		running = true;
	}

    public void resize(int width, int height) {
	    if(ui != null && ui.getViewport() != null) {
            Viewport viewport = ui.getViewport();
            viewport.setWorldHeight(height / Game.GetUiSize() * 22f);
            viewport.setWorldWidth(width / Game.GetUiSize() * 22f);
            viewport.update(width, height, true);
        }
    }

	public void matchInputSettings(Overlay existing) {
		previousInputProcessor = existing.previousInputProcessor;
		cursorWasShownBefore = existing.cursorWasShownBefore;
	}
}
