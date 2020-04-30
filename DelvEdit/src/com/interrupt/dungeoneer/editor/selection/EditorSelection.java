package com.interrupt.dungeoneer.editor.selection;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;

import java.util.Iterator;

/** Editor selection subsystem. */
public class EditorSelection {
    public Entity hovered;

    /** The first entity picked of the selection. */
    public Entity picked;

    /** Array of selected entities. Does not contain the picked entity. */
    public Array<Entity> selected;

    /** Iterable containing the picked and selected entities. */
    public Iterable<Entity> all;

    public TileSelection tiles;

    public EditorSelection() {
        hovered = null;
        picked = null;
        selected = new Array<Entity>();
        tiles = new TileSelection();

        all = new Iterable<Entity>() {
            @Override
            public Iterator<Entity> iterator() {
                Array<Entity> result = new Array<Entity>();
                if (picked != null) {
                    result.add(picked);
                }

                result.addAll(selected);

                return result.iterator();
            }
        };
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
