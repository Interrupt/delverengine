package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.modes.EditorMode;

public class ToolMenuBar extends Container<Actor> {
    private ButtonGroup<Button> buttonGroup;
    private HorizontalGroup horizontalGroup;
    private HorizontalGroup subtoolsHorizontalGroup;

    private Table toolsTable;

    public ToolMenuBar() {

    }

    public void initialize() {
        fillX();
        align(Align.topLeft);

        EditorMode.EditorModes[] modes = EditorMode.EditorModes.class.getEnumConstants();

        toolsTable = new Table();

        horizontalGroup = new HorizontalGroup();
        horizontalGroup.space(2.0f);

        subtoolsHorizontalGroup = new HorizontalGroup();
        subtoolsHorizontalGroup.space(2.0f);

        buttonGroup = new ButtonGroup<>();

        for (EditorMode.EditorModes mode : modes) {
            // Only add in the top level modes.
            EditorMode editorMode = Editor.app.editorModes.get(mode);
            if(!editorMode.showAtTopLevel)
                continue;

            Button button = new TextButton(mode.toString(), EditorUi.smallSkin, "toggle");
            buttonGroup.add(button);
            horizontalGroup.addActor(button);
        }

        // Add the main tools group
        toolsTable.add(horizontalGroup).align(Align.left);
        toolsTable.row();

        // Add the subtools group
        toolsTable.add(subtoolsHorizontalGroup).align(Align.left);

        setActor(toolsTable);

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
                if(Editor.app.getCurrentEditorMode().showAtTopLevel) {
                    EditorMode.EditorModes current = Editor.app.getCurrentEditorMode().mode;
                    setChecked(current);
                }

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

    public void setSubtools(Array<EditorMode.EditorModes> subModes) {
        // Empty the old ones
        subtoolsHorizontalGroup.clear();
        subtoolsHorizontalGroup.align(Align.left);

        ButtonGroup subtoolsButtonGroup = new ButtonGroup<>();

        for (EditorMode.EditorModes mode : subModes) {
            Button button = new TextButton(mode.toString(), EditorUi.smallSkin, "toggle");
            subtoolsButtonGroup.add(button);
            subtoolsHorizontalGroup.addActor(button);
        }

        subtoolsButtonGroup.uncheckAll();
    }
}
