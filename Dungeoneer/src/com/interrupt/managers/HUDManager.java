package com.interrupt.managers;

import com.interrupt.dungeoneer.ui.Hotbar;

public class HUDManager {
    public Hotbar quickSlots;
    public Hotbar backpack;

    public void merge(HUDManager other) {
        if (null != other.quickSlots) {
            quickSlots = other.quickSlots;
        }

        if (null != other.backpack) {
            backpack = other.backpack;
        }
    }
}
