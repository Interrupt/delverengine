package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.dungeoneer.ui.values.DynamicValue;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;
import java.util.Objects;

public class DynamicFormatText extends Element {
    public String pattern = "{0}";

    public Array<DynamicValue> args = new Array<>();

    public String getText() {
        Array<String> actualArgs = new Array<>();

        for (DynamicValue arg : args) {
            actualArgs.add(arg.stringValue());
        }

        return MessageFormat.format(
            StringManager.get(pattern),
            (Object[])actualArgs.toArray()
        );
    }

    @Override
    protected Actor createActor() {
        DynamicFormatText self = this;
        Label label = new Label(getText(), UiSkin.getSkin()) {
            private String value;
            private float lastCheck = 0f;

            @Override
            public void act(float delta) {
                super.act(delta);

                // Only check if we need to update the text at 10hz
                float frequency = 1000f / 10f;
                if (Game.instance.time < lastCheck + frequency) return;

                lastCheck = Game.instance.time;

                if (!Objects.equals(value, self.getText())) {
                    value = self.getText();
                    setText(value);
                }
            }
        };

        if (pivot.equals(com.interrupt.dungeoneer.ui.layout.Align.UNSET)) {
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
