package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;

public class OptionsOverlay extends WindowOverlay {

    private Slider musicVolume;
    private Slider sfxVolume;
    private Slider gfxQuality;
    private Label gfxQualityValueLabel;
    private float gfxQualityLastValue;
    private Slider uiSize;

    private Label uiSizeValueLabel;
    private float uiSizeLastValue;

    private Label musicVolumeValueLabel;
    private Label sfxVolumeValueLabel;

    private float sfxVolumeLastValue;
    private float musicVolumeLastValue;

    private CheckBox fullscreenMode;

    private String[] graphicsLabelValues = {
            StringManager.get("screens.OptionsScreen.graphicsLow"),
            StringManager.get("screens.OptionsScreen.graphicsMedium"),
            StringManager.get("screens.OptionsScreen.graphicsHigh"),
            StringManager.get("screens.OptionsScreen.graphicsUltra")
    };

    private boolean doForcedValuesUpdate=true;

    private Table mainTable;

    private CheckBox showUI;
    private CheckBox showCrosshair;
    private CheckBox headBob;
    private CheckBox handLag;

    public OptionsOverlay() {
        animateBackground = false;
    }

    public OptionsOverlay(boolean dimScreen, boolean showBackground)
    {
        animateBackground = false;
        this.dimScreen = dimScreen;
        this.showBackground = showBackground;
    }

