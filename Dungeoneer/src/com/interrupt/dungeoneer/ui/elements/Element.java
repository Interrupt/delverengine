package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.interrupt.dungeoneer.ui.Align;

public abstract class Element {
    public int x = 0;
    public int y = 0;

    public Align anchor = Align.BOTTOM_LEFT;
    public Align pivot = Align.UNSET;

    protected Canvas canvas;

    private Actor actor;

    public void init() {
        actor = createActor();
    }

    protected abstract Actor createActor();

    public Actor getActor() {
        return actor;
    }

    public void setCanvas(Canvas c) {
        canvas = c;
    }
}
