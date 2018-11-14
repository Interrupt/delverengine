package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.ui.ActionSpinnerButton;
import com.interrupt.managers.StringManager;

public class OptionsInputOverlay extends WindowOverlay {

    private Slider sliderLookX;
    private float valueLookXLast;
    private Slider sliderLookY;
    private float valueLookYLast;
    private CheckBox invertLook;
    private boolean invertLookLastValue;
    private ActionSpinnerButton spinnerMouseButton1;
    private ActionSpinnerButton spinnerMouseButton2;
    private ActionSpinnerButton spinnerMouseButton3;
    private CheckBox chkMouseScroller;
    private boolean mouseScrollerLastValue;

    private Table mainTable;

    public OptionsInputOverlay() {
        animateBackground = false;
    }

    public OptionsInputOverlay(boolean dimScreen, boolean showBackground)
    {
        animateBackground = false;
        this.dimScreen = dimScreen;
        this.showBackground = showBackground;
    }

    @Override
    public Table makeContent() {
        Options.loadOptions();
        Options options = Options.instance;

        if(Gdx.app.getType() == Application.ApplicationType.Android)
            Gdx.input.setCatchBackKey(true);

        TextButton backBtn = new TextButton(StringManager.get("screens.OptionsInputScreen.backButton"), skin.get(TextButton.TextButtonStyle.class));
        backBtn.setWidth(200);
        backBtn.setHeight(50);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveAndClose();
            }
        });

        TextButton keysBtn = new TextButton(StringManager.get("screens.OptionsInputScreen.keysButton"), skin.get(TextButton.TextButtonStyle.class));
        keysBtn.setWidth(200);
        keysBtn.setHeight(50);
        keysBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveOptions();
                OverlayManager.instance.replaceCurrent(new OptionsKeysOverlay(dimScreen, showBackground));
            }
        });

        TextButton gamepadBtn = new TextButton(StringManager.get("screens.OptionsInputScreen.gamepadButton"), skin.get(TextButton.TextButtonStyle.class));
        gamepadBtn.setWidth(200);
        gamepadBtn.setHeight(50);
        gamepadBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveOptions();
                OverlayManager.instance.replaceCurrent(new OptionsGamepadOverlay(dimScreen, showBackground));
            }
        });

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.columnDefaults(0).align(Align.left).padRight(4);
        mainTable.columnDefaults(1).align(Align.left).padLeft(4);
        mainTable.columnDefaults(2).align(Align.right);

        // Table Header

        Label header = new Label(StringManager.get("screens.OptionsInputScreen.headerLabel"), skin.get(Label.LabelStyle.class));
        header.setFontScale(1.1f);
        mainTable.add(header);
        mainTable.row();

        // Look X Slider

        sliderLookX = new Slider(0.1f, 3f, 0.001f, false, skin.get(Slider.SliderStyle.class));
        sliderLookX.setValue(options.mouseXSensitivity);

        Label lookXLabel = new Label(StringManager.get("screens.OptionsScreen.lookXLabel"), skin.get(Label.LabelStyle.class));
        mainTable.add(lookXLabel);
        mainTable.add(sliderLookX);
        mainTable.row();

        addGamepadButtonOrder(sliderLookX, lookXLabel);

        // Look Y Slider

        sliderLookY = new Slider(0.1f, 3f, 0.001f, false, skin.get(Slider.SliderStyle.class));
        sliderLookY.setValue(options.mouseYSensitivity);

        Label lookYLabel = new Label(StringManager.get("screens.OptionsScreen.lookYLabel"), skin.get(Label.LabelStyle.class));
        mainTable.add(lookYLabel);
        mainTable.add(sliderLookY);
        mainTable.row();

        addGamepadButtonOrder(sliderLookY, lookYLabel);

        // Invert Look

        Label invertLookLabel = new Label(StringManager.get("screens.OptionsScreen.invertLookLabel"), skin.get(Label.LabelStyle.class));
        mainTable.add(invertLookLabel);

        invertLook = new CheckBox("", skin.get(CheckBox.CheckBoxStyle.class));
        invertLook.setChecked((options.mouseInvert));

        mainTable.add(invertLook);
        mainTable.row();

        addGamepadButtonOrder(invertLook, invertLookLabel);

        // Mouse Buttons

        spinnerMouseButton1 = new ActionSpinnerButton(Options.instance.mouseButton1Action,skin.get(TextButton.TextButtonStyle.class));

        Label mouseButton1Label = new Label(StringManager.get("screens.OptionsScreen.mouseButton1Label"), skin.get(Label.LabelStyle.class));
        mainTable.add(mouseButton1Label);
        mainTable.add(spinnerMouseButton1);
        mainTable.row();

        addGamepadButtonOrder(spinnerMouseButton1, mouseButton1Label);

        spinnerMouseButton2 = new ActionSpinnerButton(Options.instance.mouseButton2Action,skin.get(TextButton.TextButtonStyle.class));

        Label mouseButton2Label = new Label(StringManager.get("screens.OptionsScreen.mouseButton2Label"), skin.get(Label.LabelStyle.class));
        mainTable.add(mouseButton2Label);
        mainTable.add(spinnerMouseButton2);
        mainTable.row();

        addGamepadButtonOrder(spinnerMouseButton2, mouseButton2Label);

        spinnerMouseButton3 = new ActionSpinnerButton(Options.instance.mouseButton3Action,skin.get(TextButton.TextButtonStyle.class));

        Label mouseButton3Label = new Label(StringManager.get("screens.OptionsScreen.mouseButton3Label"), skin.get(Label.LabelStyle.class));
        mainTable.add(mouseButton3Label);
        mainTable.add(spinnerMouseButton3);
        mainTable.row();

        addGamepadButtonOrder(spinnerMouseButton3, mouseButton3Label);

        // Mouse Scroller

        Label mouseScrollLabel = new Label(StringManager.get("screens.OptionsScreen.mouseScrollLabel"), skin.get(Label.LabelStyle.class));
        mainTable.add(mouseScrollLabel);

        chkMouseScroller = new CheckBox("", skin.get(CheckBox.CheckBoxStyle.class));
        chkMouseScroller.setChecked(options.useMouseScroller);

        mainTable.add(chkMouseScroller);
        mainTable.row();

        addGamepadButtonOrder(chkMouseScroller, mouseScrollLabel);

        // Button Row

        Table buttonTable = new Table();

        buttonTable.add(backBtn);
        buttonTable.add(keysBtn);
        buttonTable.add(gamepadBtn);

        buttonTable.getCell(backBtn).padRight(4);
        buttonTable.getCell(keysBtn).padRight(4);
        buttonTable.pack();

        buttonOrder.add(backBtn);
        buttonOrder.add(keysBtn);
        buttonOrder.add(gamepadBtn);

        mainTable.add(buttonTable);

        mainTable.getCell(header).align(Align.center).colspan(3).padBottom(8);
        mainTable.getCell(buttonTable).colspan(3).padTop(8);
        mainTable.pack();

        Table content = new Table();
        content.add(mainTable);
        content.pack();

        return content;
    }

    @Override
    public void tick(float delta) {

        if(valueLookXLast != sliderLookX.getValue()) {
            valueLookXLast = sliderLookX.getValue();
        }

        if((valueLookYLast != sliderLookY.getValue()) || (invertLookLastValue != invertLook.isChecked())) {
            valueLookYLast = sliderLookY.getValue();
            invertLookLastValue = invertLook.isChecked();
        }

        if(mouseScrollerLastValue != chkMouseScroller.isChecked()) {
            mouseScrollerLastValue = chkMouseScroller.isChecked();
        }

        // back
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if (mainTable.isVisible()) {
                saveAndClose();
            }
        }

        super.tick(delta);
    }

    public void saveAndClose() {
        saveOptions();
        OverlayManager.instance.replaceCurrent(new OptionsOverlay(dimScreen, showBackground));
    }

    public void saveOptions() {
        Options.instance.mouseInvert = invertLook.isChecked();
        Options.instance.mouseXSensitivity = sliderLookX.getValue();
        Options.instance.mouseYSensitivity = sliderLookY.getValue();
        Options.instance.mouseButton1Action = spinnerMouseButton1.getValue();
        Options.instance.mouseButton2Action = spinnerMouseButton2.getValue();
        Options.instance.mouseButton3Action = spinnerMouseButton3.getValue();

        Options.saveOptions();
    }
}