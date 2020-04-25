package com.interrupt.dungeoneer.editor.selection;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;

/** Editor selection subsystem. */
public class EditorSelection {
    public Entity hovered;
    public Entity picked;
    public Array<Entity> selected;
    public TileSelection tiles;

    public EditorSelection() {
        hovered = null;
        picked = null;
        selected = new Array<Entity>();
        tiles = new TileSelection();
    }

    public void clear() {
        hovered = null;
        picked = null;
        selected.clear();
        tiles.clear();
    }

    public boolean isSelected(Entity e) {
        return selected.contains(e, true);
    }
}
