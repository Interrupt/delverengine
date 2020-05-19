package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.editor.Editor;

public class EditorStatusBar extends Group {
    Table table;
    Label info;
    float infoTime = 0;

    public EditorStatusBar() {
        final Skin skin = EditorUi.getSmallSkin();
        table = new Table(skin);
        table.setBackground("menu_default_normal");
        table.align(Align.left);
        table.setWidth(Gdx.graphics.getWidth());
        table.setHeight(30);

        //table.setDebug(true, true);

        // Info box
        info = new Label("", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);

                if (infoTime > 5) {
                    //setText("");
                    setVisible(false);
                }

                infoTime += delta;
            }
        };
        table.add(info).align(Align.left).expand();

        // Cursor location
        table.add(new Label("0, 0", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);

                int x = Editor.selection.tiles.x;
                int y = Editor.selection.tiles.y;
                this.setText(String.format("X %d, Y %d", x, y));
            }
        });

        addActor(table);
    }

    public void showMessage(String message) {
        infoTime = 0;
        info.setText(message);
        info.setVisible(true);
    }
}
