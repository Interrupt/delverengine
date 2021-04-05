package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public abstract class AbstractFileSystemModSource implements ModSource {
    /** The root file path to look for mod content. */
    protected String root;

    protected AbstractFileSystemModSource() {
    }

    protected AbstractFileSystemModSource(String root) {
        this.root = root;
    }

    protected abstract FileHandle getRootHandle();

    @Override
    public Array<String> getInstalledMods() {
        Array<String> mods = new Array<>();
        FileHandle rootHandle = getRootHandle();

        if (!rootHandle.isDirectory()) {
            return mods;
        }

        for (FileHandle childHandle : rootHandle.list()) {
            if (childHandle.isDirectory()) {
                mods.add(childHandle.path());
            }
        }

        return mods;
    }
}
