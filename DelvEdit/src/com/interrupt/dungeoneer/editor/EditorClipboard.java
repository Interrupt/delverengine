package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.entities.Entity;

public class EditorClipboard {
    public Array<Entity> entities = new Array<Entity>();
    public Array<TileSelectionInfo> tiles = new Array<TileSelectionInfo>();
}
