package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.utils.Array;

public interface ModSource {
    /** Gets paths to all local mods. */
    Array<String> getInstalledMods();
}
