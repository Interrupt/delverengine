package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.editor.Editor;

public class EditorStatusBar extends Group {
    Table table;
    Label info;
    Image infoIcon;
    float infoTime = 0;
    Label location;

    private final Skin skin;

    public EditorStatusBar() {
        skin = EditorUi.getSmallSkin();
        table = new Table(skin);
        //table.setDebug(true, true);

        info = new Label("", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);

                if (infoTime > 5) {
                    //setText("");
                    infoIcon.setVisible(false);
                    setVisible(false);
                }

                infoTime += delta;
            }
        };

        try {
            infoIcon = new Image(skin, "statusbar-info-icon");
        }
        catch (Exception e) {
            infoIcon = new Image();
        }

        location = new Label("0, 0", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);

                float x;
                float y;
                float z;
                if (Editor.selection.picked == null) {
                    x = Editor.selection.tiles.x;
                    y = Editor.selection.tiles.y;
                    z = Editor.selection.tiles.first().floorHeight + 0.5f;
                }
                else {
                    x = Editor.selection.picked.x;
                    y = Editor.selection.picked.y;
                    z = Editor.selection.picked.z;
                }
                this.setText(String.format("X  %.3f,  Y %.3f,  Z %.3f", x, y, z));
            }
        };

        infoIcon.setVisible(false);
        table.add(infoIcon);
        table.add(info).align(Align.left).expand();
        table.add(location);

        addActor(table);
    }

    public void refresh() {
        refreshDrawables();
    }

    protected void refreshDrawables() {
        table.setBackground("menu_default_normal");
        table.align(Align.left);
        table.setWidth(Gdx.graphics.getWidth());
        table.setHeight(30);
    }

    public void showInfo(String message) {
        showMessage(message, "statusbar-info-icon");
    }

    public void showWarning(String message) {
        showMessage(message, "statusbar-warning-icon");
    }

    public void showError(String message) {
        showMessage(message, "statusbar-error-icon");
    }

    private void showMessage(String message, String drawableName) {
        Skin skin = EditorUi.getSmallSkin();
        infoTime = 0;

        info.setText(message);
        info.setVisible(true);

        try {
            infoIcon.setDrawable(skin.getDrawable(drawableName));
        }
        catch (Exception ignored) { }

        infoIcon.setVisible(true);
    }
}
