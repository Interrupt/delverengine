package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.game.Game;

public class LocalFileSystemModSource extends AbstractFileSystemModSource {
    @Override
    protected FileHandle getRootHandle() {
        return Game.getFile(root);
    }
}
