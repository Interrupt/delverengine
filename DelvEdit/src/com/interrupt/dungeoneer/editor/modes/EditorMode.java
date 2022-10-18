package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Input;
import com.interrupt.dungeoneer.editor.EditorApplication;

public class EditorMode {
    public enum EditorModes { ENTITY_PICKED, CARVE, PAINT, ARCH, DOME, STAIRS, RAMP, NOISE }

    public EditorModes mode;

    public EditorMode(EditorModes mode) {
        this.mode = mode;
    }

    // Override this to draw any editor mode gizmos
    public void draw() {

    }

    // Override this to implement editor mode logic
    public void tick() {

    }

    // Override this to run logic when being switched to
    public void start() {

    }

    // Override this to reset the state
    public void reset() {

    }
}
