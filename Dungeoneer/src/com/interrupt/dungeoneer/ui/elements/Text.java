package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.dungeoneer.ui.values.DynamicValue;
import com.interrupt.managers.StringManager;

import java.util.Objects;

public class Text extends Element {
    DynamicValue text;

    private String getText() {
        return StringManager.get(text.stringValue());
    }

    @Override
    protected Actor createActor() {
        Text self = this;
        Label label = new Label(self.getText(), UiSkin.getSkin()) {
            String value;
            @Override
            public void act(float delta) {
                super.act(delta);

                String current = self.getText();
                if (!Objects.equals(value, current)) {
                    value = current;
                    setText(value);
                }
            }
        };

        if (pivot.equals(com.interrupt.dungeoneer.ui.elements.Align.UNSET)) {
            pivot = anchor;
        }

        switch (pivot) {
            case BOTTOM_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottomLeft);
                break;

            case BOTTOM_CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottom | com.badlogic.gdx.utils.Align.center);
                break;

            case BOTTOM_RIGHT:
                label.setAlignment(com.badlogic.gdx.utils.Align.bottomRight);
                break;

            case CENTER_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.left | com.badlogic.gdx.utils.Align.center);
                break;

            case CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.center);
                break;

            case CENTER_RIGHT:
                label.setAlignment(com.badlogic.gdx.utils.Align.right | com.badlogic.gdx.utils.Align.center);
                break;

            case TOP_LEFT:
                label.setAlignment(com.badlogic.gdx.utils.Align.topLeft);
                break;

            case TOP_CENTER:
                label.setAlignment(com.badlogic.gdx.utils.Align.top | com.badlogic.gdx.utils.Align.center);
                break;

            case TOP_RIGHT:
                label.setAlignment(com.badlogic.gdx.utils.Align.topRight);
                break;
        }

        return label;
    }
}
