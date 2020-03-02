package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class CharacterScreen {
    protected Table contentTable = new Table();
    protected Table outerWindow = new Table();

    protected Player player;
    protected Skin skin;

    protected float fontScale = 2f;
    protected float uiScale = 1f;

    public CharacterScreen(Player player, Skin skin) {
        this.player = player;
        this.skin = skin;
        fontScale = 2f;
    }

    public void makeContent() {

        player = Game.instance.player;

        uiScale = Options.instance != null ? Options.instance.uiSize : 1f;
        uiScale *= Game.getDynamicUiScale();

        fontScale = 2f;
        fontScale *= uiScale;

        outerWindow.clear();
        contentTable.clear();

        Table buttonTable = new Table();

        float buttonSizeX = 60f * (uiScale + 0.25f);
        float buttonSizeY = 40f * (uiScale + 0.25f);

        Button invButton = new Button(UiSkin.getSkin());
        invButton.add(new Image(new TextureRegionDrawable(UiSkin.getSkin().getRegion("menu-inv-btn-inactive")))).size(buttonSizeX, buttonSizeY);
        invButton.addListener( new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                Game.instance.setMenuMode(Game.MenuMode.Inventory);
            }
        } );

        Button charButton = new Button(UiSkin.getSkin());
        charButton.add(new Image(new TextureRegionDrawable(UiSkin.getSkin().getRegion("menu-char-btn-active")))).size(buttonSizeX, buttonSizeY);
        charButton.addListener( new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                Game.instance.setMenuMode(Game.MenuMode.Character);
            }
        } );

        buttonTable.add(invButton).align(Align.top).row();
        buttonTable.add(charButton).align(Align.top).row();
        buttonTable.pack();

        outerWindow.add(contentTable).width(480f * uiScale);
        outerWindow.add(buttonTable).align(Align.top);
        outerWindow.row();

        // Start adding content
        makeTitle(StringManager.get("overlays.CharacterOverlay.attributesTitle"));

        String attackText = StringManager.get("gfx.GlRenderer.AttackText");
        MessageFormat.format(attackText, player.GetAttackText());

        String armorClassText = StringManager.get("gfx.GlRenderer.ArmorClassText");
        MessageFormat.format(armorClassText, player.GetArmorClass());

        Table attributesTable = new Table();
        makeRow(attributesTable, "Attack Power", player.GetAttackText());
        makeRow(attributesTable, "Armor Class", Integer.toString(player.GetArmorClass()));
        contentTable.add(attributesTable).colspan(4);
        contentTable.row();
        makeRow(contentTable);

        // Statistics
        makeTitle(StringManager.get("overlays.CharacterOverlay.statisticsTitle"));
        makeStats();

        // Player info
        makeTitle(StringManager.get("overlays.CharacterOverlay.playerTitle"));
        Table playerTable = new Table();
        makeRow(playerTable,"Level", Integer.toString(player.level));
        makeRow(playerTable,"XP", player.exp + "/" + player.getNextLevel());
        makeRow(playerTable,"Gold", Integer.toString(player.gold));
        contentTable.add(playerTable).colspan(4);
        contentTable.row();

        NinePatchDrawable background = new NinePatchDrawable(new NinePatch(UiSkin.getSkin().getRegion("inventory-window"), 16, 16, 16, 16));
        contentTable.setBackground(background);
        contentTable.setFillParent(false);
        outerWindow.setFillParent(true);
        outerWindow.align(Align.top);
        outerWindow.setY(-136f * uiScale);
        outerWindow.setX(buttonTable.getWidth() * 0.5f);

        if(!Game.ui.getActors().contains(outerWindow, true))
            Game.ui.addActor(outerWindow);
    }

    private void makeTitle(String title) {
        Label label = new Label(title, skin.get(Label.LabelStyle.class));
        label.setColor(Colors.PARALYZE);
        label.setFontScale(fontScale);
        contentTable.add(label).colspan(4).padBottom(12f * uiScale);
        contentTable.row();
    }

    private void makeStats() {
        Table table = new Table(UiSkin.getSkin());
        makeRow(table,"Health", player.stats.END, "Attack",  player.stats.ATK);
        makeRow(table,"Magic",   player.stats.MAG, "Defense", player.stats.DEF);
        makeRow(table,"Speed",   player.stats.SPD, "Agility", player.stats.DEX);
        makeRow(table);

        table.pack();
        contentTable.add(table).colspan(4);
        contentTable.row();
    }

    protected void makeRow(Table table) {
        table.add().height(16f * uiScale).row();
    }

    protected void makeRow(Table table, String labelText, String value) {
        Label label = new Label(StringManager.getOrDefaultTo("stats." + labelText.toLowerCase(), labelText), skin.get(Label.LabelStyle.class));
        Label valueLabel = new Label(value, skin.get(Label.LabelStyle.class));

        label.setFontScale(fontScale);
        label.setColor(Color.LIGHT_GRAY);
        valueLabel.setFontScale(fontScale);
        valueLabel.setAlignment(Align.right);

        // add tooltips
        addTooltipListener(label, labelText);
        addTooltipListener(valueLabel, labelText);

        table.add(label).align(Align.left).padBottom(4f * uiScale).padRight(30f * uiScale);
        table.add(valueLabel).align(Align.right).minWidth(80f * uiScale).padRight(8f * uiScale);
        table.row();
    }

    protected void makeRow(Table table, String labelText, int value, String labelTextTwo, int valueTwo) {
        makeRow(table, labelText, Integer.toString(value), labelTextTwo, Integer.toString(valueTwo));
    }

    protected void makeRow(Table table, final String labelText, String value, String labelTextTwo, String valueTwo) {
        Label label = new Label(StringManager.getOrDefaultTo("stats." + labelText.toLowerCase(), labelText), skin.get(Label.LabelStyle.class));
        Label valueLabel = new Label(value, skin.get(Label.LabelStyle.class));

        Label labelTwo = new Label(StringManager.getOrDefaultTo("stats." + labelTextTwo.toLowerCase(), labelTextTwo), skin.get(Label.LabelStyle.class));
        Label valueLabelTwo = new Label(valueTwo, skin.get(Label.LabelStyle.class));

        label.setFontScale(fontScale);
        label.setColor(Color.LIGHT_GRAY);
        valueLabel.setFontScale(fontScale);
        valueLabel.setAlignment(Align.right);

        labelTwo.setFontScale(fontScale);
        labelTwo.setColor(Color.LIGHT_GRAY);
        valueLabelTwo.setFontScale(fontScale);
        valueLabelTwo.setAlignment(Align.right);

        table.add(label).align(Align.left).padBottom(4f * uiScale).padRight(8f * uiScale);
        table.add(valueLabel).align(Align.right).minWidth(60f * uiScale).padRight(18f * uiScale);

        table.add(labelTwo).align(Align.left).padLeft(8f * uiScale).padBottom(4f * uiScale).padRight(8f * uiScale);
        table.add(valueLabelTwo).minWidth(60f * uiScale).align(Align.right);

        // add tooltips
        addTooltipListener(label, labelText);
        addTooltipListener(valueLabel, labelText);

        addTooltipListener(labelTwo, labelTextTwo);
        addTooltipListener(valueLabelTwo, labelTextTwo);

        table.row();
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

    public void addTooltipListener(Label label, final String tooltip) {
        label.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                showTooltip(event.getStageX(), event.getStageY(), tooltip);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hideTooltip();
            }
        });
    }

    public void showTooltip(float x, float y, String tooltip) {
        Game.tooltip.show(x, y, tooltip);
    }

    public void hideTooltip() {
        Game.tooltip.hide();
    }

    public void resize() {
        makeContent();
    }
}
