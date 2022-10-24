package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.selection.TileSelection;

public class EditorMode {
    public enum EditorModes { ENTITY_PICKED, CARVE, PAINT, TEXPAN, DRAW, ERASE, FLATTEN, STAIRS, RAMP, RAMP2, RAMP3, PYRAMID, ARCH, DOME, NOISE, LANDSCAPE }

    public EditorModes mode;
    protected TileSelection hoverSelection;
    protected Array<TileSelection> pickedTileSelections = new Array<>();

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

    private Array<TileSelection> foundSelections = new Array<>();
    public Array<TileSelection> getPickedTileSelections(boolean includeHovered) {
        if(pickedTileSelections.size > 0) {
            return pickedTileSelections;
        }

        if(!includeHovered)
            return pickedTileSelections;

        foundSelections.clear();
        foundSelections.add(hoverSelection);
        return foundSelections;
    }
}
