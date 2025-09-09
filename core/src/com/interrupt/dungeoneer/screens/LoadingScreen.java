package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;

public class LoadingScreen extends BaseScreen {
	
    
    private int hasDrawn = 0;
    public String text = StringManager.get("screens.LoadingScreen.loadingLabel");
	private int saveSlot = 0;

    private boolean isFadingIn = true;
    private float fadeFactor = 1f;
    private Color fadeColor = new Color(Color.BLACK);

    private boolean didDisposeArt = false;
	
	public LoadingScreen(String loadingMessage, int saveSlot) {
		this.saveSlot = saveSlot;
		text = loadingMessage;
		useBackgroundLevel = false;
	}

	private void refreshArtCaches() {
		didDisposeArt = true;
		GameManager.renderer.disposeMeshes();
		Art.KillCache();
		GameManager.renderer.initTextures();
	}
	
	@Override
	public void show() {
		super.show();

		didDisposeArt = false;
		BaseScreen.freeBackgroundLevel();

		GameManager.renderer.disposeMeshes();
		Art.KillCache();
		GameManager.renderer.initTextures();
    }

	@Override
	public void render(float delta) {
		try {
			if(running) {

				if(!didDisposeArt) {
					refreshArtCaches();

					backgroundTexture = Art.loadTexture("splash/Delver-Creation-BG.png");
					backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
				}

	        	tick(delta);
		        draw(delta);
			}
		} catch (Exception ex) {
			Gdx.app.log("DelverLevelChangeScreen", "Exception: " + ex.getMessage());
		}
	}
	
	public void draw(float delta) {

        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		super.draw(delta);

        gl.glDisable(GL20.GL_CULL_FACE);

        float fontSize = Math.min(renderer.camera2D.viewportWidth, renderer.camera2D.viewportHeight) / 40;
		
		renderer.uiBatch.setProjectionMatrix(renderer.camera2D.combined);
		renderer.uiBatch.begin();

		float yPos = renderer.camera2D.viewportHeight * 0.2f;
		renderer.drawCenteredText(text, -yPos, fontSize * 1.25f, Color.WHITE, Color.DARK_GRAY);
		
		renderer.uiBatch.end();

        if(isFadingIn && fadeFactor > 0f) {
            fadeFactor -= delta * 3.5f;
            renderer.drawFlashOverlay(fadeColor.set(0f, 0f, 0f, 1f * fadeFactor));
        }
		
		hasDrawn++;
	}
	
	public void tick(float delta) {
		if(fadeFactor <= 0) {
			GameApplication.SetSaveLocation(saveSlot);
			GameApplication.ShowMainScreen();
		}
	}
}
