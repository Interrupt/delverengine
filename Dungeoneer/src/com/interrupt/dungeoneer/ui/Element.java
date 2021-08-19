package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class Element {
    public int x = 0;
    public int y = 0;

    public Align anchor = Align.BOTTOM_LEFT;

    public Actor getActor() {
        return new Actor();
    }
}
