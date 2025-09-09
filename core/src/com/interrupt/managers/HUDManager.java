package com.interrupt.managers;

import com.interrupt.dungeoneer.ui.Hotbar;

public class HUDManager {
    public Hotbar quickSlots = new Hotbar(6, 1, 0);
    public Hotbar backpack = new Hotbar(6, 3, 6);

    public void merge(HUDManager other) {
        if (null != other.quickSlots) {
            quickSlots = other.quickSlots;
        }

        if (null != other.backpack) {
            backpack = other.backpack;
        }
    }
}
