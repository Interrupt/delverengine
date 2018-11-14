package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.Tesselator;
import com.interrupt.dungeoneer.metrics.MetricsCore;
import com.interrupt.dungeoneer.overlays.OverlayManager;

import java.util.Map.Entry;

public class GameScreen implements Screen {
	
	GameManager gameManager;
	
	public GameInput input;
    private boolean running = true;
    
    public boolean saveOnPause = true;
    
    private int curWidth;
    private int curHeight;
    
    public boolean didStart = false;
    public static float cDelta = 0;
    
    public int saveLoc = 0;
    
    private long nextSecond = System.currentTimeMillis() + 1000;
    private int fpsCount = 0;
    
    public static boolean resetDelta = true;
    
    public OverlayManager overlayManager = OverlayManager.instance;

	private Level editorLevel = null;
    
    public GameScreen(Level level, GameManager gameManager, GameInput input) {
    	this.gameManager = gameManager;
		this.input = input;
		this.editorLevel  = level;
    }
	
	public GameScreen(GameManager gameManager, GameInput input) {
		this.gameManager = gameManager;
		this.input = input;
	}

	@Override
	public void render(float delta) {
		cDelta = delta;

		if(running) {
			Game game = GameManager.getGame();

			if(resetDelta) {
				resetDelta = false;
				delta = 1 / 60f;
			}

			// set a maximum time between ticks (12 fps, wouldn't be playable anyway)
			if(delta > 0.083f) delta = 0.083f;

			if(!overlayManager.shouldPauseGame())
			{
				gameManager.tick(delta * 60f);

				if(game != null) {
					// yay speedrunning!
					if (gameManager.running && game.player != null) {
						game.player.updatePlaytime(delta);
						game.progression.updatePlaytime(delta);
					}
				}
			}

			if(game != null)
				game.updateMouseInput();

			// draw the game
			gameManager.render();

			// draw any overlays
			overlayManager.draw(delta);

			if(MetricsCore.enabled) {
				fpsCount++;
				long currentTime = System.currentTimeMillis();
				if(currentTime > nextSecond) {
					Gdx.app.log("DelverMetrics", "FPS: " + String.valueOf(fpsCount));

					for(Entry<String,Long> entry : MetricsCore.getCounts().entrySet()) {
						Gdx.app.log("DelverMetrics", entry.getKey() + ": " + entry.getValue());
					}

					fpsCount = 0;
					nextSecond = System.currentTimeMillis() + 1000;
				}

				MetricsCore.frameReset();
			}
		}
	}
	
	@Override
	public void pause() {
		if(Game.isMobile) {
			Gdx.app.log("DelverGameScreen", "LibGdx Pause");
			doPause();
		}
	}
	
	public void doPause() {
		running = false;
		
		try {
			Audio.steps.stop();
			Audio.torch.stop();
			Audio.music.stop();
			Audio.stopLoopingSounds();
		}
		catch(Exception e) { }
		
		try {
			if(saveOnPause)
				GameManager.getGame().save();
            else if(GameManager.getGame().level != null)
                GameManager.getGame().level.preSaveCleanup();
		}
		catch(Exception ex) {
			
		}
		
		OverlayManager.instance.reset();
	}

	@Override
	public void resize(int width, int height) {
		Gdx.app.log("DelverGameScreen", "LibGdx Resize");
		if(width != curWidth || height != curHeight) {
			curWidth = width;
			curHeight = height;
			GameManager.renderer.setSize(width, height);
			OverlayManager.instance.resize(width, height);
		}
	}

	@Override
	public void resume() {
		if(Game.isMobile) {
			Gdx.app.log("DelverGameScreen", "Resume");
			
			resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			gameManager.init();
			
			didStart = false;
			GameApplication.SetScreen( new MainMenuScreen());
		}
		else {
			running = true;
		}
	}

	@Override
	public void show() {
		Gdx.app.log("DelverGameScreen", "Show");
		
		Audio.disposeAudio("door_beginning.mp3,floor_descend.mp3");
		Audio.init();
		
		if(!didStart) {
			Gdx.app.log("DelverGameScreen", "Starting game");
			
			if(editorLevel == null) gameManager.startGame(saveLoc);
			else gameManager.startGame(editorLevel);
			
			didStart = true;
		}
		
		Gdx.app.log("DelverGameScreen", "Init player");
		running = true;
		Game.instance.player.init();
		saveOnPause = editorLevel == null ? true : false;
		
		if(Options.instance.enableMusic) {
			Gdx.app.log("DelverGameScreen", "Playing music");
			if(Game.instance.player.isHoldingOrb)
				Audio.playMusic(Game.instance.level.actionMusic, Game.instance.level.loopMusic);
			else
				Audio.playMusic(Game.instance.level.music, Game.instance.level.loopMusic);
		}
		
		if(Game.instance.level.ambientSound != null && !Game.isMobile)
			Audio.playAmbientSound(Game.instance.level.ambientSound, Game.instance.level.ambientSoundVolume, 0.1f);
		
		if(Game.isMobile) Gdx.input.setCatchBackKey( true );
		
		Gdx.app.log("DelverGameScreen", "Finished showing");
	}
	
	@Override
	public void hide() {
		Gdx.app.log("DelverGameScreen", "Hide");
		
		doPause();
		
		if(Game.isMobile) Gdx.input.setCatchBackKey( false );
		Audio.stopLoopingSounds();
		
		if(editorLevel != null) GameApplication.editorRunning = false;
	}

	@Override
	public void dispose() {
		Audio.disposeAudio(null);
		if(editorLevel != null) GameApplication.editorRunning = false;
	}
}
