package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class TextElement extends Element{
    public String text = "";
    public Color color = Color.WHITE;

    @Override
    public Actor createActor() {
        Label label = new Label(text, UiSkin.getSkin());
        label.setPosition(x, y);
        label.setColor(color.r, color.g, color.b, 1f);

        return label;
    }
}
