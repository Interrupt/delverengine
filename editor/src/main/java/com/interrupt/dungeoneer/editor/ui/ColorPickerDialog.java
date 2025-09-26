package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class ColorPickerDialog extends Dialog {
    private final Color selectedColor = new Color();
    private final TextField redField;
    private final TextField blueField;
    private final TextField greenField;
    private final TextField alphaField;

    private final Slider saturationSlider;

    private final Pixmap colorPixmap;
    private final Texture colorTexture;

    public ColorPickerDialog(Skin skin, Color selected) {
        super("Pick a color", skin);
        if(selected != null) selectedColor.set(selected);

        int pickerWidth = 300;
        int pickerHeight = 300;

        final ColorHueImage hueImage = new ColorHueImage(pickerWidth, pickerHeight) {
            @Override
            public void onResult(Color picked) {
                selectedColor.set(picked);

                TextField.TextFieldFilter redFilter = redField.getTextFieldFilter();
                redField.setTextFieldFilter(null);
                redField.setText(Integer.toString((int) (selectedColor.r * 255)));
                redField.setTextFieldFilter(redFilter);

                TextField.TextFieldFilter greenFilter = greenField.getTextFieldFilter();
                greenField.setTextFieldFilter(null);
                greenField.setText(Integer.toString((int) (selectedColor.g * 255)));
                greenField.setTextFieldFilter(greenFilter);

                TextField.TextFieldFilter blueFilter = greenField.getTextFieldFilter();
                blueField.setTextFieldFilter(null);
                blueField.setText(Integer.toString((int)(selectedColor.b * 255)));
                blueField.setTextFieldFilter(blueFilter);

                updateColor();
            }
        };

        colorPixmap = new Pixmap(pickerWidth, 10, Pixmap.Format.RGB888);
        colorTexture = new Texture(colorPixmap);
        updateColor();
        Image colorImage = new Image(colorTexture);

        getContentTable().add(hueImage).colspan(2).width(pickerWidth).minWidth(pickerWidth).height(pickerWidth).minHeight(pickerWidth);

        redField = new TextField(Integer.toString((int) (selectedColor.r * 255)), skin);
        greenField = new TextField(Integer.toString((int)(selectedColor.g * 255)), skin);
        blueField = new TextField(Integer.toString((int)(selectedColor.b * 255)), skin);
        alphaField = new TextField(Integer.toString((int)(selectedColor.a * 255)), skin);

        saturationSlider = new Slider(0f, 1f, 0.001f, false, skin);
        saturationSlider.setValue(1f);
        saturationSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                hueImage.updateSaturation(saturationSlider.getValue());
            }
        });

        getContentTable().row();
        getContentTable().add(colorImage).colspan(2);

        getContentTable().row();
        getContentTable().add("Saturation").align(Align.left);
        getContentTable().add(saturationSlider).align(Align.left).fill();

        getContentTable().row();
        getContentTable().add("Red").align(Align.left);
        getContentTable().add(redField).align(Align.left).fill();
        getContentTable().row();
        getContentTable().add("Green").align(Align.left);
        getContentTable().add(greenField).fill().align(Align.left);
        getContentTable().row();
        getContentTable().add("Blue").align(Align.left);
        getContentTable().add(blueField).align(Align.left).fill();
        getContentTable().row();
        getContentTable().add("Alpha").align(Align.left);
        getContentTable().add(alphaField).align(Align.left).fill();

        redField.setTextFieldFilter(new PropertiesMenu.IntegerFilter(0, 255));
        redField.addListener(new InputListener() {
            public boolean keyTyped(InputEvent event, char character) {
                updateColorFromTextField();
                return super.keyTyped(event, character);
            }
        });

        greenField.setTextFieldFilter(new PropertiesMenu.IntegerFilter(0, 255));
        greenField.addListener(new InputListener() {
            public boolean keyTyped(InputEvent event, char character) {
                updateColorFromTextField();
                return super.keyTyped(event, character);
            }
        });

        blueField.setTextFieldFilter(new PropertiesMenu.IntegerFilter(0, 255));
        blueField.addListener(new InputListener() {
            public boolean keyTyped(InputEvent event, char character) {
                updateColorFromTextField();
                return super.keyTyped(event, character);
            }
        });

        alphaField.setTextFieldFilter(new PropertiesMenu.IntegerFilter(0, 255));
        alphaField.addListener(new InputListener() {
            public boolean keyTyped(InputEvent event, char character) {
                updateColorFromTextField();
                return super.keyTyped(event, character);
            }
        });

        button("Okay", selectedColor);
        button("Cancel", null);

        pack();
    }

    public void updateColor() {
        colorPixmap.setColor(selectedColor);
        colorPixmap.fill();
        colorTexture.draw(colorPixmap, 0, 0);
    }

    public void updateColorFromTextField() {
        try {
            selectedColor.r = Integer.parseInt(redField.getText()) / 255f;
            selectedColor.g = Integer.parseInt(greenField.getText()) / 255f;
            selectedColor.b = Integer.parseInt(blueField.getText()) / 255f;
            selectedColor.a = Integer.parseInt(alphaField.getText()) / 255f;
        }
        catch(Exception ex) { }
    }
}
