package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.screens.LoadingScreen;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;

import java.text.MessageFormat;

public class SelectSaveSlotOverlay extends WindowOverlay {
    public class SaveSlot extends Table {
        private int number;
        boolean isSelected = false;

        public SaveSlot(int number) {
            super(skin);
            this.number = number;
            Progression progression = progress[number];

            String saveName = StringManager.get("screens.MainMenuScreen.newGameSaveSlot");
            if(saveGames[number] != null && saveGames[number] == errorPlayer) saveName = StringManager.get("screens.MainMenuScreen.errorSaveSlot");
            else if(saveGames[number] != null) saveName = getSaveName(progression, saveGames[number].levelNum, saveGames[number].levelName);
            else {
                if(progression != null) saveName = getSaveName(progression, null, null);
            }

            float fontScale = 1f;
            float rowHeight = 15f;

            Label fileTitle = new Label("" + (number + 1), skin);
            fileTitle.setFontScale(fontScale);
            fileTitle.setColor(Color.GRAY);

            NinePatchDrawable fileSelectBg = new NinePatchDrawable(new NinePatch(skin.getRegion("save-select"), 6, 6, 6, 6));

            add(fileTitle).size(20f, rowHeight * 2f).align(Align.center);
            setBackground(fileSelectBg);
            center();

            Label locationLabel = new Label(saveName, skin);
            locationLabel.setFontScale(fontScale);

            Table t2 = new Table(skin);
            t2.align(Align.left);

            if(progression != null) {
                Label playtimeLabel = new Label(progression.getPlaytime(), skin);
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

                Label goldLabel = new Label(progression.gold + "", skin);
                goldLabel.setFontScale(fontScale);

                Label deathLabel = new Label(progression.deaths + "", skin);
                deathLabel.setFontScale(fontScale);

                Label winsLabel = new Label(progression.wins + "", skin);
                winsLabel.setFontScale(fontScale);

                Table progressTable = new Table(skin);
                progressTable.add(goldIcon).width(20).height(20).align(Align.left);
                progressTable.add(goldLabel).width(45);
                progressTable.add(skullIcon).width(20).height(20).align(Align.left);
                progressTable.add(deathLabel).padLeft(2).width(45);

                if(progression.wins > 0) {
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

            add(t2);
            pack();

            // fancy animation!
            addAction(Actions.sequence(Actions.fadeOut(0.0001f), Actions.delay(((float) number + 1f) * 0.1f), Actions.fadeIn(0.2f)));

            setTouchable(Touchable.enabled);

            SaveSlot self = this;
            addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectSaveButtonEvent(number, self);

                    if (event instanceof GamepadEvent) {
                        handlePlayButtonEvent(false);
                    }
                }
            });

            setColor(Color.GRAY);
        }

        public void select() {
            selectSaveButtonEvent(number, this);
            isSelected = true;
        }

