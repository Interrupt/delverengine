package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Stairs;
import com.interrupt.dungeoneer.entities.Stairs.StairDirection;
import com.interrupt.dungeoneer.entities.triggers.TriggeredWarp;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.Logger;

import java.text.MessageFormat;

public class LevelChangeScreen extends BaseScreen {

	public String text = StringManager.get("screens.LoadingScreen.loadingLabel");

	protected GameManager dungeoneerComponent;
    
    private int hasDrawn = 0;
    private boolean hasLoaded = false;

    public int levelNum = 1;
	public Stairs stair = null;
	public TriggeredWarp triggeredWarp = null;

	public String backgroundTextureFile = "splash/DungeonLoadingScreen.png";

	private boolean didDisposeArt = false;
	
	public LevelChangeScreen(GameManager dungeoneerComponent) {
		this.dungeoneerComponent = dungeoneerComponent;
		useBackgroundLevel = false;
		showMouse = false;
	}

	private void refreshArtCaches() {
		didDisposeArt = true;
		if(triggeredWarp == null || triggeredWarp.unloadParentLevel) {
			GameManager.renderer.disposeMeshes();
			Art.KillCache();
			GameManager.renderer.initTextures();
		}
	}

	@Override
	public void show() {
		super.show();

		hasLoaded = false;
		hasDrawn = 0;

		didDisposeArt = false;

		// update the level number
		if (stair != null) {
			levelNum = Game.instance.player.levelNum + 1;
			if (stair.direction == StairDirection.down)
				levelNum++;
			else
				levelNum--;
		}

		setInfoFromLevel();

		if(stair != null) {
			if (stair.direction == StairDirection.down && levelNum == 2) {
				Audio.playSound("door_beginning.mp3", 0.78f);
			} else {
				Audio.playSound("floor_descend.mp3", 0.6f);
			}
		}
		else if(triggeredWarp != null) {
			Audio.playSound(triggeredWarp.levelChangeSound, 0.7f);
		}
	}

	public void setInfoFromLevel() {
		if(stair != null) {
			Array<Level> levels = Game.buildLevelLayout();

			Level l = null;
			try {
				l = levels.get(levelNum - 1);
			}
			catch(Exception ex) {
				Gdx.app.error("Delver", ex.getMessage());
			}

			// Set the level name text
			if(l != null) {
				text = l.levelName;

				if(l.loadingScreenBackground != null && !l.loadingScreenBackground.isEmpty()) {
					backgroundTextureFile = l.loadingScreenBackground;
				}
			}
			else {
				text = MessageFormat.format(StringManager.get("game.Game.levelNameText"), levelNum - 1);
			}
		}

		if(triggeredWarp != null) {
			text = triggeredWarp.levelName;

			if(triggeredWarp.loadingScreenBackground != null && !triggeredWarp.loadingScreenBackground.isEmpty())
				backgroundTextureFile = triggeredWarp.loadingScreenBackground;
		}
	}

	@Override
	public void render(float delta) {
		try {
			if(running) {

				if(!didDisposeArt) {
					refreshArtCaches();

					backgroundTexture = Art.loadTexture(backgroundTextureFile);
					backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
				}

				tick(delta);
				draw(delta);
			}
		} catch (Exception ex) {
			Logger.logExceptionToFile(ex);
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

		hasDrawn++;
	}
	
	public void tick(float delta) {
		if(hasDrawn > 4 && !hasLoaded) {
			hasLoaded = true;

			if(stair != null) {
				Game.instance.doLevelChange(stair);
			}
			else {
				if(triggeredWarp.isExit) {
					Game.instance.doLevelExit(triggeredWarp);
				}
				else {
					Game.instance.warpToLevel(triggeredWarp.travelPath, triggeredWarp);
				}
			}

			GameApplication.ShowMainScreen();
			GameScreen.resetDelta = true;
			Game.flash(Color.BLACK, 30);
		}
	}

}
