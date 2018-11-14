package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;

public class GameOverScreen extends StatsScreen {
	
	protected GameManager dungeoneerComponent;
	private int curWidth;
    private int curHeight;
    public boolean gameOver = false;

    protected float tickTime = 0f;
    protected float shownTime = 0;
    protected float uiScale = 3.75f;
    
    private int gameOverProgress = 0;
    private float gameOverTimer = 0;

    protected Color backgroundColor = new Color(0.6f, 0, 0, 1);

    protected boolean fadingOut = false;
    protected float fadeTime = 0f;

    protected Timer delayTimer = new Timer();

    protected boolean doTick = false;

    private boolean firstDeath = false;

    private boolean dealtAchievements = false;
    
    private String[] winTexts = {
            StringManager.get("screens.GameOverScreen.win_text_0"),
            StringManager.get("screens.GameOverScreen.win_text_1"),
            StringManager.get("screens.GameOverScreen.win_text_2"),
            StringManager.get("screens.GameOverScreen.win_text_3"),
            StringManager.get("screens.GameOverScreen.win_text_4"),
            StringManager.get("screens.GameOverScreen.win_text_5"),
            StringManager.get("screens.GameOverScreen.win_text_6"),
            StringManager.get("screens.GameOverScreen.win_text_7")
    };

    private String pickedWinText = "";

    public GameOverScreen() { }
	
