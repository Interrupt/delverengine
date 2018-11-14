package com.interrupt.dungeoneer.editor.ui.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntMap;
import com.interrupt.dungeoneer.input.ReadableKeys;

public class MenuAccelerator {
    private int keystroke;
    private boolean controlPressed;
    private boolean shiftPressed;

    public MenuAccelerator(int keystroke, boolean controlPressed, boolean shiftPressed) {
        this.keystroke = keystroke;
        this.controlPressed = controlPressed;
        this.shiftPressed = shiftPressed;
    }

    @Override
    public String toString() {
        return ReadableKeys.keyNames.get(keystroke);
    }

    public int getKeystroke() {
        return keystroke;
    }

    public boolean getControlRequired() {
        return  controlPressed;
    }

    public boolean getShiftRequired() {
        return shiftPressed;
    }
}
