package com.interrupt.managers;

import com.interrupt.dungeoneer.ui.Hotbar;
import com.interrupt.dungeoneer.ui.layout.Element;

import java.util.HashMap;

public class HUDManager {
    public Hotbar quickSlots = new Hotbar(6, 1, 0);
    public Hotbar backpack = new Hotbar(6, 3, 6);

    public HashMap<String, Element> prefabs = new HashMap<>();

    public void merge(HUDManager other) {
        if (null != other.quickSlots) {
            quickSlots = other.quickSlots;
        }

        if (null != other.backpack) {
            backpack = other.backpack;
        }

        if (null != other.prefabs) {
            prefabs = other.prefabs;
        }
    }

    public Element getPrefab(String name) {
        return prefabs.getOrDefault(name, null);
    }
}
