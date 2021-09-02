package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class Element {
    public int x = 0;
    public int y = 0;

    public Align anchor = Align.BOTTOM_LEFT;
    public Align pivot = Align.UNSET;

    public Color color = Color.WHITE;

    protected Canvas canvas;

    private Actor actor;

    public void init() {
        actor = createActor();
        actor.setColor(color);
    }

    protected abstract Actor createActor();

    public Actor getActor() {
        return actor;
    }

    public void setCanvas(Canvas c) {
        canvas = c;
    }
}
