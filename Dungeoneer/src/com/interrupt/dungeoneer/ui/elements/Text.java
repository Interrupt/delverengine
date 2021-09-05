package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;

public class Text extends Element{
    public String text = "";

    @Override
    public Actor createActor() {
        Label label = new Label(StringManager.get(text), UiSkin.getSkin());

        com.interrupt.dungeoneer.ui.elements.Align a = pivot;
        if (pivot.equals(com.interrupt.dungeoneer.ui.elements.Align.UNSET)) {
            pivot = anchor;
        }

        switch (pivot) {
            case BOTTOM_LEFT:
                label.setAlignment(Align.bottomLeft);
                break;

            case BOTTOM_CENTER:
                label.setAlignment(Align.bottom | Align.center);
                break;

            case BOTTOM_RIGHT:
                label.setAlignment(Align.bottomRight);
                break;

            case CENTER_LEFT:
                label.setAlignment(Align.left | Align.center);
                break;

            case CENTER:
                label.setAlignment(Align.center);
                break;

            case CENTER_RIGHT:
                label.setAlignment(Align.right | Align.center);
                break;

            case TOP_LEFT:
                label.setAlignment(Align.topLeft);
                break;

            case TOP_CENTER:
                label.setAlignment(Align.top | Align.center);
                break;

            case TOP_RIGHT:
                label.setAlignment(Align.topRight);
                break;
        }

        return label;
    }
}
