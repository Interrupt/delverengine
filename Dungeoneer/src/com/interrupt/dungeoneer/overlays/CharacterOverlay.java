package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.entities.Player;

public class CharacterOverlay extends WindowOverlay {

    protected Table contentTable = new Table();
    protected Player player;

    protected float fontScale = 0.8f;

    public CharacterOverlay(Player player) {
        this.player = player;
        pausesGame = false;
        catchInput = false;
        animate = false;

        offset.set(0, -35f);
        align = Align.top;
    }

    @Override
    public Table makeContent() {
        makeRow("Health",  player.stats.END, "Level", player.level);
        makeRow("Magic",   player.stats.MAG, "Gold", player.gold);
        makeRow("Speed", Integer.toString(player.stats.SPD), "XP", player.exp + "/" + player.getNextLevel());
        makeRow("Attack",  player.stats.ATK);
        makeRow("Defense", player.stats.DEF);
        makeRow("Agility", player.stats.DEX);
        return contentTable;
    }

    protected void makeRow(String labelText, int value) {
        Label label = new Label(labelText, skin.get(Label.LabelStyle.class));
        Label valueLabel = new Label(Integer.toString(value), skin.get(Label.LabelStyle.class));

        label.setFontScale(fontScale);
        valueLabel.setFontScale(fontScale);

        contentTable.add(label).align(Align.left).padBottom(4f).padRight(10f);
        contentTable.add(valueLabel).minWidth(20f);
        contentTable.row();
    }

    protected void makeRow(String labelText, int value, String labelTextTwo, int valueTwo) {
        makeRow(labelText, Integer.toString(value), labelTextTwo, Integer.toString(valueTwo));
    }

    protected void makeRow(String labelText, String value, String labelTextTwo, String valueTwo) {
        Label label = new Label(labelText, skin.get(Label.LabelStyle.class));
        Label valueLabel = new Label(value, skin.get(Label.LabelStyle.class));

        Label labelTwo = new Label(labelTextTwo, skin.get(Label.LabelStyle.class));
        Label valueLabelTwo = new Label(valueTwo, skin.get(Label.LabelStyle.class));

        label.setFontScale(fontScale);
        valueLabel.setFontScale(fontScale);

        labelTwo.setFontScale(fontScale);
        valueLabelTwo.setFontScale(fontScale);

        contentTable.add(label).align(Align.left).padBottom(4f).padRight(10f);
        contentTable.add(valueLabel).minWidth(20f);

        contentTable.add(labelTwo).align(Align.left).padLeft(8f).padBottom(4f).padRight(4f);
        contentTable.add(valueLabelTwo);

        contentTable.row();
    }
}
