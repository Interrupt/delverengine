package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class ResizeLevelDialog extends Dialog {

    TextField widthField;
    TextField heightField;

    public ResizeLevelDialog(Skin skin, Integer currentWidth, Integer currentHeight) {
        super("Resize Level", skin);

        widthField = new TextField(currentWidth.toString(), skin);
        heightField = new TextField(currentHeight.toString(), skin);

        getContentTable().add(new Label("Width", skin));
        getContentTable().add(widthField);
        getContentTable().row();
        getContentTable().add(new Label("Height", skin));
        getContentTable().add(heightField);
        getContentTable().row();

        button("Create", true);
        button("Cancel", false);
    }

    public int getLevelWidth() {
        if(widthField.getText().equals("")) return 17;
        return Integer.parseInt(widthField.getText());
    }

    public int getLevelHeight() {
        if(heightField.getText().equals("")) return 17;
        return Integer.parseInt(heightField.getText());
    }
}