package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.shaders.ShaderData;
import com.interrupt.dungeoneer.ui.SpinnerButton;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.ShaderManager;
import com.interrupt.managers.StringManager;

public class OptionsGraphicsOverlay extends WindowOverlay {

    private Slider particleDensity;
    private Label particleDensityValueLabel;
    private float  particleDensityLastValue;
    private Slider fovSlider;
    private Label fovValueLabel;
    private CheckBox fxaaCheckbox;
    private Label fxaaLabel;
    private float lastFov = 0f;
    private float lastMsaa = 0f;

    public boolean doForcedValuesUpdate = false;

    private CheckBox shadows;
    private Slider postProcessingQualitySlider;
    private SpinnerButton<String> postProcessingOptions;

    private Table mainTable;

    Label aaWarning;

    public OptionsGraphicsOverlay() {
        animateBackground = false;
    }

    public OptionsGraphicsOverlay(boolean dimScreen, boolean showBackground)
    {
        animateBackground = false;
        this.dimScreen = dimScreen;
        this.showBackground = showBackground;
    }

    @Override
    public Table makeContent() {
        Options.loadOptions();

        if(Gdx.app.getType() == Application.ApplicationType.Android)
            Gdx.input.setCatchBackKey(true);

        skin = UiSkin.getSkin();

        TextButton backBtn = new TextButton(StringManager.get("screens.OptionsInputScreen.backButton"), skin.get(TextButton.TextButtonStyle.class));
        backBtn.setWidth(200);
        backBtn.setHeight(50);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveAndClose();
            }
        });

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.columnDefaults(0).align(Align.left).padRight(4);
        mainTable.columnDefaults(1).align(Align.left).padLeft(4);
        mainTable.columnDefaults(2).align(Align.right);

        // Table Header

        Label header = new Label(StringManager.getOrDefaultTo("screens.OptionsGraphicsScreen.headerLabel", "Advanced Graphics Options"), skin.get(Label.LabelStyle.class));
        header.setFontScale(1.1f);
        mainTable.add(header);
        mainTable.row();

        addMainContent();

        // Button Row
        Table buttonTable = new Table();
        buttonTable.add(backBtn);

        mainTable.add(buttonTable);

        mainTable.getCell(header).align(Align.center).colspan(3).padBottom(8);
        mainTable.getCell(buttonTable).colspan(3).padTop(8);
        mainTable.pack();

        Table content = new Table();
        content.add(mainTable);
        content.pack();

        doForcedValuesUpdate = true;

        buttonOrder.add(backBtn);

        return content;
    }

    public void addMainContent() {
        Options options = Options.instance;

        // FOV
        fovSlider = new Slider(50f, 110f, 1f, false, skin.get(Slider.SliderStyle.class));
        fovSlider.setValue(options.fieldOfView);

        Label fovLabel = new Label(StringManager.get("screens.OptionsScreen.fovLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(fovLabel);
        mainTable.add(fovSlider);
        fovValueLabel = new Label("xxx",skin.get(Label.LabelStyle.class));
        fovValueLabel.setAlignment(Align.right);
        mainTable.add(fovValueLabel);
        mainTable.row();

        addGamepadButtonOrder(fovSlider, fovLabel);

        // Particle Density
        particleDensity = new Slider(0f, 1f, 0.01f, false, skin.get(Slider.SliderStyle.class));
        particleDensity.setValue(Options.instance.gfxQuality);

        Label particleDensityLabel = new Label(StringManager.get("screens.OptionsScreen.particleDensityLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(particleDensityLabel);
        mainTable.add(particleDensity);

        particleDensityValueLabel=new Label("x.x",skin.get(Label.LabelStyle.class));
        particleDensityValueLabel.setAlignment(Align.right);
        mainTable.add(particleDensityValueLabel);
        mainTable.row();

        addGamepadButtonOrder(particleDensity, particleDensityLabel);

        // Post Processing Quality
        Label postProcessingQualityLabel = new Label(StringManager.get("screens.OptionsScreen.postProcessingQualityLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(postProcessingQualityLabel);
        postProcessingQualitySlider = new Slider(0, 3, 1, false, skin.get(Slider.SliderStyle.class));
        postProcessingQualitySlider.setValue(Options.instance.postProcessingQuality);
        mainTable.add(postProcessingQualitySlider);
        mainTable.row();

        addGamepadButtonOrder(postProcessingQualitySlider, postProcessingQualityLabel);

        // Post Processing Filter
        Label postProcessingLabel = new Label(StringManager.get("screens.OptionsScreen.postProcessingLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(postProcessingLabel);
        postProcessingOptions = new SpinnerButton<String>(getPostProcessFilters(), skin.get(TextButton.TextButtonStyle.class));

        if(Options.instance.postProcessFilter != null) {
            postProcessingOptions.setValue(Options.instance.postProcessFilter.replace("post_filter_", ""));
        }

        mainTable.add(postProcessingOptions);
        mainTable.row();

        addGamepadButtonOrder(postProcessingOptions, postProcessingLabel);

        // Anti Aliasing
        fxaaCheckbox = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
        fxaaCheckbox.setChecked(Options.instance.fxaaEnabled);

        Label antiAliasingLabel = new Label(StringManager.getOrDefaultTo("screens.OptionsScreen.antiAliasingLabel", "Anti Aliasing (FXAA)"),skin.get(Label.LabelStyle.class));
        mainTable.add(antiAliasingLabel);
        mainTable.add(fxaaCheckbox);
        mainTable.row();

        addGamepadButtonOrder(fxaaCheckbox, antiAliasingLabel);

        // Shadows
        Label shadowsLabel = new Label(StringManager.get("screens.OptionsScreen.shadowsLabel"),skin.get(Label.LabelStyle.class));
        mainTable.add(shadowsLabel);

        shadows = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
        shadows.setChecked(Options.instance.shadowsEnabled);

        mainTable.add(shadows);
        mainTable.row();

        addGamepadButtonOrder(shadows, shadowsLabel);

        // VSync
        Label vsyncLabel = new Label(StringManager.getOrDefaultTo("screens.OptionsScreen.vsyncLabel", "VSync"),skin.get(Label.LabelStyle.class));
        mainTable.add(vsyncLabel);
        final CheckBox vsyncCheckbox = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
        vsyncCheckbox.setChecked(Options.instance.vsyncEnabled);

        vsyncCheckbox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Options.instance.vsyncEnabled = vsyncCheckbox.isChecked();
                try {
                    Gdx.graphics.setVSync(Options.instance.vsyncEnabled);
                }
                catch(Exception ex) {
                    Gdx.app.log("Renderer", ex.getMessage());
                }
            }});

        mainTable.add(vsyncCheckbox);
        mainTable.row();

        addGamepadButtonOrder(vsyncCheckbox, vsyncLabel);

        // FPS limit
        Label limitFpsLabel = new Label(StringManager.getOrDefaultTo("screens.OptionsScreen.limitFpsLabel", "Limit FPS"),skin.get(Label.LabelStyle.class));
        mainTable.add(limitFpsLabel);
        final CheckBox limitFpsCheckbox = new CheckBox(null, skin.get(CheckBox.CheckBoxStyle.class));
        limitFpsCheckbox.setChecked(Options.instance.fpsLimit != 0);

        limitFpsCheckbox.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(limitFpsCheckbox.isChecked()) {
                    Options.instance.fpsLimit = 120;
                }
                else {
                    Options.instance.fpsLimit = 0;
                }
            }});

        mainTable.add(limitFpsCheckbox);
        mainTable.row();

        addGamepadButtonOrder(limitFpsCheckbox, limitFpsLabel);
    }

    public void tick(float delta) {
        updateValues();

        // back
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            saveAndClose();
        }

        super.tick(delta);
    }

    public void updateValues() {
        if(doForcedValuesUpdate || (particleDensityLastValue != particleDensity.getValue())) {
            particleDensityLastValue = particleDensity.getValue();
            particleDensityValueLabel.setText(String.format("%5.0f",100*particleDensityLastValue) + "%");
        }

        boolean postProcessingWasEnabled = Options.instance.enablePostProcessing;

        if(doForcedValuesUpdate || (lastFov != fovSlider.getValue())) {
            lastFov = fovSlider.getValue();
            GameManager.renderer.camera.fieldOfView = lastFov;
            Options.instance.fieldOfView = lastFov;
            fovValueLabel.setText(String.format("%1.0f",lastFov));
        }

        Options.instance.shadowsEnabled = shadows.isChecked();
        Options.instance.fxaaEnabled = fxaaCheckbox.isChecked();
        Options.instance.gfxQuality = particleDensity.getValue();
        Options.instance.postProcessingQuality = (int)postProcessingQualitySlider.getValue();
        Options.instance.enablePostProcessing = (!postProcessingOptions.getValue().equals("Off") && Options.instance.postProcessingQuality != 0) || Options.instance.fxaaEnabled;
        Options.instance.postProcessFilter = "post_filter_" + postProcessingOptions.getValue();

        if(Options.instance.postProcessFilter.toLowerCase().equals("post_filter_off")) {
            Options.instance.postProcessFilter = null;
        }

        if(Options.instance.enablePostProcessing != postProcessingWasEnabled) {
            GameManager.renderer.CreateFrameBuffers(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        doForcedValuesUpdate=false;
    }

    public void saveAndClose() {
        saveOptions();
        OverlayManager.instance.replaceCurrent(new OptionsOverlay(dimScreen, showBackground));
    }

    public void saveOptions() {
        Options.instance.gfxQuality = particleDensity.getValue();
        Options.instance.fieldOfView = fovSlider.getValue();
        Options.instance.shadowsEnabled = shadows.isChecked();
        Options.instance.fxaaEnabled = fxaaCheckbox.isChecked();
        Options.instance.postProcessingQuality = (int)postProcessingQualitySlider.getValue();
        Options.instance.enablePostProcessing = (!postProcessingOptions.getValue().equals("Off") && Options.instance.postProcessingQuality != 0) || Options.instance.fxaaEnabled;
        Options.instance.postProcessFilter = "post_filter_" + postProcessingOptions.getValue();

        if(Options.instance.postProcessFilter.toLowerCase().equals("post_filter_off")) {
            Options.instance.postProcessFilter = null;
        }


        Options.saveOptions();
    }

    public Array<String> getPostProcessFilters() {
        Array<String> filters = new Array<String>();
        filters.add("Off");

        for(ObjectMap.Entry<String, ShaderData> shader : ShaderManager.getShaderManager().shaders.entries()) {
            String s = shader.key;
            if(s.startsWith("post_filter_")) {
                filters.add(s.replace("post_filter_", ""));
            }
        }

        return filters;
    }
}
