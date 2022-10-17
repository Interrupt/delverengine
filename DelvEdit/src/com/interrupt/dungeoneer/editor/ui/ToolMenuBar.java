package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.modes.EditorMode;

public class ToolMenuBar extends Container<Actor> {
    private ButtonGroup<Button> buttonGroup;
    private HorizontalGroup horizontalGroup;

    public ToolMenuBar() {
        initialize();
    }

    private void initialize() {
        fillX();
        align(Align.left);

        EditorMode.EditorModes[] modes = EditorMode.EditorModes.class.getEnumConstants();

        horizontalGroup = new HorizontalGroup();
        horizontalGroup.space(2.0f);

        buttonGroup = new ButtonGroup<>();

        for (EditorMode.EditorModes mode : modes) {
            Button button = new TextButton(mode.toString(), EditorUi.smallSkin, "toggle");
            buttonGroup.add(button);
            horizontalGroup.addActor(button);
        }

        setActor(horizontalGroup);

        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!(actor instanceof TextButton)) return;

                TextButton button = (TextButton)actor;

                if (button.isChecked()) {
                    EditorMode.EditorModes newMode = EditorMode.EditorModes.valueOf(button.getText().toString());
                    Editor.app.setCurrentEditorMode(newMode);
                }
            }
        });

        addAction(new Action() {
            @Override
            public boolean act(float delta) {
                EditorMode.EditorModes current = Editor.app.getCurrentEditorMode().mode;
                setChecked(current);
                return false;
            }
        });
    }

    public void setChecked(EditorMode.EditorModes mode) {
        Array<Button> buttons = buttonGroup.getButtons();
        buttonGroup.uncheckAll();

        for (Button b : buttons) {
            if (!(b instanceof TextButton)) continue;

            TextButton textButton = (TextButton)b;

            String text = textButton.getText().toString();

            if (text.equals(mode.toString())) {
                textButton.setChecked(true);
                return;
            }
        }
    }
}