    @Override
    public Table makeContent() {

        Options.loadOptions();
        Options options = Options.instance;

        skin = UiSkin.getSkin();

        TextButton backBtn = new TextButton(StringManager.get("screens.OptionsScreen.backButton"), skin.get(TextButton.TextButtonStyle.class));
        backBtn.setWidth(200);
        backBtn.setHeight(50);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveAndClose();
                Audio.playSound("/ui/ui_button_click.mp3", 0.1f);
            }
        });

        TextButton controlsBtn = new TextButton(StringManager.get("screens.OptionsScreen.inputButton"), skin.get(TextButton.TextButtonStyle.class));
        controlsBtn.setWidth(200);
        controlsBtn.setHeight(50);
        controlsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveOptions();
                OverlayManager.instance.replaceCurrent(new OptionsInputOverlay(dimScreen, showBackground));
            }
        });

        TextButton graphicsBtn = new TextButton(StringManager.getOrDefaultTo("screens.OptionsScreen.graphicsButton", "Graphics"), skin.get(TextButton.TextButtonStyle.class));
        graphicsBtn.padRight(6f).padLeft(6f);
        graphicsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveOptions();
                OverlayManager.instance.replaceCurrent(new OptionsGraphicsOverlay(dimScreen, showBackground));
            }
        });

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.columnDefaults(0).align(Align.left).padRight(4);
        mainTable.columnDefaults(1).align(Align.left).padLeft(4).padRight(4);
        mainTable.columnDefaults(2).align(Align.right).padLeft(4);

        //Header
        Label header = new Label(StringManager.get("screens.OptionsScreen.headerLabel"),skin.get(Label.LabelStyle.class));
        header.setFontScale(1.1f);
        mainTable.add(header);
        mainTable.row();

        // Music Volume
        musicVolume = new Slider(0f, 1f, 0.01f, false, skin.get(Slider.SliderStyle.class));
        musicVolume.setValue(options.musicVolume);

        Label musicVolumeLabel = new Label(StringManager.get("screens.OptionsScreen.musicVolumeLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(musicVolumeLabel);
        mainTable.add(musicVolume);

        addGamepadButtonOrder(musicVolume, musicVolumeLabel);

        musicVolumeValueLabel=new Label("x.xxx",skin.get(Label.LabelStyle.class));
        musicVolumeValueLabel.setAlignment(Align.right);
        mainTable.add(musicVolumeValueLabel);

        mainTable.row();

        // SFX Volume
        sfxVolume = new Slider(0f, 2f, 0.01f, false, skin.get(Slider.SliderStyle.class));
        sfxVolume.setValue(options.sfxVolume);

        Label sfxVolumeLabel = new Label(StringManager.get("screens.OptionsScreen.soundVolumeLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(sfxVolumeLabel);
        mainTable.add(sfxVolume);

        addGamepadButtonOrder(sfxVolume, sfxVolumeLabel);

        sfxVolumeValueLabel=new Label("x.xxx",skin.get(Label.LabelStyle.class));
        sfxVolumeValueLabel.setAlignment(Align.right);
        mainTable.add(sfxVolumeValueLabel);

        mainTable.row();

        // Graphics Quality
        gfxQuality = new Slider(1f, 4f, 1f, false, skin.get(Slider.SliderStyle.class));
        gfxQuality.setValue(options.graphicsDetailLevel);

        Label graphicsDetailLabel = new Label(StringManager.get("screens.OptionsScreen.graphicsDetailLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(graphicsDetailLabel);
        mainTable.add(gfxQuality);

        addGamepadButtonOrder(gfxQuality, graphicsDetailLabel);

        gfxQualityValueLabel=new Label("xx.xx",skin.get(Label.LabelStyle.class));
        gfxQualityValueLabel.setAlignment(Align.right);
        mainTable.add(gfxQualityValueLabel).minSize(48f, 0f);
        mainTable.row();

        //UI Size
        float uiMin = 0.5f;
        float uiMax = 1.5f;
        uiSize = new Slider(uiMin, uiMax, 0.01f, false, skin.get(Slider.SliderStyle.class));
        options.uiSize = Math.max(Math.min(options.uiSize,uiMax),uiMin);
        uiSize.setValue(options.uiSize);

        Label uiSizeLabel = new Label(StringManager.get("screens.OptionsScreen.uiSizeLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(uiSizeLabel);
        mainTable.add(uiSize);

        uiSizeValueLabel=new Label("x.xxx",skin.get(Label.LabelStyle.class));
        uiSizeValueLabel.setAlignment(Align.right);
        mainTable.add(uiSizeValueLabel);
        mainTable.row();

        addGamepadButtonOrder(uiSize, uiSizeLabel);

        // Show HUD
        Label showHudLabel = new Label(StringManager.get("screens.OptionsScreen.showHudLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(showHudLabel);

        showUI = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
        showUI.setChecked(!Options.instance.hideUI);

        addGamepadButtonOrder(showUI, showHudLabel);

        mainTable.add(showUI);
        mainTable.row();

        // Show Crosshair
        Label showCrosshairLabel = new Label(StringManager.get("screens.OptionsScreen.showCrosshairLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(showCrosshairLabel);

        showCrosshair = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
        showCrosshair.setChecked(Options.instance.alwaysShowCrosshair);

        addGamepadButtonOrder(showCrosshair, showCrosshairLabel);

        mainTable.add(showCrosshair);
        mainTable.row();

        // Head Bob
        Label headBobLabel = new Label(StringManager.get("screens.OptionsScreen.headBobLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(headBobLabel);

        headBob = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
        headBob.setChecked(Options.instance.headBobEnabled);

        addGamepadButtonOrder(headBob, headBobLabel);

        mainTable.add(headBob);
        mainTable.row();

        // Hand lag
        Player playerData;
        if (Game.instance != null && Game.instance.player != null) {
            playerData = Game.instance.player;
        }
        else {
            FileHandle file = Game.findInternalFileInMods("data/player.dat");
            playerData = JsonUtil.fromJson(Player.class, file, Player::new);
        }

        if (playerData.handLagStrength > 0f) {
            Label handLagLabel = new Label(StringManager.get("screens.OptionsScreen.handLagLabel"), skin.get(Label.LabelStyle.class));
            mainTable.add(handLagLabel);

            handLag = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
            handLag.setChecked(Options.instance.handLagEnabled);

            addGamepadButtonOrder(handLag, handLagLabel);

            mainTable.add(handLag);
            mainTable.row();
        }

        // Fullscreen Mode
        if(!(Gdx.app.getType() == Application.ApplicationType.Android || Gdx.app.getType() == Application.ApplicationType.iOS)) {
            fullscreenMode = new CheckBox("", skin.get(CheckBox.CheckBoxStyle.class));
            fullscreenMode.setChecked(Options.instance.fullScreen);

            fullscreenMode.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if(fullscreenMode.isChecked()) {
                        Graphics.DisplayMode desktopMode = Gdx.app.getGraphics().getDisplayMode(Gdx.graphics.getMonitor());
                        Gdx.app.getGraphics().setFullscreenMode(desktopMode);
                    }
                    else {
                        Graphics.DisplayMode desktopMode = Gdx.app.getGraphics().getDisplayMode(Gdx.graphics.getMonitor());
                        Gdx.app.getGraphics().setWindowedMode(desktopMode.width, desktopMode.height);
                    }
                }
            });

            Label fullScreenLabel = new Label(StringManager.get("screens.OptionsScreen.fullscreenLabel"),skin.get(Label.LabelStyle.class));
            mainTable.add(fullScreenLabel);
            mainTable.add(fullscreenMode);

            addGamepadButtonOrder(fullscreenMode, fullScreenLabel);

            mainTable.row();
        }

        // Button Bar
        Table buttonTable = new Table();
        buttonTable.add(backBtn).padRight(4f);
        buttonTable.add(controlsBtn).padRight(4f);
        buttonTable.add(graphicsBtn).padRight(4f);
        buttonTable.pack();

        mainTable.add(buttonTable);
        mainTable.getCell(header).align(Align.center).colspan(3).padBottom(8);
        mainTable.getCell(buttonTable).colspan(2).padTop(8);
        mainTable.pack();

        Table content = new Table();
        content.add(mainTable);
        content.pack();

        buttonOrder.add(backBtn);
        buttonOrder.add(controlsBtn);
        buttonOrder.add(graphicsBtn);

        return content;
    }

    public void tick(float delta) {

        // back
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if (mainTable.isVisible()) {
                saveAndClose();
            }
        }

        Options.instance.hideUI = !showUI.isChecked();

        updateValues();
        super.tick(delta);
    }

    public void updateValues() {

        Options.instance.uiSize = uiSize.getValue();
        Options.instance.musicVolume = musicVolume.getValue();
        Options.instance.sfxVolume = sfxVolume.getValue();
        Options.instance.graphicsDetailLevel = (int)gfxQuality.getValue();

        if(doForcedValuesUpdate || (uiSizeLastValue != uiSize.getValue())) {
            uiSizeLastValue = uiSize.getValue();
            uiSizeValueLabel.setText(String.format("%5.0f",100*uiSizeLastValue) + "%");

            if(Game.instance != null && Game.instance.player != null) {
                Game.RefreshUI();
            }

            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        if(doForcedValuesUpdate || (sfxVolumeLastValue != sfxVolume.getValue())) {
            sfxVolumeLastValue = sfxVolume.getValue();
            Audio.updateLoopingSoundVolumes();
            sfxVolumeValueLabel.setText(String.format("%5.0f",100*sfxVolumeLastValue) + "%");
        }

        if(doForcedValuesUpdate || (musicVolumeLastValue != musicVolume.getValue())) {
            if(Audio.music != null) Audio.setMusicVolume(1f);
            musicVolumeLastValue = musicVolume.getValue();
            musicVolumeValueLabel.setText(String.format("%5.0f",100*musicVolumeLastValue) + "%");
        }

        if(doForcedValuesUpdate || (gfxQualityLastValue != gfxQuality.getValue())) {
            gfxQualityLastValue = gfxQuality.getValue();

            try {
                gfxQualityValueLabel.setText(graphicsLabelValues[(int)gfxQualityLastValue - 1]);
            }
            catch(Exception ex) {
                gfxQualityValueLabel.setText("");
            }
        }

        doForcedValuesUpdate=false;
    }

    public void saveAndClose() {
        saveOptions();
        visible = false;
        OverlayManager.instance.clear();
    }

    public void saveOptions() {
        Options.instance.musicVolume = musicVolume.getValue();
        Options.instance.uiSize = uiSize.getValue();
        Options.instance.sfxVolume = sfxVolume.getValue();
        Options.instance.graphicsDetailLevel = (int)gfxQuality.getValue();
        Options.instance.hideUI = !showUI.isChecked();
        Options.instance.alwaysShowCrosshair = showCrosshair.isChecked();
        Options.instance.headBobEnabled = headBob.isChecked();
        Options.instance.handLagEnabled = handLag.isChecked();
        if(fullscreenMode != null) Options.instance.fullScreen = fullscreenMode.isChecked();
        Options.saveOptions();
    }
}