        public void deselect() {
            setColor(Color.GRAY);
            isSelected = false;
        }
    }

    private Progression[] progress = new Progression[3];
    private Player[] saveGames = new Player[3];

    private Table fullTable;
    private TextButton playButton;
    private TextButton deleteButton;
    private Array<Table> saveSlots = new Array<>();

    private final Player errorPlayer = new Player();

    private final Color fadeColor = new Color(Color.BLACK);
    private boolean fadingOut = false;
    private float fadeFactor = 0f;

    @Override
    public Table makeContent() {
        if (fullTable == null) {
            fullTable = new Table(skin);
        }
        fullTable.clear();

        this.gamepadSelectionIndex = null;

        // The main action buttons
        playButton = new TextButton(StringManager.get("screens.MainMenuScreen.playButton"), skin);
        playButton.setColor(Colors.PLAY_BUTTON);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handlePlayButtonEvent(false);
            }
        });

        deleteButton = new TextButton(StringManager.get("screens.MainMenuScreen.eraseButton"), skin);
        deleteButton.setColor(Colors.ERASE_BUTTON);
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleDeleteButtonEvent();
            }
        });

        // Start building the UI
        fullTable.clearChildren();
        fullTable.add(StringManager.get("screens.MainMenuScreen.selectSaveSlot")).align(Align.left).padTop(24f);
        fullTable.add(deleteButton).align(Align.right).padTop(20f);
        fullTable.row();

        for(int i = 0; i < 3; i++) {
            SaveSlot slot = new SaveSlot(i);
            saveSlots.add(slot);

            fullTable.add(slot).height(42f).padTop(4f).colspan(2);
            fullTable.row();

            addGamepadButtonOrder(slot, null);
        }

        Table buttonTable = new Table(skin);

        // Back
        TextButton textButton = new TextButton("Back", skin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        addGamepadButtonOrder(textButton, null);
        buttonTable.add(textButton).align(Align.left);

        buttonTable.add().growX();

        // Play
        buttonTable.add(playButton).align(Align.right);

        fullTable.row();
        fullTable.add(buttonTable).colspan(2).height(30f).fill(true, false).align(Align.center);
        fullTable.row();
        fullTable.add().colspan(2).height(60f);

        fullTable.pack();
        fullTable.padTop(42);

        fullTable.addAction(Actions.sequence(Actions.fadeOut(0.0001f), Actions.fadeIn(0.2f)));

        playButton.setVisible(false);
        deleteButton.setVisible(false);

        return fullTable;
    }

    @Override
    public void onShow() {
        loadSavegames();
        dimScreen = false;
        showBackground = false;
        buttonOrder.clear();
        super.onShow();
    }

    @Override
    public void tick(float delta) {
        super.tick(delta);

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
        }
    }

    @Override
    public void draw(float delta) {
        super.draw(delta);

        if (fadeFactor > 0f) {
            renderer = GameManager.renderer;
            renderer.drawFlashOverlay(fadeColor.set(0f, 0f, 0f, fadeFactor));
        }
    }

    public void selectSaveButtonEvent(int saveLoc, SaveSlot selected) {
        gamepadSelectionIndex = saveLoc;

        for(int i = 0; i < saveSlots.size; i++) {
            saveSlots.get(i).setColor(Color.GRAY);
        }

        if(selected != null) {
            selected.setColor(Color.WHITE);
        }

        playButton.setVisible(saveGames[saveLoc] != errorPlayer);
        Options.instance.selectedSaveSlot = saveLoc;

        deleteButton.setVisible(saveGames[Options.instance.selectedSaveSlot] != null || progress[Options.instance.selectedSaveSlot] != null);

        // Don't keep playing the click button over and over
        if(selected.isSelected == true)
            return;

        selected.isSelected = true;
        Audio.playSound("/ui/ui_button_click.mp3", 0.3f);
    }

    /** Handles the event when the play button is clicked. */
    private void handlePlayButtonEvent(boolean force) {
        Audio.playSound("/ui/ui_button_click.mp3", 0.3f);

        if(!force) {
            Progression p = progress[Options.instance.selectedSaveSlot];
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

        ui.getRoot().addAction(
            Actions.sequence(
                Actions.fadeOut(0.3f),
                Actions.delay(0.5f),
                Actions.addAction(new Action() {
                    @Override
                    public boolean act(float v) {
                        fadingOut = true;
                        return true;
                    }
                }),
                Actions.addAction(new TemporalAction(1.75f) {
                    @Override
                    protected void update(float percent) {
                        fadeFactor = percent;
                    }
                }),
                Actions.delay(1.75f),
                Actions.addAction(new Action() {
                    @Override
                    public boolean act(float v) {
                        String loadingMessage = saveGames[Options.instance.selectedSaveSlot] == null ? StringManager.get("screens.MainMenuScreen.creatingDungeon") : StringManager.get("screens.MainMenuScreen.loadingSaveSlot");

                        close();

                        GameApplication.SetScreen(
                            new LoadingScreen(
                                loadingMessage,
                                Options.instance.selectedSaveSlot
                            )
                        );

                        return true;
                    }
                })
            )
        );
    }

    /** Handles the event when the delete button is clicked. */
    private void handleDeleteButtonEvent() {
        ClickListener eraseListener = new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                deleteSelectedSaveSlot();
            }
        };

        showModal(
            StringManager.get("screens.MainMenuScreen.eraseSaveWarning"),
            StringManager.get("screens.MainMenuScreen.eraseButton"),
            StringManager.get("screens.MainMenuScreen.cancelButton"),
            eraseListener,
            220
        );
    }

    private void deleteSelectedSaveSlot() {
        saveGames[Options.instance.selectedSaveSlot] = null;
        progress[Options.instance.selectedSaveSlot] = null;
        deleteSavegame(Options.instance.selectedSaveSlot);
        makeContent();
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

    private static final String BASE_SAVE_DIR = "save/";
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

    private String getSaveName(Progression p, Integer levelNum, String levelName) {
        if(p != null && p.won) return StringManager.get("screens.MainMenuScreen.finishedSaveSlot");
        if(levelNum == null) return StringManager.get("screens.MainMenuScreen.deadSaveSlot");

        if(levelNum == -1 && Game.gameData.tutorialLevel != null) {
            return Game.gameData.tutorialLevel.levelName;
        }

        return levelName;
    }

    public void showModal(String message, String yesText, String noText, ClickListener yesListener, int width) {
        Table full = fullTable;
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
                makeContent();
            }
        });

        yesButton.setColor(Color.RED);

        t.add(noButton).align(Align.left).expand().padTop(8);
        t.add(yesButton).align(Align.right).expand().padTop(8);
        t.row();

        full.add(t);

        this.gamepadSelectionIndex = null;
    }
}
