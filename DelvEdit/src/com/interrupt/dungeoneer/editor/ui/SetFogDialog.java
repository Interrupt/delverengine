package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.interrupt.dungeoneer.game.Level;

import java.text.DecimalFormat;

public class SetFogDialog extends Dialog {

    EditorColorPicker fogPicker;
    EditorColorPicker sunPicker;

    public SetFogDialog(Skin skin, final Level level) {
        super("Set Fog Settings", skin);

        level.skyLightColor.a = 1;

        fogPicker = new EditorColorPicker(200, 200, level.fogColor) {
            @Override
            public void onSetValue(Color newColor) {
                level.fogColor.set(newColor);
            }
        };

        sunPicker = new EditorColorPicker(200, 200, level.skyLightColor) {
            @Override
            public void onSetValue(Color newColor) {
                level.skyLightColor.set(newColor);
            }
        };

        getContentTable().add(new Label("Fog Color", skin));
        getContentTable().add(fogPicker);
        getContentTable().row();

        getContentTable().add(new Label("Sun Color", skin));
        getContentTable().add(sunPicker);
        getContentTable().row();

        /*getContentTable().add(new Label("Fog Start Distance", skin));
        getContentTable().add(fogStartField);
        getContentTable().row();

        getContentTable().add(new Label("Fog End Distance", skin));
        getContentTable().add(fogEndField);
        getContentTable().row();*/

        button("Done", true);
    }

    public TextField MakeNumericField(float v, Skin skin) {
        final TextField tf = new TextField(Float.toString(v), skin);
        tf.setTextFieldFilter(new PropertiesMenu.DecimalsFilter());
        tf.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                DecimalFormat format = new DecimalFormat("##.##");
                if(keycode == Input.Keys.UP) {
                    double dval = 0;
                    if(!tf.getText().equals("")) dval = Double.parseDouble(tf.getText());
                    dval += 0.1;
                    tf.setTextFieldFilter(null);
                    tf.setText(format.format(dval));
                    tf.setTextFieldFilter(new PropertiesMenu.DecimalsFilter());
                }
                else if(keycode == Input.Keys.DOWN) {
                    double dval = 0;
                    if(!tf.getText().equals("")) dval = Double.parseDouble(tf.getText());
                    dval -= 0.1;
                    tf.setTextFieldFilter(null);
                    tf.setText(format.format(dval));
                    tf.setTextFieldFilter(new PropertiesMenu.DecimalsFilter());
                }
                return false;
            }
        });

        return tf;
    }

    /*public float getFogStartValue() {
        return Float.parseFloat(fogStartField.getText());
    }

    public float getFogEndValue() {
        return Float.parseFloat(fogEndField.getText());
    }*/
}
