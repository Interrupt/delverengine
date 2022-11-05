package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.ui.ToolMenuBar;

public class EditorMode {
    public enum EditorModes { ENTITY_PICKED, ENTITY_TRANSLATE, ENTITY_ROTATE, ENTITY_SCALE, CARVE, VERTEX, PAINT, TEXPAN, DRAW, ERASE, FLATTEN, STAIRS, RAMP, RAMP2, RAMP3, PYRAMID, ARCH, DOME, NOISE, LANDSCAPE }

    public EditorModes mode;
    protected TileSelection hoverSelection;
    protected Array<TileSelection> pickedTileSelections = new Array<>();

    // Whether this should show at the top level of tools
    public boolean showAtTopLevel = true;

    public EditorMode parentEditorMode;
    public Array<EditorModes> subModes = new Array<>();

    public EditorMode(EditorModes mode) {
        this.mode = mode;
    }

    public EditorMode(EditorModes mode, EditorMode inParentEditorMode) {
        this.mode = mode;
        showAtTopLevel = inParentEditorMode == null;
        parentEditorMode = inParentEditorMode;
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

    // Override this to take action when the state is changing to another mode
    public void onSwitchTo(EditorMode newMode) {

    }

    // Override this to take action when the state changes to another mode
    public void onSwitchFrom(EditorMode oldMode) {

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
