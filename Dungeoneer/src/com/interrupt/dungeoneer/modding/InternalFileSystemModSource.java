package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;

public class InternalFileSystemModSource extends AbstractFileSystemModSource {
    public InternalFileSystemModSource(String root) {
        super(root);
    }

    @Override
    protected FileHandle getRootHandle() {
        return Game.getInternal(root);
    }
}
