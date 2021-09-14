package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.ui.ItemSlot;
import com.interrupt.dungeoneer.ui.UiSkin;

public class HoveredItemName extends Element{
    public Color color = Color.WHITE;

    @Override
    public Actor createActor() {
        Label label = new Label("X", UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);
                if (ItemSlot.hovered == null) {
                    setText("");
                    return;
                }
                setText(ItemSlot.hovered.name);
            }
        };
        label.setPosition(x, y);
        label.setColor(color.r, color.g, color.b, 1f);

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
