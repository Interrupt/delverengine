package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.editor.Editor;

public class SaveChangesDialog extends Dialog {
    public enum SaveChangesDialogResult {
        SAVE,
        CANCEL,
        DONT_SAVE
    }

    public SaveChangesDialog() {
        super("Save Changes?", EditorUi.getSmallSkin());

        Table contentTable = getContentTable();
        contentTable.align(Align.left);

        String docName = Editor.app.file.name();
        Label warningLabel = new Label("Do you want to save the changes you made to " + docName + "?", EditorUi.getMediumSkin());
        warningLabel.setWrap(true);
        contentTable.add(warningLabel).width(425).align(Align.left);
        contentTable.row();

        if (!Editor.app.file.directory().equals("")) {
            Label infoLabel = new Label("The last save was " + timeSinceLastSave() + " ago.", EditorUi.getSmallSkin());
            contentTable.add(infoLabel).width(425).align(Align.left);
        }
        else {
            contentTable.add(new Label("", EditorUi.getSmallSkin()));
        }

        Table buttonTable = getButtonTable();

        TextButton saveButton = new TextButton("Save", EditorUi.getSmallSkin());
        buttonTable.add(saveButton);
        setObject(saveButton, SaveChangesDialogResult.SAVE);

        TextButton cancelButton = new TextButton("Cancel", EditorUi.getSmallSkin());
        buttonTable.add(cancelButton).padRight(15);
        setObject(cancelButton, SaveChangesDialogResult.CANCEL);

        TextButton dontSaveButton = new TextButton("Don't Save", EditorUi.getSmallSkin());
        buttonTable.add(dontSaveButton);
        setObject(dontSaveButton, SaveChangesDialogResult.DONT_SAVE);
    }

    @Override
    public Dialog show (Stage stage) {
        if (!Editor.app.file.isDirty()) {
            onDontSave();
            return this;
        }

        return super.show(stage);
    }

    @Override
    public void result(Object object) {
        SaveChangesDialogResult result = (SaveChangesDialogResult)object;
        if (result == null) {
            return;
        }

        switch (result) {
            case SAVE:
                onSave();
                break;

            case CANCEL:
                onCancel();
                break;

            case DONT_SAVE:
                onDontSave();
                break;
        }
    }

    public void onSave() {}
    public void onCancel() {}
    public void onDontSave() {}

    @Override
    public float getPrefWidth() {
        return 450;
    }

    @Override
    public float getPrefHeight() {
        return 200;
    }

    private String timeSinceLastSave() {
        long hours = Editor.app.file.getHoursSinceLastSave();

        if (hours > 0) {
            long minutes = Editor.app.file.getMinutesSinceLastSave() % 60;

            return hours + " " + pluralize("hour", hours) + " and " + minutes + pluralize("minute", minutes);
        }

        long minutes = Editor.app.file.getMinutesSinceLastSave();

        if (minutes > 0) {
            return minutes + " " + pluralize("minute", minutes);
        }

        long seconds = Editor.app.file.getSecondsSinceLastSave();

        return seconds + " " + pluralize("second", seconds);
    }

    private String pluralize(String base, long amount) {
        if (amount == 1) {
            return base;
        }

        return base + "s";
    }
}
