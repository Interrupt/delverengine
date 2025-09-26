package com.interrupt.dungeoneer.editor.ui.menu;

import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;

public class AssetListItem {

    public String path;
    public String name;
    public boolean isDirectory;
    public String parent = null;

    public AssetListItem(String path, String name) {
        this.name = name;
        this.path = path;
    }

    public AssetListItem(String path, String name, boolean isDirectory, String parent){
        if(isDirectory){
            name += "/";
        }
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.parent = parent;
    }

    public FileHandle getFile() {
        return Game.findInternalFileInMods(path);
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof AssetListItem) {
            AssetListItem o = (AssetListItem)other;
            return o.path.equals(path);
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
