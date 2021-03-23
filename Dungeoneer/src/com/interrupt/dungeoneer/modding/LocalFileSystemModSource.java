package com.interrupt.dungeoneer.modding;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;

public class LocalFileSystemModSource implements ModSource {
    /** The root file path to look for mod content. */
    public String root;

    /** All available mods from this local file system. */
    private transient Array<String> mods = new Array<>();

    /** Mapping for a mods enabled state. */
    private HashMap<String, Boolean> modsEnabled = new HashMap<>();

    public LocalFileSystemModSource(String root) {
        this.root = root;
    }

    /** Refreshes the mod source. */
    public void refresh() {
        mods.clear();
        modsEnabled.clear();

        FileHandle rootHandle = Game.getFile(root);

        if (!rootHandle.isDirectory()) {
            Gdx.app.error("LocalFileSystemModSource", "The provided root '" + root + "' is not a valid mod directory.");
        }

        for (FileHandle childHandle : rootHandle.list()) {
            if (childHandle.isDirectory()) {
                String mod = root + "/" + childHandle.name();

                mods.add(mod);
                modsEnabled.put(mod, false);
            }
        }

        Gdx.app.log("LocalFileSystemModSource", "Loaded " + mods.size + " mods from '" + root + "'");
    }

    @Override
    public Array<String> getInstalledMods() {
        return mods;
    }

    @Override
    public Array<String> getEnabledMods() {
        Array<String> modsFiltered = new Array<>();

        for (String mod : mods) {
            if (modsEnabled.containsKey(mod) && modsEnabled.get(mod).booleanValue()) {
                modsFiltered.add(mod);
            }
        }

        return modsFiltered;
    }

    @Override
    public Array<ModData> getAvailableMods() {
        return new Array<>();
    }
}
