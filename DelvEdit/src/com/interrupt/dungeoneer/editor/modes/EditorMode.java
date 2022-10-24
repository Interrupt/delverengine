package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Input;
import com.interrupt.dungeoneer.editor.EditorApplication;

public class EditorMode {
    public enum EditorModes { ENTITY_PICKED, CARVE, PAINT, DRAW, ERASE, FLATTEN, STAIRS, RAMP, RAMP2, RAMP3, PYRAMID, ARCH, DOME, NOISE, LANDSCAPE }

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

    public void refresh() {

    }

    // Override this to take action when the state changes to another mode
    public void onSwitchTo(EditorMode newMode) {

    }
}
