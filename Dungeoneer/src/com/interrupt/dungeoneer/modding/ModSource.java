package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.utils.Array;

interface ModSource {
    /** Get paths to all local mods. */
    Array<String> getInstalledMods();

    /** Get paths to all enabled mods. */
    Array<String> getEnabledMods();

    /** Get info for all available mods. These may not be locally installed. */
    Array<ModData> getAvailableMods();
}
