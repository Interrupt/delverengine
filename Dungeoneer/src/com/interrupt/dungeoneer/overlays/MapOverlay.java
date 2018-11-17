package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
//import com.esotericsoftware.tablelayout.Cell;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.Actions.Action;
import com.interrupt.dungeoneer.input.ControllerState;
import com.interrupt.dungeoneer.ui.UiSkin;

public class MapOverlay extends Overlay {

    TextureRegion mapArrowRegion = null;
    Vector2 mapOffset = new Vector2();
    public float timer = 0;

    Interpolation lerp = Interpolation.sine;
    protected float lerpValue = 0;

    Image mapImage;
    Image markerImage;
    Table mainTable;

    public MapOverlay() {
        pausesGame = true;
        showCursor = true;
    }

    @Override
    public void tick(float delta) {
        if(running) {
            if (Game.gamepadManager.controllerState.buttonEvents.size > 0 ||
                Game.gamepadManager.controllerState.menuButtonEvents.size > 0) {

                OverlayManager.instance.pop();
                Game.gamepadManager.controllerState.clearEvents();
                Game.gamepadManager.controllerState.resetState();
            }

            timer += delta;

            lerpValue = lerp.apply(0, 1f, Math.min(timer * 4, 1f));

            if (mapImage != null && markerImage != null) {
                markerImage.setWidth(10);
                markerImage.setHeight(10);
                Vector2 modified = mapImage.localToStageCoordinates(
                        new Vector2(
                                mapImage.getWidth() * ((Game.instance.player.x + 0.5f) / (float) Game.instance.level.width),
                                mapImage.getHeight() * (1 - ((Game.instance.player.y + 0.5f) / (float) Game.instance.level.height))));

                markerImage.setX(modified.x - markerImage.getWidth() / 2f);
                markerImage.setY(modified.y - markerImage.getHeight() / 2f);
                markerImage.setOrigin(markerImage.getWidth() / 2f, markerImage.getHeight() / 2f);
                markerImage.setRotation(Game.instance.player.rot * 57.2957795f + 180f);

                markerImage.setColor(1, 1, 1, 0.75f + (float) Math.sin(timer * 6f) * 0.25f);
                markerImage.setScale(1.1f + (float) Math.sin(timer * 6f) * 0.1f);
            }
        }
    }

    @Override
    public void onShow() {
        renderer = GameManager.renderer;

        mapArrowRegion =
                com.interrupt.dungeoneer.gfx.TextureAtlas
                        .getCachedRegion(Entity.ArtType.item.toString()).getSprite(62);

        InputProcessor processor = new InputProcessor() {
            @Override
            public boolean keyDown(int i) {
                if(i == Input.Keys.ESCAPE || i == Actions.keyBindings.get(Actions.Action.MAP)) {
                    OverlayManager.instance.pop();
                }
                return false;
            }

            @Override
            public boolean keyUp(int i) {
                return false;
            }

            @Override
            public boolean keyTyped(char c) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        		if ((button==0)&&(Options.instance.mouseButton1Action == Action.MAP) || 
        			(button==1)&&(Options.instance.mouseButton2Action == Action.MAP) ||
        			(button==2)&&(Options.instance.mouseButton3Action == Action.MAP)
        		) {
                    OverlayManager.instance.pop();
        		}
                return false;
            }

            @Override
            public boolean touchUp(int i, int i2, int i3, int i4) {
                return false;
            }

            @Override
            public boolean touchDragged(int i, int i2, int i3) {
                return false;
            }

            @Override
            public boolean mouseMoved(int i, int i2) {
                return false;
            }

            @Override
            public boolean scrolled(int i) {
                return false;
            }
        };

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        float scaleFactor = width < height * 1.5 ? width : height;
        float scaleMod = width < height * 1.5 ? 480f * 1.5f : 480f;
        float uiScale = (scaleFactor / scaleMod) * 2.4f;

        Skin skin = UiSkin.getSkin();
        FillViewport viewport = new FillViewport(Gdx.graphics.getWidth() / uiScale, Gdx.graphics.getHeight() / uiScale);
        ui = new Stage(viewport);

        if(pausesGame)
            Gdx.input.setInputProcessor(processor);

        Table stageTable = null;
        if(stageTable == null) {
            stageTable = new Table();
            ui.addActor(stageTable);
        }
        else {
            stageTable.clear();
        }

        stageTable.setFillParent(true);

        mainTable = new Table();

        NinePatchDrawable background = new NinePatchDrawable(new NinePatch(UiSkin.getSkin().getRegion("map-window"), 8, 8, 8, 8));

        float mapSize = (270) * 0.472f;
        float mapAspectRatio = (float)renderer.mapTextureRegion.getRegionWidth() / renderer.mapTextureRegion.getRegionHeight();

        if(background != null) mainTable.setBackground(background);

        Label mapName = new Label(Game.instance.player.levelName, skin);
        mapName.setColor(0, 0, 0, 0.85f);
        mapName.setFontScale(0.75f);
        mapName.setAlignment(Align.center);

        Table mapTable = new Table();

        mapImage = new Image(new TextureRegionDrawable(renderer.mapTextureRegion));
        mapImage.setColor(1f, 0.6f, 0.6f, 0.6f);
        markerImage = new Image(new TextureRegionDrawable(mapArrowRegion));
        markerImage.setVisible(false);

        mapTable.add(mapImage);

        Table sidebarTable = new Table(skin);
        sidebarTable.left();
        sidebarTable.setWidth(mapSize * 0.475f);
        sidebarTable.add(mapName).padTop(mapSize * 0.021f).align(Align.top);
        sidebarTable.row();

        mainTable.add(mapTable).width(mapSize * mapAspectRatio).height(mapSize);
        mainTable.add(sidebarTable).padLeft(mapSize * 0.345f).align(Align.top);
        mainTable.pack();

        stageTable.add(mainTable).width(270).height(184).align(Align.center).center();

        ui.addActor(markerImage);

        mapOffset.x = -1024;

        ui.act(0.1f);
        Game.gamepadManager.controllerState.buttonEvents.clear();
        tick(0);
    }

    @Override
    public void onHide() {

    }

    @Override
    protected void draw(float delta) {

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        float scaleFactor = width < height * 1.5 ? width : height;
        float scaleMod = width < height * 1.5 ? 480f * 1.5f : 480f;
        float uiScale = (scaleFactor / scaleMod) * 2.4f;

        if(ui != null) {
            ui.getViewport().setWorldSize(Gdx.graphics.getWidth() / uiScale, Gdx.graphics.getHeight() / uiScale);
        }

        renderer = GameManager.renderer;
        gl = renderer.getGL();

        renderer.uiBatch.begin();
        renderer.uiBatch.enableBlending();
        renderer.uiBatch.setColor(0, 0, 0, 0.5f * lerpValue);
        renderer.uiBatch.draw(renderer.flashRegion, -renderer.camera2D.viewportWidth / 2,-renderer.camera2D.viewportHeight / 2, renderer.camera2D.viewportWidth, renderer.camera2D.viewportHeight);
        renderer.uiBatch.end();

        mainTable.setColor(1f, 1f, 1f, Math.min(1f, lerpValue * 2f));

        super.draw(delta);

        markerImage.setVisible(true);
    }
}
