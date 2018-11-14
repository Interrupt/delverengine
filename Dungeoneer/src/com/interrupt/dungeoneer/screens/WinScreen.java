package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.helpers.Message;
import com.interrupt.managers.MessageManager;

public class WinScreen extends StatsScreen {

    private GameManager dungeoneerComponent;
    public boolean doTick;
    public float tickTime;
    protected Message credits = null;

    private boolean showingStats = false;

    private boolean finishedShowingStats = false;
    private float finishedTime = 0f;

    private float fadeTiming = 6f;

    public WinScreen() { }

	public WinScreen(GameManager dungeoneerComponent) {
		this.dungeoneerComponent = dungeoneerComponent;
		skin = UiSkin.getSkin();
		ui = new Stage(getViewport(Gdx.graphics.getWidth() / uiScale, Gdx.graphics.getHeight() / uiScale));
        splashLevel = "levels/win-screen-splash.bin";
        statLabelColor.set(1f, 1f, 1f, 1f);
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
        backgroundColor.set(0f, 0f, 0f, 1);

        super.draw(delta);

        float showTime = Math.min(tickTime, 1f);
        if(showTime < 1f) {
            backgroundColor.a = 1f - showTime;
            renderer.drawFlashOverlay(backgroundColor);
        }

        ui.draw();

        boolean doFade = tickTime < fadeTiming;
        if(doFade) {
            float fadeOutTime = Math.min((fadeTiming - tickTime) / fadeTiming, 1f);
            backgroundColor.set(0f, 0f, 0f, fadeOutTime);
            renderer.drawFlashOverlay(backgroundColor);

            if(fadeOutTime >= fadeTiming) {
                gl.glClearColor(0f, 0f, 0f, 1);
            }
        }
	}

	@Override
	public void show() {

        showingStats = false;
        finishedShowingStats = false;
        finishedTime = 0f;
        tickTime = 0f;

        // save the updated progress
        if(Game.instance != null && Game.instance.progression != null) {
            final Progression progression = Game.instance.progression;
            progression.gold = Game.instance.player.gold;
            progression.won = true;
            progression.wins++;
            Game.saveProgression(progression, Game.instance.getSaveSlot());
        }

        Timer.Task startFade = new Timer.Task() {
            @Override
            public void run() {
                doTick = true;
                Audio.playMusic("win.mp3", false);
            }
        };

        // Schedule the music task
        Timer delayTimer = new Timer();
        delayTimer.scheduleTask(startFade, 2.75f);
        delayTimer.start();

		Gdx.input.setCursorCatched(true);
		running = true;

        startCreditsSequence();
		super.show();
	}

	private void startCreditsSequence()
    {
        credits = MessageManager.getMessage("credits.dat");
        showDelverLogo();
    }

	private void showDelverLogo() {
        ui.clear();

        Texture logoTexture = Art.loadTexture("splash/Delver-Logo.png");
        Image logo = new Image(logoTexture);

        Table t = new Table();
        t.add(logo).center().fill();
        t.setFillParent(true);

        Action next = new Action() {
            public boolean act (float delta) {
                showText(credits.messages);
                return true;
            }
        };

        t.setColor(1f, 1f, 1f, 0f);
        t.addAction(Actions.sequence(Actions.delay(5f), Actions.fadeIn(4f), Actions.delay(2f), Actions.fadeOut(4), next));

        ui.addActor(t);
    }

    private  void showText(final Array<Array<String>> text) {
        ui.clear();
        
        if(text.size > 0) {
            Array<String> textToShow = text.removeIndex(0);
            Table t = new Table(UiSkin.getSkin());

            // Add the messages
            for (int i = 0; i < textToShow.size; i++) {
                Label label = new Label(textToShow.get(i), UiSkin.getSkin());
                t.add(label).center().padTop(5f);
                t.row();
            }

            t.setFillParent(true);

            Action next = new Action() {
                public boolean act(float delta) {
                    showText(text);
                    return true;
                }
            };

            t.setColor(1f, 1f, 1f, 0f);
            t.addAction(Actions.sequence(Actions.delay(1f), Actions.fadeIn(2f), Actions.delay(2.5f), Actions.fadeOut(1), next));

            ui.addActor(t);
        }
        else {
            showStats();
        }
    }

    private void showStats() {
        SteamApi.api.achieve("WON");
        showingStats = true;
        showStats(1);
    }

    private void showStats(final int progress) {
        if(progress < 10) {
            Timer.Task startFade = new Timer.Task() {
                @Override
                public void run() {
                    showStats(progress + 1);
                }
            };

            // Schedule the music task
            Timer delayTimer = new Timer();
            delayTimer.scheduleTask(startFade, 0.05f);
            delayTimer.start();

            statNumber = progress;
            makeStats(statNumber, true);
        }
        else {
            finishedShowingStats = true;
        }
    }

    @Override
	public void tick(float delta) {
        tickTime += delta;

        Game.gamepadManager.menuMode = true;

        // tick the background level, if one is loaded
        tick_level(delta);

        ui.act(delta);

        if(finishedShowingStats) {
            finishedTime += delta;
        }

		// Back to the main menu!
		if(tickTime >= (fadeTiming * 0.95f) && (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.justTouched() || Game.gamepadManager.controllerState.buttonEvents.size > 0)) {
		    if(showingStats) {
		        if(statNumber < 10) {
                    statNumber = 10;
                    makeStats(10, false);
                }
                else {
		            if(finishedTime >= 7f) {
                        GameApplication.ShowMainMenuScreen();
                        Gdx.input.setCursorCatched(false);
                    }
                }
            }
            else {
                if(Game.instance != null && Game.instance.progression != null) {
                    if(Game.instance.progression.wins >= 2) {
                        showStats();
                    }
                }
            }
		}

        Game.gamepadManager.tick(delta);
	}
}
