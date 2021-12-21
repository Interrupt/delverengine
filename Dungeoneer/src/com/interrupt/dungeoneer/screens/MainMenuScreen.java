package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.overlays.ModsOverlay;
import com.interrupt.dungeoneer.overlays.OptionsOverlay;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;

import java.text.MessageFormat;

public class MainMenuScreen extends BaseScreen {
    private Texture menuTexture;
    private TextureRegion[] menuRegions;

    private Table fullTable = null;
    private Table buttonTable = null;

    private TextButton playButton;
    private TextButton deleteButton;
    private TextButton optionsButton;

    private Progression[] progress = new Progression[3];
    private Player[] saveGames = new Player[3];
    private Integer selectedSave;

    private boolean ignoreEscapeKey = false;
    private boolean refreshOnEscape = false;

    private Array<Table> saveSlotUi = new Array<>();

    private Player errorPlayer = new Player();

    private Color fadeColor = new Color(Color.BLACK);
    private boolean fadingOut = false;
    private float fadeFactor = 1f;

    private static final String BASE_SAVE_DIR = "save/";

	public MainMenuScreen() {
		if(splashScreenInfo != null) {
		    splashLevel = splashScreenInfo.backgroundLevel;
        }

		screenName = "MainMenuScreen";

		menuTexture = Art.loadTexture("menu.png");
		menuRegions = new TextureRegion[(menuTexture.getWidth() / 16) * (menuTexture.getHeight() / 16)];
		int count = 0;

		for(int y = 0; y < (menuTexture.getHeight() / 16); y++) {
			for(int x = 0; x < (menuTexture.getWidth() / 16); x++) {
				menuRegions[count++] = new TextureRegion(menuTexture, x * 16, y * 16, 16, 16);
			}
		}

		ui = new Stage(viewport);

        fullTable = new Table(skin);
        fullTable.setFillParent(true);
        fullTable.align(Align.center);

        buttonTable = new Table();

        FileHandle upFile = Game.getInternal("ui/discord_up.png");
        if (upFile.exists()) {
            Drawable drawable = new TextureRegionDrawable(new Texture(upFile));
            Drawable downDrawable = null;

            FileHandle downFile = Game.getInternal("ui/discord_down.png");
            if (downFile.exists()) {
                downDrawable = new TextureRegionDrawable(new Texture(Game.getInternal("ui/discord_down.png")));
            }

            ImageButton discordButton = new ImageButton(drawable, downDrawable) {
                @Override
                public void act(float delta) {
                    super.act(delta);
                    setY(8);
                    setX(ui.getWidth() - this.getWidth() - 8);
                }
            };
            discordButton.setColor(Colors.DISCORD_BUTTON);
            discordButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.net.openURI("https://discord.gg/gMEg3PPgD4");
                }
            });

            ui.addActor(discordButton);
        }

        Label versionLabel = new Label(Game.VERSION, skin);
        versionLabel.setPosition(8, 8);
        versionLabel.setColor(Color.GRAY);
        ui.addActor(versionLabel);

        ui.addActor(fullTable);

		Gdx.input.setInputProcessor(ui);
	}

    public void makeContent() {
	    this.gamepadEntries.clear();
	    this.gamepadSelectionIndex = null;
	    refreshOnEscape = false;

        String paddedButtonText = " {0} ";

        // The main action buttons
        playButton = new TextButton(MessageFormat.format(paddedButtonText, StringManager.get("screens.MainMenuScreen.playButton")), skin);
        playButton.setColor(Colors.PLAY_BUTTON);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handlePlayButtonEvent(false);
            }
        });

        deleteButton = new TextButton(MessageFormat.format(paddedButtonText, StringManager.get("screens.MainMenuScreen.eraseButton")), skin);
        deleteButton.setColor(Colors.ERASE_BUTTON);
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleDeleteButtonEvent(false);
            }
        });

        optionsButton = new TextButton(MessageFormat.format(paddedButtonText, StringManager.get("screens.MainMenuScreen.optionsButton")), skin);
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleOptionsButtonEvent();
            }
        });



        TextButton modsButton = new TextButton(MessageFormat.format(paddedButtonText, StringManager.get("screens.MainMenuScreen.modsButton")), skin);
        modsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleModsButtonEvent();
            }
        });

        // Start building the UI
        fullTable.padTop(42);
        fullTable.clearChildren();
        fullTable.add(StringManager.get("screens.MainMenuScreen.selectSaveSlot")).align(Align.left).padTop(24f);
        fullTable.add(deleteButton).align(Align.right).padTop(20f);
        fullTable.row();

        buttonTable.clearChildren();

        NinePatchDrawable fileSelectBg = new NinePatchDrawable(new NinePatch(skin.getRegion("save-select"), 6, 6, 6, 6));

        for(int i = 0; i < 3; i++) {
            final int loc = i;

            String saveName = StringManager.get("screens.MainMenuScreen.newGameSaveSlot");
            if(saveGames[i] != null && saveGames[i] == errorPlayer) saveName = StringManager.get("screens.MainMenuScreen.errorSaveSlot");
            else if(saveGames[i] != null) saveName = getSaveName(progress[i], saveGames[i].levelNum, saveGames[i].levelName);
            else {
                if(progress[i] != null) saveName = getSaveName(progress[i], null, null);
            }

            float fontScale = 1f;
            float rowHeight = 15f;

            Label fileTitle = new Label("" + (i + 1), skin);
            fileTitle.setFontScale(fontScale);
            fileTitle.setColor(Color.GRAY);

            final Table t = new Table(skin);
            t.add(fileTitle).size(20f, rowHeight * 2f).align(Align.center);
            t.setBackground(fileSelectBg);
            t.center();

            Label locationLabel = new Label(saveName, skin);
            locationLabel.setFontScale(fontScale);

            Table t2 = new Table(skin);
            t2.align(Align.left);

            if(progress[i] != null) {
                Label playtimeLabel = new Label(progress[i].getPlaytime(), skin);
                playtimeLabel.setAlignment(Align.right);

                Table topRow = new Table();
                topRow.add(locationLabel);
                topRow.add(playtimeLabel).expand().align(Align.right);

                t2.add(topRow).width(220).padTop(2);
                t2.row();

                TextureAtlas itemAtlas = TextureAtlas.cachedAtlases.get("item");

                Image goldIcon = new Image(new TextureRegionDrawable(itemAtlas.getSprite(89)));
                goldIcon.setAlign(Align.left);

                Image skullIcon = new Image(new TextureRegionDrawable(itemAtlas.getSprite(56)));
                skullIcon.setAlign(Align.left);

                Image orbIcon = new Image(new TextureRegionDrawable(itemAtlas.getSprite(59)));
                orbIcon.setAlign(Align.left);

                Label goldLabel = new Label(progress[i].gold + "", skin);
                goldLabel.setFontScale(fontScale);

                Label deathLabel = new Label(progress[i].deaths + "", skin);
                deathLabel.setFontScale(fontScale);

                Label winsLabel = new Label(progress[i].wins + "", skin);
                winsLabel.setFontScale(fontScale);

                Table progressTable = new Table(skin);
                progressTable.add(goldIcon).width(20).height(20).align(Align.left);
                progressTable.add(goldLabel).width(45);
                progressTable.add(skullIcon).width(20).height(20).align(Align.left);
                progressTable.add(deathLabel).padLeft(2).width(45);

                if(progress[i].wins > 0) {
                    progressTable.add(orbIcon).width(20).height(20).align(Align.left);
                    progressTable.add(winsLabel).padLeft(2).width(45);
                }

                progressTable.pack();

                t2.add(progressTable).align(Align.left);
            }
            else {
                t2.add(locationLabel).align(Align.left).size(220, rowHeight * 2f).padTop(2f).padBottom(4f);
            }

            t2.pack();

            t.add(t2);
            t.pack();

            // fancy animation!
            t.addAction(Actions.sequence(Actions.fadeOut(0.0001f), Actions.delay(((float) i + 1f) * 0.1f), Actions.fadeIn(0.2f)));

            t.setTouchable(Touchable.enabled);
            t.addListener(new ClickListener() {

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectSaveButtonEvent(loc, t);
                }
            });

            // keep track of this slot for changing the color later
            t.setColor(Color.GRAY);
            saveSlotUi.add(t);

            fullTable.add(t).height(42f).padTop(4f).colspan(2);
            fullTable.row();

            GamepadEntry g = new GamepadEntry(
                t,
                new GamepadEntryListener() {
                    @Override
                    public void onPress() {
                        for (EventListener listener : playButton.getListeners()) {
                            if (listener instanceof ClickListener) {
                                ((ClickListener) listener).clicked(new InputEvent(), playButton.getX(), playButton.getY());
                            }
                        }
                    }
                },
                new GamepadEntryListener() {
                    @Override
                    public void onPress() {
                        if (!deleteButton.isVisible()) {
                            return;
                        }

                        for (EventListener listener : deleteButton.getListeners()) {
                            if (listener instanceof ClickListener) {
                                ((ClickListener) listener).clicked(new InputEvent(), playButton.getX(), playButton.getY());
                            }
                        }
                    }
                }
            );
            this.gamepadEntries.add(g);
        }

        Table playButtonTable = new Table();
        playButtonTable.add(playButton).align(Align.left).height(20f).expand();

        buttonTable.add(playButtonTable).align(Align.left).fillX().expand();

        if(hasMods())
            buttonTable.add(modsButton).padRight(4).align(Align.right).height(20);

        buttonTable.add(optionsButton).align(Align.right).height(20f);
        buttonTable.pack();

        GamepadEntry optionsEntry = new GamepadEntry(optionsButton, new GamepadEntryListener() {
            @Override
            public void onPress() {
                for (EventListener listener : optionsButton.getListeners()) {
                    if (listener instanceof ClickListener) {
                        ((ClickListener) listener).clicked(new InputEvent(), optionsButton.getX(), optionsButton.getY());
                    }
                }
            }
        },
        new GamepadEntryListener() {
            @Override
            public void onPress() {
                for (EventListener listener : optionsButton.getListeners()) {
                    if (listener instanceof ClickListener) {
                        ((ClickListener) listener).clicked(new InputEvent(), optionsButton.getX(), optionsButton.getY());
                    }
                }
            }
        });

        gamepadEntries.add(optionsEntry);

        fullTable.row();
        fullTable.add(buttonTable).colspan(2).height(30f).fill(true, false).align(Align.center);
        fullTable.row();
        fullTable.add().colspan(2).height(60f);

        fullTable.pack();
        fullTable.padTop(42);

        fullTable.addAction(Actions.sequence(Actions.fadeOut(0.0001f), Actions.fadeIn(0.2f)));

        playButton.setVisible(false);
        deleteButton.setVisible(false);
    }

    public void showModal(String message, String yesText, String noText, ClickListener yesListener, int width) {
	    Table full = fullTable;
	    fullTable.padTop(0);
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
                makeContent();
            }
        });

        yesButton.setColor(Color.RED);

        t.add(noButton).align(Align.left).expand().padTop(8);
        t.add(yesButton).align(Align.right).expand().padTop(8);
        t.row();

        full.add(t);

        GamepadEntry yesEntry = new GamepadEntry(yesButton, new GamepadEntryListener() {
            @Override
            public void onPress() {
                for (EventListener listener : yesButton.getListeners()) {
                    if (listener instanceof ClickListener) {
                        ((ClickListener) listener).clicked(new InputEvent(), yesButton.getX(), yesButton.getY());
                    }
                }
            }
        },
        new GamepadEntryListener() {
            @Override
            public void onPress() {
                for (EventListener listener : yesButton.getListeners()) {
                    if (listener instanceof ClickListener) {
                        ((ClickListener) listener).clicked(new InputEvent(), yesButton.getX(), yesButton.getY());
                    }
                }
            }
        });

        GamepadEntry noEntry = new GamepadEntry(noButton, new GamepadEntryListener() {
            @Override
            public void onPress() {
                for (EventListener listener : noButton.getListeners()) {
                    if (listener instanceof ClickListener) {
                        ((ClickListener) listener).clicked(new InputEvent(), noButton.getX(), noButton.getY());
                    }
                }
            }
        },
        new GamepadEntryListener() {
            @Override
            public void onPress() {
                for (EventListener listener : noButton.getListeners()) {
                    if (listener instanceof ClickListener) {
                        ((ClickListener) listener).clicked(new InputEvent(), noButton.getX(), noButton.getY());
                    }
                }
            }
        });

        gamepadEntries.clear();
        gamepadEntries.add(noEntry);
        gamepadEntries.add(yesEntry);
        this.gamepadSelectionIndex = null;

        refreshOnEscape = true;
    }

	@Override
	public void show() {
		super.show();

		if(Game.instance != null)
			Game.instance.clearMemory();

		loadSavegames();

        makeContent();

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

	private String getSaveName(Progression p, Integer levelNum, String levelName) {
		if(p != null && p.won) return StringManager.get("screens.MainMenuScreen.finishedSaveSlot");
		if(levelNum == null) return StringManager.get("screens.MainMenuScreen.deadSaveSlot");

		if(levelNum == -1 && Game.gameData.tutorialLevel != null) {
            return Game.gameData.tutorialLevel.levelName;
        }

		return levelName;
	}

    @Override
	public void tick(float delta) {
        super.tick(delta);

		// quit!
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			if(!ignoreEscapeKey) {
			    if(refreshOnEscape) {
                    refreshOnEscape = false;
                    makeContent();
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

	public void selectSaveButtonEvent(int saveLoc, Table selected) {
        gamepadSelectionIndex = saveLoc;

        for(int i = 0; i < saveSlotUi.size; i++) {
            saveSlotUi.get(i).setColor(Color.GRAY);
        }

        if(selected != null) {
            selected.setColor(Color.WHITE);
        }

        playButton.setVisible(saveGames[saveLoc] != errorPlayer);
		selectedSave = saveLoc;

		deleteButton.setVisible(saveGames[selectedSave] != null || progress[selectedSave] != null);

        Audio.playSound("/ui/ui_button_click.mp3", 0.3f);
	}

    /** Handles the event when the play button is clicked. */
	private void handlePlayButtonEvent(boolean force) {
        Audio.playSound("/ui/ui_button_click.mp3", 0.3f);

        if(!force) {
            Progression p = progress[selectedSave];
            if(p != null) {
                Array<String> missing = p.checkForMissingMods();
                if(missing.size > 0) {
                    ClickListener playListener = new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            handlePlayButtonEvent(true);
                        }
                    };

                    String message = StringManager.get("screens.MainMenuScreen.missingModsWarning");
                    StringBuilder missingModsStringBuilder = new StringBuilder();

                    for(int i = 0; i < missing.size; i++) {
                        String m = missing.get(i);

                        int maxModLength = 30;
                        if(m.length() > maxModLength)
                            m = m.substring(0, maxModLength - 3) + "...";

                            missingModsStringBuilder.append(m);
                            missingModsStringBuilder.append("\n");
                    }

                    showModal(MessageFormat.format(message, missingModsStringBuilder.toString()), StringManager.get("screens.MainMenuScreen.playButton"), StringManager.get("screens.MainMenuScreen.cancelButton"), playListener, 260);
                    return;
                }
            }
        }

        fullTable.addAction(Actions.sequence(Actions.fadeOut(0.3f), Actions.delay(0.5f), Actions.addAction(new Action() {
            @Override
            public boolean act(float v) {
                fadingOut = true;
                return true;
            }
        })
        , Actions.delay(1.75f), Actions.addAction(new Action() {
            @Override
            public boolean act(float v) {
                GameApplication.SetScreen(new LoadingScreen(saveGames[selectedSave] == null ? StringManager.get("screens.MainMenuScreen.creatingDungeon") : StringManager.get("screens.MainMenuScreen.loadingSaveSlot"), selectedSave));
                return true;
            }
        })));
	}

    /** Handles the event when the delete button is clicked. */
	private void handleDeleteButtonEvent(boolean force) {
	    if(!force) {
            ClickListener eraseListener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleDeleteButtonEvent(true);
                }
            };

            showModal(StringManager.get("screens.MainMenuScreen.eraseSaveWarning"), StringManager.get("screens.MainMenuScreen.eraseButton"), StringManager.get("screens.MainMenuScreen.cancelButton"), eraseListener, 220);
            return;
        }

		saveGames[selectedSave] = null;
		progress[selectedSave] = null;
		deleteSavegame(selectedSave);
		selectedSave = null;
    }

    /** Handles the event when the options button is clicked. */
    private void handleOptionsButtonEvent() {
        GameApplication.SetScreen(new OverlayWrapperScreen(new OptionsOverlay(false, true)));
    }

    /** Handles the event when the mods button is clicked. */
    private void handleModsButtonEvent() {
        GameApplication.SetScreen(new OverlayWrapperScreen(new ModsOverlay()));
    }

	private void loadSavegames() {
		FileHandle dir = Game.getFile(BASE_SAVE_DIR);

		Gdx.app.log("DelverLifeCycle", "Getting savegames from " + dir.path());
		for(int i = 0; i < saveGames.length; i++) {
			FileHandle file = Game.getFile(BASE_SAVE_DIR + i + "/player.dat");
			if(file.exists())
			{
				try {
					saveGames[i] = JsonUtil.fromJson(Player.class, file);
				}
				catch(Exception ex) {
					saveGames[i] = errorPlayer;
				}
			}
		}

		for(int i = 0; i < saveGames.length; i++) {
			FileHandle file = Game.getFile(BASE_SAVE_DIR + "game_" + i + ".dat");
			if(file.exists())
			{
				try {
					progress[i] = JsonUtil.fromJson(Progression.class, file);
				}
				catch(Exception ex) {
					progress[i] = null;
				}
			}
		}
	}

	private void deleteSavegame(int saveLoc) {
		try {
			FileHandle file = Game.getFile(BASE_SAVE_DIR + saveLoc + "/");
			Gdx.app.log("DelverLifeCycle", "Deleting savegame " + file.path());
			file.deleteDirectory();
		} catch(Exception ex) {
            Gdx.app.error("DelverLifeCycle", ex.getMessage());
        }

		try {
			FileHandle file = Game.getFile(BASE_SAVE_DIR + "game_" + saveLoc + ".dat");
			Gdx.app.log("DelverLifeCycle", "Deleting progress " + file.path());
			file.delete();
		} catch(Exception ex) {
            Gdx.app.error("DelverLifeCycle", ex.getMessage());
        }

        makeContent();
	}

	private boolean hasMods() {
	    if(Game.modManager == null) return false;
	    if(Game.modManager.modsFound == null) return false;
	    return Game.modManager.hasExtraMods();
    }
}
