package com.interrupt.dungeoneer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.entities.Stairs;
import com.interrupt.dungeoneer.entities.triggers.TriggeredWarp;
import com.interrupt.dungeoneer.game.GameData;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.screens.*;
import com.interrupt.utils.JsonUtil;

public class GameApplication extends Game {

	protected GameManager gameManager = null;
	public GameInput input = new GameInput();

    public GameScreen mainScreen;
    public GameOverScreen gameoverScreen;
    public LevelChangeScreen levelChangeScreen;
    public SplashScreen mainMenuScreen;

    public WinScreen winScreen;

    public static GameApplication instance;
    public static boolean editorRunning = false;

	@Override
	public void create() {
		instance = this;
		Gdx.app.log("DelverLifeCycle", "LibGdx Create");

        Gdx.app.setLogLevel(Application.LOG_INFO);

		gameManager = new GameManager(this);
        Gdx.input.setInputProcessor( input );
        gameManager.init();

        mainMenuScreen = new SplashScreen();
        mainScreen = new GameScreen(gameManager, input);
        gameoverScreen = new GameOverScreen(gameManager);
        levelChangeScreen = new LevelChangeScreen(gameManager);
        winScreen = new WinScreen(gameManager);

        setScreen(new SplashScreen());
	}

	public void createFromEditor(Level level) {
		instance = this;
		Gdx.app.log("DelverLifeCycle", "LibGdx Create From Editor");

		gameManager = new GameManager(this);
        Gdx.input.setInputProcessor( input );
        gameManager.init();

		com.interrupt.dungeoneer.game.Game.inEditor = true;
        mainMenuScreen = new SplashScreen();
        mainScreen = new GameScreen(level, gameManager, input);
        gameoverScreen = new GameOverScreen(gameManager);
        levelChangeScreen = new LevelChangeScreen(gameManager);

        setScreen(mainScreen);
	}

	@Override
	public void dispose() {
		Gdx.app.log("DelverLifeCycle", "Goodbye");
		mainScreen.dispose();
		SteamApi.api.dispose();
	}

	public static void ShowMainScreen() {
		Gdx.input.setInputProcessor( instance.input );
		instance.setScreen(instance.mainScreen);
	}

	public static void ShowGameOverScreen(boolean escaped) {

		// Only show the ending level once!
		if(escaped) {
			GameData gameData = JsonUtil.fromJson(GameData.class, com.interrupt.dungeoneer.game.Game.findInternalFileInMods("data/game.dat"));
			Level endingLevel = gameData.endingLevel;

			// Warp to the ending level, if we're not there already.
			if(endingLevel != null && (GameManager.getGame().level.levelFileName == null || !GameManager.getGame().level.levelFileName.equals(endingLevel.levelFileName))) {
				// Make a warp for this ending level!
				TriggeredWarp warp = new TriggeredWarp();
				warp.generated = endingLevel.generated;
				warp.levelToLoad = endingLevel.levelFileName;
				warp.levelTheme = endingLevel.theme;
				warp.fogColor.set(endingLevel.fogColor);
				warp.fogEnd = endingLevel.fogEnd;
				warp.fogStart = endingLevel.fogStart;
				warp.fogEnd = endingLevel.viewDistance;
				warp.levelName = endingLevel.levelName;
				warp.spawnMonsters = endingLevel.spawnMonsters;
				warp.objectivePrefabToSpawn = endingLevel.objectivePrefab;
				warp.skyLightColor = endingLevel.skyLightColor;
				warp.music = endingLevel.music;
				warp.ambientSound = endingLevel.ambientSound;
				// Warp must be initialized to work correctly.
				warp.init(endingLevel, Level.Source.SPAWNED);

				GameManager.getGame().player.makeEscapeEffects = false;
				GameManager.getGame().warpToLevel("ending", warp);

				return;
			}
		}

		GameManager.getGame().gameOver = true;
		instance.gameoverScreen.gameOver = !escaped;

		if(escaped) {
			instance.setScreen(instance.winScreen);
		}
		else {
			instance.setScreen(instance.gameoverScreen);
		}
	}

	public static void ShowLevelChangeScreen(Stairs stair) {
		instance.levelChangeScreen.stair = stair;
		instance.levelChangeScreen.triggeredWarp = null;
		instance.mainScreen.saveOnPause = false;

		instance.setScreen(instance.levelChangeScreen);
	}

	public static void ShowLevelChangeScreen(TriggeredWarp warp) {
		instance.levelChangeScreen.triggeredWarp = warp;
		instance.levelChangeScreen.stair = null;
		instance.mainScreen.saveOnPause = false;

		instance.setScreen(instance.levelChangeScreen);
	}

	public static void SetScreen(Screen newScreen) {
		instance.setScreen(newScreen);
	}

	public static void SetSaveLocation(int saveLoc) {
		instance.mainScreen.saveLoc = saveLoc;
	}

	public static void ShowMainMenuScreen() {
		instance.mainScreen.didStart = false;
		instance.setScreen(new MainMenuScreen());
	}
}
