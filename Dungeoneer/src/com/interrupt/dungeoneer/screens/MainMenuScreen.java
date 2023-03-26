package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.overlays.ModsOverlay;
import com.interrupt.dungeoneer.overlays.OptionsOverlay;
import com.interrupt.dungeoneer.overlays.SelectSaveSlotOverlay;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.OSUtils;

public class MainMenuScreen extends BaseScreen {
    private boolean ignoreEscapeKey = false;
    private boolean refreshOnEscape = false;

    private Color fadeColor = new Color(Color.BLACK);
    private boolean fadingOut = false;
    private float fadeFactor = 1f;

	public MainMenuScreen() {
		if(splashScreenInfo != null) {
		    splashLevel = splashScreenInfo.backgroundLevel;
        }

		screenName = "MainMenuScreen";

		ui = new Stage(viewport);

		Gdx.input.setInputProcessor(ui);
	}

    public void makeContent2() {
        clearGamepadEntries();
        Stage stage = ui;
        //ui.setDebugAll(true);

        Table table = new Table();
        table.setFillParent(true);

        float spacing = 4.0f;

        Stack stack = new Stack();

        //region Background Layer
        Table layerTable = new Table();
        layerTable.setName("backgroundLayer");
        layerTable.pad(8.0f);

        // Version
        Label label = new Label(Game.VERSION, skin);
        label.setColor(Color.GRAY);
        layerTable.add(label).align(Align.bottomLeft);

        // Empty cell to take all available space
        layerTable.add().grow();

        // Discord
        FileHandle upFile = Game.getInternal("ui/discord_up.png");
        if (upFile.exists()) {
            Drawable drawable = new TextureRegionDrawable(new Texture(upFile));
            Drawable downDrawable = null;

            FileHandle downFile = Game.getInternal("ui/discord_down.png");
            if (downFile.exists()) {
                downDrawable = new TextureRegionDrawable(new Texture(Game.getInternal("ui/discord_down.png")));
            }

            ImageButton discordButton = new ImageButton(drawable, downDrawable);
            discordButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.net.openURI("https://discord.gg/gMEg3PPgD4");
                }
            });

            layerTable.add(discordButton).align(Align.bottomRight);
        }

        stack.addActor(layerTable);
        //endregion

        //region Menu Layer
        layerTable = new Table();
        layerTable.setName("menuLayer");
        layerTable.pad(8.0f);

        // Pad above menu 1/3 free space
        layerTable.add().growY();

        // Logo
        if(splashScreenInfo.logoImage != null) {
            layerTable.row();

            Texture texture = Art.loadTexture(splashScreenInfo.logoImage);

            if(splashScreenInfo.logoFilter) {
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }

            Image image = new Image(texture);
            image.setScaling(Scaling.fit);
            layerTable.add(image).prefHeight(96f);
        }

        layerTable.row();
        Table buttonTable = new Table();

        // Play
        TextButton textButton = new TextButton(StringManager.get("screens.MainMenuScreen.playButton"), skin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handlePlayButtonEvent();
            }
        });
        addGamepadEntry(textButton);
        buttonTable.add(textButton).spaceBottom(spacing).fillX();

        // Mods
        if (hasMods()) {
            buttonTable.row();
            textButton = new TextButton(StringManager.get("screens.MainMenuScreen.modsButton"), skin);
            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleModsButtonEvent();
                }
            });
            addGamepadEntry(textButton);
            buttonTable.add(textButton).spaceBottom(spacing).fillX();
        }

        // Options
        buttonTable.row();
        textButton = new TextButton(StringManager.get("screens.MainMenuScreen.optionsButton"), skin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleOptionsButtonEvent();
            }
        });
        addGamepadEntry(textButton);
        buttonTable.add(textButton).spaceBottom(spacing).fillX();

        // Quit
        if (OSUtils.isDesktop() || OSUtils.isMobile()) {
            buttonTable.row();
            textButton = new TextButton(StringManager.get("screens.MainMenuScreen.quitButton"), skin);
            textButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.exit();
                }
            });
            addGamepadEntry(textButton);
            buttonTable.add(textButton).growX();
        }

        layerTable.add(buttonTable);

        // Pad below menu 2/3 free space
        layerTable.row();
        layerTable.add().growY();
        layerTable.row();
        layerTable.add().growY();

        stack.addActor(layerTable);
        //endregion

        //region Modal Layer
        layerTable = new Table();
        layerTable.setName("modalLayer");
        layerTable.pad(8.0f);
        stack.addActor(layerTable);
        //endregion

        table.add(stack).grow();

        table.addAction(
            Actions.sequence(
                Actions.fadeOut(0.0001f),
                Actions.fadeIn(0.2f)
            )
        );

        stage.addActor(table);
    }

    public void showModal(String message, String yesText, String noText, ClickListener yesListener, int width) {
        Table menu = ui.getRoot().findActor("menuLayer");
        menu.setVisible(false);

        Table full = ui.getRoot().findActor("modalLayer");
	    full.padTop(0);
	    full.clear();

	    Label text = new Label(message, UiSkin.getSkin());
	    text.setWrap(true);

        NinePatchDrawable background = new NinePatchDrawable(new NinePatch(UiSkin.getSkin().getRegion("window"), 11, 11, 11, 11));

        Table t = new Table(UiSkin.getSkin());
        t.setBackground(background);
        t.add(text).colspan(2).align(Align.left).width(width).expand();
        t.row();

        final TextButton yesButton = new TextButton(" " + yesText + " ", UiSkin.getSkin());
        final TextButton noButton = new TextButton(" " + noText + " ", UiSkin.getSkin());

        if(yesListener != null) {
            yesButton.addListener(yesListener);
        }

        noButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menu.setVisible(true);
            }
        });

        yesButton.setColor(Color.RED);

        t.add(noButton).align(Align.left).expand().padTop(8);
        t.add(yesButton).align(Align.right).expand().padTop(8);
        t.row();

        full.add(t);

        clearGamepadEntries();
        addGamepadEntry(noButton);
        addGamepadEntry(yesButton);
        this.gamepadSelectionIndex = null;

        refreshOnEscape = true;
    }

	@Override
	public void show() {
		super.show();

		if(Game.instance != null)
			Game.instance.clearMemory();

        makeContent2();

		ignoreEscapeKey = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);

        if(splashScreenInfo.music != null)
            Audio.playMusic(splashScreenInfo.music, true);
	}

    @Override
	public void draw(float delta) {
		super.draw(delta);

		renderer = GameManager.renderer;
		ui.draw();

        if(fadeFactor < 1f) {
            renderer.drawFlashOverlay(fadeColor.set(0f, 0f, 0f, 1f - fadeFactor));
        }
	}

    @Override
	public void tick(float delta) {
        super.tick(delta);

		// quit!
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			if(!ignoreEscapeKey) {
			    if(refreshOnEscape) {
                    refreshOnEscape = false;
                    makeContent2();
                }
                else {
                    Gdx.app.exit();
                }
            }
		}
		else ignoreEscapeKey = false;

		ui.act(delta);

        if(fadingOut) {
            fadeFactor -= delta * 0.5f;
            if(fadeFactor < 0) fadeFactor = 0;

            Audio.setMusicVolume(Math.min(1f, 1f * fadeFactor));
        }
	}

    /** Handles the event when the play button is clicked. */
	private void handlePlayButtonEvent() {
        GameApplication.SetScreen(new OverlayWrapperScreen(new SelectSaveSlotOverlay()));
	}

    /** Handles the event when the options button is clicked. */
    private void handleOptionsButtonEvent() {
        GameApplication.SetScreen(new OverlayWrapperScreen(new OptionsOverlay(false, true)));
    }

    /** Handles the event when the mods button is clicked. */
    private void handleModsButtonEvent() {
        GameApplication.SetScreen(new OverlayWrapperScreen(new ModsOverlay()));
    }

	private boolean hasMods() {
	    if(Game.modManager == null) return false;
	    if(Game.modManager.modsFound == null) return false;
	    return Game.modManager.hasExtraMods();
    }
}
