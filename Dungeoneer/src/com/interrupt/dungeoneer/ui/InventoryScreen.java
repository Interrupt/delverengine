package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;

public class InventoryScreen {
    protected Table contentTable = new Table();
    protected Table outerWindow = new Table();

    protected Player player;
    protected Skin skin;

    protected float fontScale = 2f;

    public InventoryScreen(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;
        fontScale = 2f;
    }

    public void makeContent() {
        float uiScale = Options.instance != null ? Options.instance.uiSize : 1f;
        uiScale *= Game.getDynamicUiScale();

        fontScale = 2f;

        outerWindow.clear();
        contentTable.clear();

        float buttonSizeX = 60f * (uiScale + 0.25f);
        float buttonSizeY = 40f * (uiScale + 0.25f);

        Table buttonTable = new Table();

        Button invButton = new Button(UiSkin.getSkin());
        invButton.add(new Image(new TextureRegionDrawable(UiSkin.getSkin().getRegion("menu-inv-btn-active")))).size(buttonSizeX, buttonSizeY);
        invButton.addListener( new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                Game.instance.setMenuMode(Game.MenuMode.Inventory);
            }
        } );

        Button charButton = new Button(UiSkin.getSkin());
        charButton.add(new Image(new TextureRegionDrawable(UiSkin.getSkin().getRegion("menu-char-btn-inactive")))).size(buttonSizeX, buttonSizeY);
        charButton.addListener( new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                Game.instance.setMenuMode(Game.MenuMode.Character);
            }
        } );

        buttonTable.add(invButton).align(Align.top).row();
        buttonTable.add(charButton).align(Align.top).row();
        buttonTable.pack();

        outerWindow.add(contentTable).width(480.1f * uiScale);
        outerWindow.add(buttonTable).align(Align.top);
        outerWindow.row();

        contentTable.setFillParent(false);
        outerWindow.setFillParent(true);
        outerWindow.align(Align.top);
        outerWindow.setY(-136f * uiScale);
        outerWindow.setX(buttonTable.getWidth() * 0.5f);

        if(!Game.ui.getActors().contains(outerWindow, true))
            Game.ui.addActor(outerWindow);
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public boolean isVisible() {
        return outerWindow.isVisible();
    }

    public void setVisible(boolean visible) {
        if(visible) {
            makeContent();
        }
        outerWindow.setVisible(visible);
    }

    public void resize() {
        makeContent();
    }
}
