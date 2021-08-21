package com.interrupt.managers;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.ui.Hotbar;
import com.interrupt.dungeoneer.ui.IHUDRenderable;

public class HUDManager {
    public Hotbar quickSlots = new Hotbar(6, 1, 0);
    public Hotbar backpack = new Hotbar(6, 3, 6);

    public Array<IHUDRenderable> renderables = new Array<>();

    public void merge(HUDManager other) {
        if (null != other.quickSlots) {
            quickSlots = other.quickSlots;
        }

        if (null != other.backpack) {
            backpack = other.backpack;
        }

        if (other.renderables.size > 0) {
            renderables = other.renderables;
        }
    }

    public void draw() {
        for (IHUDRenderable renderable : renderables) {
            renderable.draw();
        }
    }
}
