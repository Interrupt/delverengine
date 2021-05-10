package com.interrupt.dungeoneer.editor.ui.menu.generator;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.editor.ui.EditorUi;

public class RoomGeneratorSettingsDialog extends Dialog {
    private RoomGeneratorSettings settings;

    public RoomGeneratorSettingsDialog(Skin skin, RoomGeneratorSettings settings) {
        super("Room Generator Settings", skin);
        this.settings = RoomGeneratorSettings.copy(settings);

        getContentTable().pad(8);

        makeSectionHeader("Exits");
        makeBooleanInputField("Exit North", settings.northExit, new ApplyChangesHandler<Boolean>() {
            @Override
            public void apply(Boolean value, RoomGeneratorSettings settings) {
                settings.northExit = value;
            }
        });
        makeBooleanInputField("Exit East", settings.eastExit, new ApplyChangesHandler<Boolean>() {
            @Override
            public void apply(Boolean value, RoomGeneratorSettings settings) {
                settings.eastExit = value;
            }
        });
        makeBooleanInputField("Exit South", settings.southExit, new ApplyChangesHandler<Boolean>() {
            @Override
            public void apply(Boolean value, RoomGeneratorSettings settings) {
                settings.southExit = value;
            }
        });
        makeBooleanInputField("Exit West", settings.westExit, new ApplyChangesHandler<Boolean>() {
            @Override
            public void apply(Boolean value, RoomGeneratorSettings settings) {
                settings.westExit = value;
            }
        });

        getContentTable().pack();

        button("Save", true);
        button("Cancel", false);
    }

    private void makeSectionHeader(String label) {
        getContentTable().add(label).align(Align.left).padLeft(-8f);
        getContentTable().row();
    }

    private Label makeLabel(String name) {
        Label label = new Label(name, EditorUi.smallSkin);
        label.setColor(1f, 1f, 1f, 0.75f);

        return label;
    }

    private SelectBox<Boolean> makeSelectBox(boolean value, ApplyChangesHandler<Boolean> handler) {
        Boolean[] values = new Boolean[2];
        values[0] = true;
        values[1] = false;

        SelectBox<Boolean> selectBox = new SelectBox<>(EditorUi.smallSkin);
        selectBox.setItems(values);
        selectBox.setSelectedIndex(value ? 0 : 1);
        selectBox.addListener(new ChangeListener() {
            public void changed(ChangeEvent changeEvent, Actor actor) {
                handler.apply(selectBox.getSelected(), settings);
            }
        });

        return selectBox;
    }

    private void makeBooleanInputField(String label, Boolean value, ApplyChangesHandler<Boolean> handler) {
        getContentTable().add(makeLabel(label)).align(Align.left);
        getContentTable().add(makeSelectBox(value, handler)).align(Align.left).fill();
        getContentTable().row();
    }

    private interface ApplyChangesHandler<T> {
        public void apply(T value, RoomGeneratorSettings settings);
    }

    public RoomGeneratorSettings getSettings() {
        return settings;
    }
}
