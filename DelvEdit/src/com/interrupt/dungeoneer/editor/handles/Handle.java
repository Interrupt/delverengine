package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Array;

import java.lang.ref.WeakReference;
import java.util.Iterator;

public abstract class Handle extends InputAdapter {
    private static int handleIds = 1;
    private final int id;

    public boolean hovered;
    public boolean selected;

    public Handle() {
        id = handleIds++;
        handles.add(new WeakReference<>(this));
    }

    public int getId() {
        return id;
    }

    public abstract void draw();

    /** Called when cursor is moved over handle. */
    public void enter() {
        hovered = true;
    }

    /** Called when cursor is moved out of handle. */
    public void exit() {
        hovered = false;
    }

    /** Called when handle becomes selected. */
    public void select() {}

    /** Called when handle become deselected. */
    public void deselect() {}

    /** Called when handle is manipulated. */
    public void change() {}

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != 0) {
            hovered = false;
        }

        if (selected != hovered) {
            if (!hovered) deselect();
            else select();
        }
        selected = hovered;

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        selected = false;
        deselect();
        return false;
    }

    private static final Array<WeakReference<Handle>> handles = new Array<>();
    /** Gets the handle object for the given id. */
    public static Handle get(int id) {
        for (WeakReference<Handle> ref : handles) {
            Handle handle = ref.get();
            if (handle == null) continue;
            if (handle.getId() == id) return handle;
        }

        return null;
    }

    public static Array<Handle> result = new Array<>();
    public static Iterable<Handle> all = new Iterable<Handle>() {
        @Override
        public Iterator<Handle> iterator() {
            result.clear();

            for (Iterator<WeakReference<Handle>> it = handles.iterator(); it.hasNext(); ) {
                WeakReference<Handle> ref = it.next();
                Handle handle = ref.get();

                // Clean up list if needed.
                if (handle == null) {
                    it.remove();
                    continue;
                }

                result.add(handle);
            }

            return result.iterator();
        }
    };
}
