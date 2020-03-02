package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.managers.StringManager;

public class Tooltip {

    private Table tooltipTable = null;
    private Table tooltipInnerTable = null;
    private Label tooltipLabel = null;

    private String showingHint = null;
    private Item showingItem = null;

    private Color tooltipColor = new Color(Colors.PARALYZE);

    private void init() {
        if(tooltipTable != null) {
            Game.ui.getActors().removeValue(tooltipTable, true);
        }

        tooltipTable = new Table(UiSkin.getSkin());
        tooltipTable.setBackground("tooltip-window");

        tooltipLabel = new Label("", UiSkin.getSkin().get(Label.LabelStyle.class));
        tooltipLabel.setWrap(true);

        tooltipInnerTable = new Table();
        tooltipInnerTable.add(tooltipLabel).width(320f).row();
        tooltipTable.add(tooltipInnerTable).row();
    }

    public void show(float x, float y, Item item) {
        if(item != showingItem && showingHint == null) {
            showingItem = item;

            String tooltipText = "[#" + item.GetTextColor().toString() + "]" + item.GetName() + "\n[#" + tooltipColor.toString() + "]" + item.GetInfoText();

            if(item.description != null) {
                String description = StringManager.getOrDefaultTo(item.description, item.description);

                if(description != null && description.length() > 0)
                    tooltipText += "\n" + "\n[#" + Color.GRAY.toString() + "]" + description;
            }

            if (tooltipTable == null || tooltipLabel == null) {
                init();
            }

            // Resize based on the UI scaling
            float uiScale = Game.getDynamicUiScale();
            Cell cell = tooltipInnerTable.getCell(tooltipLabel);

            float padding = uiScale * 2f;
            if(padding < 1f) {
                padding = Math.abs(padding) + 1.f;
                padding *= -1;
            }

            cell.pad(padding).width(320f * uiScale);

            tooltipLabel.setText(tooltipText);
            tooltipInnerTable.pack();
        }

        place(x, y);
    }

    // Show a tooltip using text looked up from the StringManager
    public void show(float x, float y, String tooltip) {
        if(showingHint == null || !showingHint.equals(tooltip)) {
            showingItem = null;
            showingHint = tooltip;

            String tooltipLookup = "screens.CharacterScreen.tooltips." + tooltip.toLowerCase();
            String tooltipText = StringManager.getOrDefaultTo(tooltipLookup, null);
            if (tooltipText == null) return;

            String tooltipTitle = StringManager.getOrDefaultTo("stats." + tooltip.toLowerCase(), tooltip);
            tooltipText = tooltipTitle + "\n[#" + tooltipColor.toString() + "]" + tooltipText;

            if (tooltipTable == null || tooltipLabel == null) {
                init();
            }

            tooltipLabel.setText(tooltipText);
        }

        place(x, y);
    }

    private void place(float x, float y) {

        // Might need to adjust for the gamepad cursor position
        if(Game.instance.input.getGamepadCursorPosition() != null) {
            x += Options.instance.uiSize * 75f;
        }

        float fontScale = Options.instance != null ? Options.instance.uiSize : 1f;
        fontScale *= Game.getDynamicUiScale() * 2f;

        if(fontScale < 1)
            fontScale = 1f;

        tooltipLabel.setFontScale(fontScale);
        tooltipTable.pack();

        if(!Game.ui.getActors().contains(tooltipTable, true)) {
            Game.ui.addActor(tooltipTable);
        }

        if(y + tooltipTable.getHeight() > Gdx.graphics.getHeight()) {
            //y -= (y + tooltipTable.getHeight()) - Gdx.graphics.getHeight();
            y -= tooltipTable.getHeight();
        }

        if(x + tooltipTable.getWidth() > Gdx.graphics.getWidth()) {
            //x -= (x + tooltipTable.getWidth()) - Gdx.graphics.getWidth();
            x -= tooltipTable.getWidth();
        }

        tooltipTable.setPosition(x, y + 1f);

        tooltipTable.clearActions();
        tooltipTable.addAction(Actions.fadeIn(0.001f));
        tooltipTable.setZIndex(100);
    }

    public void hide() {
        showingItem = null;
        showingHint = null;
        if(tooltipTable != null) {
            tooltipTable.addAction(Actions.sequence(Actions.fadeOut(5f), Actions.removeActor()));
        }
    }

    public boolean isShowingItem() {
        return showingItem != null;
    }
}