	public GameOverScreen(GameManager dungeoneerComponent) {
		this.dungeoneerComponent = dungeoneerComponent;
		
		skin = UiSkin.getSkin();
		ui = new Stage(getViewport(Gdx.graphics.getWidth() / uiScale, Gdx.graphics.getHeight() / uiScale));

        splashLevel = "levels/death-screen-splash.bin";
        splashCameraSway = false;

        splashCameraLookAt.set(4f,0.8f,4f);
        splashCameraPosition.set(4.5f, 1f, 4f);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void hide() {
		running = false;
	}

	@Override
	public void pause() {
		running = false;
	}

    @Override
    public void draw(float delta) {
		GlRenderer renderer = GameManager.renderer;
		
		if(gameOver) {
            backgroundColor.set(0.6f, 0, 0, 1);
		} else {
            backgroundColor.set(0f, 0f, 0f, 1);
		}

        drawLevel(delta);

        if(level != null) {
            splashCameraLookAt.set(3f,0.8f,3f);
            splashCameraPosition.set(3.5f, 1f + (1.25f - Interpolation.circleOut.apply(Math.min(tickTime * 0.1f, 1f)) * 1.25f), 5f);
            splashCameraPosition.add((float)Math.sin(tickTime * 0.06f) * 0.5f - 0.25f, 0f, 0f);
        }

        float showTime = Math.min(tickTime, 1f);
        if(showTime < 1f) {
            backgroundColor.a = 1f - showTime;
            renderer.drawFlashOverlay(backgroundColor);
        }

		float fontSize = Math.min(renderer.camera2D.viewportWidth, renderer.camera2D.viewportHeight) / 15;

		renderer.uiBatch.setProjectionMatrix(renderer.camera2D.combined);
		renderer.uiBatch.begin();

        renderer.drawText("", 0, 0, 1f, Color.WHITE);

		if(!gameOver) {
			if(gameOverProgress == 0) {
				final String winText = StringManager.get("screens.GameOverScreen.delverEscapedText");
				float xOffset = winText.length() / 2.0f;
				float yPos = (int)((1 * -fontSize * 1.2) / 2 + fontSize * 1.2);

				yPos -= fontSize * 2f;

				renderer.drawText(winText, -xOffset * fontSize + fontSize * 0.1f, -yPos, fontSize, Color.BLACK);
				renderer.drawText(winText, -xOffset * fontSize, -yPos, fontSize, Color.WHITE);

				yPos += fontSize * 0.5f;
				renderer.drawText(StringManager.get("screens.GameOverScreen.recoveredOrbText"), -xOffset * fontSize + fontSize * 0.1f, -yPos - fontSize, fontSize / 1.5f, Color.LIGHT_GRAY);
				yPos += fontSize * 0.5f;
				renderer.drawText(pickedWinText, -xOffset * fontSize + fontSize * 0.1f, -yPos - fontSize * 2, fontSize / 1.5f, Color.LIGHT_GRAY);
			}
		}

		renderer.uiBatch.end();
		
		if(gameOverProgress >= 0) {
			ui.draw();
		}

        if(fadingOut) {
            float fadeOutTime = Math.min(fadeTime, 1f);
            backgroundColor.set(0f, 0f, 0f, fadeOutTime);
            renderer.drawFlashOverlay(backgroundColor);

            if(fadeOutTime >= 1f) {
                gl.glClearColor(0f, 0f, 0f, 1);
            }
        }
	}

	public void drawLevel(float delta) {
        super.draw(delta);
    }

	@Override
	public void resume() {
		//dungeoneerComponent.init();
		running = true;
	}

	@Override
	public void show() {

        // save the updated progress
        final Progression progression = Game.instance.progression;
        progression.gold = Game.instance.player.gold;
        progression.won = !gameOver;

        if(gameOver) {
            progression.deaths++;
        }
        else {
            progression.wins++;
        }

        firstDeath = progression.deaths == 1 && progression.wins == 0;

        Game.saveProgression(progression, Game.instance.getSaveSlot());

        Timer.Task startFade = new Timer.Task() {
            @Override
            public void run() {
                doTick = true;

                if(gameOver) {
                    if(progression.deaths > 1)
                        Audio.playMusic("gameover.mp3", false);
                }
                else {
                    Audio.playMusic("win.mp3", false);
                }
            }
        };

        doTick = false;
        delayTimer = new Timer();
        delayTimer.scheduleTask(startFade, 0.1f);
        delayTimer.start();

        if(gameOver) {
            splashLevel = "levels/death-screen-splash.bin";
            if(firstDeath) {
                splashLevel = "levels/first-death-screen-splash.bin";
            }
        }
        else {
            splashLevel = "levels/win-screen-splash.bin";
            pickedWinText = winTexts[Game.rand.nextInt(winTexts.length)];
        }

        super.show();

        tickTime = 0f;
        fadingOut = false;
        fadeTime = 0f;

		Gdx.input.setCursorCatched(true);
		running = true;
		
		gameOverProgress = 0;
        statNumber = 0;

        if(gameOver) {
            makeDeathText(firstDeath);
        }
        else {
            ui.clear();
        }
	}

    public void makeDeathText(boolean firstDeath) {
        ui.clear();

        // display some stats
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        Table innerTable = new Table();

        String mainLabelLookup = "screens.GameOverScreen.goodbyeDelverText";
        String subLabelLookup = "screens.GameOverScreen.betterLuckText";

        if(firstDeath) {
            mainLabelLookup = "screens.GameOverScreen.tutorialText1";
            subLabelLookup = "screens.GameOverScreen.tutorialText2";
        }

        Label mainText = new Label(StringManager.get(mainLabelLookup), skin.get(LabelStyle.class));
        mainText.setColor(1, 1, 1, 1);
        mainText.setFontScale(1.495f);
        mainText.setAlignment(Align.center);
        mainText.addAction(Actions.sequence(Actions.fadeOut(0.00001f), Actions.delay(3.2f), Actions.fadeIn(0.5f)));

        Label subText = new Label(StringManager.get(subLabelLookup), skin.get(LabelStyle.class));
        subText.setColor(0.6f, 0, 0, 1);
        subText.setFontScale(0.935f);
        mainText.setAlignment(Align.center);
        subText.addAction(Actions.sequence(Actions.fadeOut(0.00001f), Actions.delay(4.5f), Actions.fadeIn(0.5f)));

        innerTable.add(mainText).padBottom(6f);
        innerTable.row();
        innerTable.add(subText);

        innerTable.pack();
        mainTable.add(innerTable);
        ui.addActor(mainTable);

        innerTable.addAction(Actions.sequence(Actions.moveBy(0f, -5f, 0.00001f), Actions.delay(3.2f), Actions.moveBy(0f, 20f, 0.5f, Interpolation.circleOut)));

        ui.act(0.0001f);
    }
	
	public void makeStats(Integer progress, boolean doFade) {
	    if(firstDeath) {
	        fadingOut = true;
	        return;
        }

		super.makeStats(progress, doFade);

        // Achievements!
        dealAchievements();
	}

    private void dealAchievements() {
	    if(dealtAchievements) return;
        dealtAchievements = true;

        if(gameOver) {
            SteamApi.api.achieve("DIED");
        }
        else {
            SteamApi.api.achieve("WON");
        }

        int goldThisRun = Game.instance.player.gold - Game.instance.progression.goldAtStartOfRun;
        if(goldThisRun > 300) {
            SteamApi.api.achieve("RUN_GOLD");
        }
    }

    public void startGameOver() {
        freeBackgroundLevel();

        if(!gameOver) {
            GameApplication.ShowMainMenuScreen();
        }
        else {
            GameApplication.instance.mainScreen.didStart = false;
            GameApplication.SetScreen(new LoadingScreen("LOADING", Game.instance.getSaveSlot()));
        }
    }

    @Override
	public void tick(float delta) {

        if(!doTick) return;

        tickTime += delta;

        Game.gamepadManager.menuMode = true;

        // tick the background level, if one is loaded
        tick_level(delta);

        ui.act(delta);

		shownTime += delta;
		if(shownTime < 1f || (shownTime < 3.5f && gameOverProgress == 0)) return;

        if(fadingOut) {
            fadeTime += delta * 0.4f;

            if(fadeTime > 1.1f) {
                startGameOver();
            }

            return;
        }

		// quit
		if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.justTouched() || Game.gamepadManager.controllerState.buttonEvents.size > 0) {
			shownTime = 0.5f;
			gameOverProgress++;
			if(gameOverProgress > 1) {
                fadingOut = true;
                makeStats(10, false);
			}
			else {
				makeStats(10, true);
			}
		}
        Game.gamepadManager.tick(delta);

        if(gameOverProgress >= 1) {
            gameOverTimer += delta;

            if(gameOverProgress <= 10 && gameOverTimer >= 0.05f) {
                gameOverProgress++;
                gameOverTimer = 0;

            }
        }
	}

}
